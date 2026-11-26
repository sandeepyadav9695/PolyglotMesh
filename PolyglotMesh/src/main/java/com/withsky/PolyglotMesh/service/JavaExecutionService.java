package com.withsky.PolyglotMesh.service;

import com.withsky.PolyglotMesh.dto.ExecutionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class JavaExecutionService {

    private static final int MAX_OUTPUT_BYTES = 64 * 1024;

    private final String dockerImage;
    private final long timeoutSeconds;

    public JavaExecutionService(
            @Value("${polyglot.java.docker-image:eclipse-temurin:21-jdk}")
            String dockerImage,

            @Value("${polyglot.java.timeout-seconds:10}")
            long timeoutSeconds
    ) {
        this.dockerImage = dockerImage;
        this.timeoutSeconds = timeoutSeconds;
    }

    public ExecutionResponse execute(String code) {

        long startTime = System.nanoTime();

        Path workspace = null;
        String containerName =
                "polyglot-java-" + UUID.randomUUID().toString().substring(0, 12);

        boolean containerStarted = false;

        try {
            if (code == null || code.isBlank()) {
                return response(
                        false, "", "Java source code cannot be empty",
                        startTime, false
                );
            }

            // Create isolated temporary workspace.
            workspace = Files.createTempDirectory("polyglot-java-");

            Path sourceFile = workspace.resolve("Main.java");

            Files.writeString(
                    sourceFile,
                    code,
                    StandardCharsets.UTF_8
            );

            String mount = "type=bind,source="
                    + workspace.toAbsolutePath()
                    + ",target=/workspace";

            /*
             * User-provided code is never inserted into this shell command.
             * The command itself is fixed; only Main.java is user-controlled.
             */
            String executionScript =
                    "javac -proc:none -encoding UTF-8 "
                            + "-d /workspace /workspace/Main.java "
                            + "&& java "
                            + "-Xms16m "
                            + "-Xmx64m "
                            + "-XX:MaxMetaspaceSize=64m "
                            + "-XX:ReservedCodeCacheSize=16m "
                            + "-Djava.io.tmpdir=/tmp "
                            + "-cp /workspace Main";

            ProcessBuilder builder = new ProcessBuilder(
                    "docker",
                    "run",
                    "--rm",
                    "--name", containerName,
                    "--network=none",
                    "--memory=256m",
                    "--memory-swap=256m",
                    "--cpus=0.5",
                    "--pids-limit=64",
                    "--read-only",
                    "--cap-drop=ALL",
                    "--security-opt=no-new-privileges",
                    "--tmpfs", "/tmp:rw,noexec,nosuid,size=16m",
                    "--mount", mount,
                    "--workdir", "/workspace",
                    dockerImage,
                    "sh", "-c", executionScript
            );

            builder.redirectErrorStream(false);

            Process process = builder.start();
            containerStarted = true;

            // Drain both streams concurrently to prevent pipe deadlocks.
            CompletableFuture<byte[]> stdoutFuture =
                    CompletableFuture.supplyAsync(
                            () -> readLimited(process.getInputStream())
                    );

            CompletableFuture<byte[]> stderrFuture =
                    CompletableFuture.supplyAsync(
                            () -> readLimited(process.getErrorStream())
                    );

            boolean completed = process.waitFor(
                    timeoutSeconds,
                    TimeUnit.SECONDS
            );

            if (!completed) {
                killContainer(containerName);

                process.destroyForcibly();
                process.waitFor(2, TimeUnit.SECONDS);

                String output = readFuture(stdoutFuture);
                String error = readFuture(stderrFuture);

                return response(
                        false,
                        output,
                        appendMessage(
                                error,
                                "Java execution timed out after "
                                        + timeoutSeconds + " seconds"
                        ),
                        startTime,
                        true
                );
            }

            int exitCode = process.exitValue();

            String output = readFuture(stdoutFuture);
            String error = readFuture(stderrFuture);

            return response(
                    exitCode == 0,
                    output,
                    error,
                    startTime,
                    false
            );

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            if (containerStarted) {
                killContainer(containerName);
            }

            return response(
                    false,
                    "",
                    "Java execution was interrupted",
                    startTime,
                    true
            );

        } catch (Exception ex) {

            if (containerStarted) {
                killContainer(containerName);
            }

            return response(
                    false,
                    "",
                    "Java execution failed: " + ex.getMessage(),
                    startTime,
                    false
            );

        } finally {

            // Remove temporary source and compiled class files.
            if (workspace != null) {
                deleteWorkspace(workspace);
            }
        }
    }

    private byte[] readLimited(InputStream inputStream) {

        try (InputStream input = inputStream;
             ByteArrayOutputStream captured = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int count;
            int remaining = MAX_OUTPUT_BYTES;

            while ((count = input.read(buffer)) != -1) {

                if (remaining > 0) {
                    int bytesToWrite = Math.min(count, remaining);

                    captured.write(buffer, 0, bytesToWrite);
                    remaining -= bytesToWrite;
                }
                // Continue draining even after reaching the output limit.
            }

            return captured.toByteArray();

        } catch (IOException ex) {
            return ("Unable to read process output: " + ex.getMessage())
                    .getBytes(StandardCharsets.UTF_8);
        }
    }

    private String readFuture(CompletableFuture<byte[]> future) {

        try {
            return new String(
                    future.get(3, TimeUnit.SECONDS),
                    StandardCharsets.UTF_8
            );

        } catch (Exception ex) {
            future.cancel(true);
            return "Unable to collect execution output";
        }
    }

    private void killContainer(String containerName) {

        try {
            Process kill = new ProcessBuilder(
                    "docker",
                    "kill",
                    containerName
            ).start();

            if (!kill.waitFor(3, TimeUnit.SECONDS)) {
                kill.destroyForcibly();
            }

        } catch (Exception ignored) {
            // Best-effort container termination.
        }
    }

    private void deleteWorkspace(Path workspace) {

        try (var paths = Files.walk(workspace)) {

            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best-effort temporary file cleanup.
                        }
                    });

        } catch (IOException ignored) {
            // Best-effort workspace cleanup.
        }
    }

    private ExecutionResponse response(
            boolean success,
            String output,
            String error,
            long startTime,
            boolean resourceExhausted
    ) {
        long executionTime =
                Duration.ofNanos(System.nanoTime() - startTime).toMillis();

        return new ExecutionResponse(
                success,
                "java",
                output,
                error,
                executionTime,
                resourceExhausted
        );
    }

    private String appendMessage(String existing, String message) {
        if (existing == null || existing.isBlank()) {
            return message;
        }

        return existing + System.lineSeparator() + message;
    }
}

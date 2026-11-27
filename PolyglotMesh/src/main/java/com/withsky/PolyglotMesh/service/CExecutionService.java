package com.withsky.PolyglotMesh.service;


import com.withsky.PolyglotMesh.dto.ExecutionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CExecutionService {

    private final String dockerImage;
    private final long timeoutSeconds;

    public CExecutionService(
            @Value("${polyglot.c.docker-image:gcc:13}") String dockerImage,
            @Value("${polyglot.c.timeout-seconds:10}") long timeoutSeconds) {
        this.dockerImage = dockerImage;
        this.timeoutSeconds = timeoutSeconds;
    }

    public ExecutionResponse execute(String code) {
        long start = System.currentTimeMillis();
        Path workspace = null;
        String containerName = "polyglot-c-" +
                UUID.randomUUID().toString().substring(0, 8);

        try {
            workspace = Files.createTempDirectory("polyglot-c-");

            Path sourceFile = workspace.resolve("main.c");
            Path outputFile = workspace.resolve("output.txt");

            Files.writeString(sourceFile, code, StandardCharsets.UTF_8);

            String mount = "type=bind,source=" +
                    workspace.toAbsolutePath() +
                    ",target=/workspace";

            String script =
                    "gcc -std=c17 -O2 -Wall -Wextra " +
                            "/workspace/main.c -o /workspace/program " +
                            "&& /workspace/program " +
                            "> /workspace/output.txt 2>&1";

            ProcessBuilder builder = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "--name", containerName,
                    "--network=none",
                    "--memory=128m",
                    "--memory-swap=128m",
                    "--cpus=0.5",
                    "--pids-limit=32",
                    "--read-only",
                    "--cap-drop=ALL",
                    "--security-opt=no-new-privileges",
                    "--tmpfs", "/tmp:rw,noexec,nosuid,size=16m",
                    "--mount", mount,
                    "--workdir", "/workspace",
                    "--entrypoint", "/bin/sh",
                    dockerImage,
                    "-c", script
            );

            builder.redirectErrorStream(true);
            builder.redirectOutput(outputFile.toFile());

            Process process = builder.start();

            long timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);

            boolean completed = process.waitFor(
                    timeoutMillis,
                    TimeUnit.MILLISECONDS
            );

            if (!completed) {
                process.destroyForcibly();

                new ProcessBuilder("docker", "kill", containerName)
                        .start()
                        .waitFor(3, TimeUnit.SECONDS);

                return new ExecutionResponse(
                        false,
                        "c",
                        "",
                        "C execution exceeded " + timeoutSeconds + " seconds",
                        System.currentTimeMillis() - start,
                        true
                );
            }

            String output = Files.exists(outputFile)
                    ? Files.readString(outputFile, StandardCharsets.UTF_8)
                    : "";

            int exitCode = process.exitValue();

            return new ExecutionResponse(
                    exitCode == 0,
                    "c",
                    output,
                    exitCode == 0 ? null : output,
                    System.currentTimeMillis() - start,
                    false
            );

        } catch (IOException e) {
            return new ExecutionResponse(
                    false, "c", "",
                    "C execution failed: " + e.getMessage(),
                    System.currentTimeMillis() - start, false);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return new ExecutionResponse(
                    false, "c", "",
                    "C execution interrupted",
                    System.currentTimeMillis() - start, true);

        } finally {
            if (workspace != null) {
                try (var files = Files.walk(workspace)) {
                    files.sorted(java.util.Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignored) {
                                }
                            });
                } catch (IOException ignored) {
                }
            }
        }
    }
}

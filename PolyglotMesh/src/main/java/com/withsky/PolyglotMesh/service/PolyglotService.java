
package com.withsky.PolyglotMesh.service;

import com.withsky.PolyglotMesh.config.SandboxConfig;
import com.withsky.PolyglotMesh.dto.ExecutionRequest;
import com.withsky.PolyglotMesh.dto.ExecutionResponse;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PolyglotService {

    private final SandboxConfig sandboxConfig;
    private final JavaExecutionService javaExecutionService;
    private final CExecutionService cExecutionService;

    public PolyglotService(
            SandboxConfig sandboxConfig,
            JavaExecutionService javaExecutionService,
            CExecutionService cExecutionService) {

        this.sandboxConfig = sandboxConfig;
        this.javaExecutionService = javaExecutionService;
        this.cExecutionService = cExecutionService;
    }

    public ExecutionResponse execute(ExecutionRequest request) {

        String language = request.language();
        String code = request.code();

        // Java compilation and execution through Docker.
        if ("java".equalsIgnoreCase(language)) {
            return javaExecutionService.execute(code);
        }

        // C compilation and execution through GCC Docker image.
        if ("c".equalsIgnoreCase(language)) {
            return cExecutionService.execute(code);
        }

        long startTime = System.nanoTime();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        try {
            try (Context context = sandboxConfig.createBuilder(
                    language,
                    output,
                    error
            ).build()) {

                String guestLanguage =
                        "javascript".equalsIgnoreCase(language)
                                || "js".equalsIgnoreCase(language)
                                ? "js"
                                : "python";

                context.eval(guestLanguage, code);
            }

            return new ExecutionResponse(
                    true,
                    language,
                    output.toString(StandardCharsets.UTF_8),
                    error.toString(StandardCharsets.UTF_8),
                    executionTime(startTime),
                    false
            );

        } catch (PolyglotException ex) {

            boolean resourceExhausted = ex.isResourceExhausted();

            String message = resourceExhausted
                    ? "Sandbox resource limit exceeded: " + ex.getMessage()
                    : ex.getMessage();

            return new ExecutionResponse(
                    false,
                    language,
                    output.toString(StandardCharsets.UTF_8),
                    message,
                    executionTime(startTime),
                    resourceExhausted
            );

        } catch (Exception ex) {

            return new ExecutionResponse(
                    false,
                    language,
                    output.toString(StandardCharsets.UTF_8),
                    ex.getMessage() == null
                            ? "Code execution failed"
                            : ex.getMessage(),
                    executionTime(startTime),
                    false
            );
        }
    }

    private long executionTime(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}

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

    public PolyglotService(SandboxConfig sandboxConfig) {
        this.sandboxConfig = sandboxConfig;
    }

    public ExecutionResponse execute(ExecutionRequest request) {

        long startTime = System.nanoTime();

        String language = request.language();
        String code = request.code();

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        ByteArrayOutputStream error =
                new ByteArrayOutputStream();

        try {

            // Create a new isolated context for each request
            try (Context context =
                         sandboxConfig.createBuilder(
                                 language,
                                 output,
                                 error
                         ).build()) {

                String guestLanguage =
                        "javascript".equals(language)
                                ? "js"
                                : "python";

                // Execute guest code
                context.eval(guestLanguage, code);

            }

            long executionTime =
                    (System.nanoTime() - startTime) / 1_000_000;

            return new ExecutionResponse(
                    true,
                    language,
                    output.toString(StandardCharsets.UTF_8),
                    error.toString(StandardCharsets.UTF_8),
                    executionTime,
                    false
            );

        } catch (PolyglotException ex) {

            long executionTime =
                    (System.nanoTime() - startTime) / 1_000_000;

            boolean resourceExhausted =
                    ex.isResourceExhausted();

            String message = resourceExhausted
                    ? "Sandbox resource limit exceeded: "
                    + ex.getMessage()
                    : ex.getMessage();

            return new ExecutionResponse(
                    false,
                    language,
                    output.toString(StandardCharsets.UTF_8),
                    message,
                    executionTime,
                    resourceExhausted
            );

        } catch (Exception ex) {

            long executionTime =
                    (System.nanoTime() - startTime) / 1_000_000;

            return new ExecutionResponse(
                    false,
                    language,
                    output.toString(StandardCharsets.UTF_8),
                    ex.getMessage(),
                    executionTime,
                    false
            );
        }
    }
}

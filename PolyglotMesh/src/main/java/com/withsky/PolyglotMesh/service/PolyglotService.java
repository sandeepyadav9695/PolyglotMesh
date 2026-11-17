
package com.withsky.PolyglotMesh.service;

import com.withsky.PolyglotMesh.dto.ExecutionRequest;
import com.withsky.PolyglotMesh.dto.ExecutionResponse;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.io.IOAccess;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PolyglotService {

    public ExecutionResponse execute(ExecutionRequest request) {

        long startTime = System.nanoTime();

        String language = request.language().toLowerCase();
        String code = request.code();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        try {

            String languageId;

            if ("python".equals(language)) {
                languageId = "python";
            } else if ("javascript".equals(language)) {
                languageId = "js";
            } else {
                throw new IllegalArgumentException(
                        "Unsupported language: " + language
                );
            }

            Context.Builder builder = Context.newBuilder(languageId);

            builder
                    .out(output)
                    .err(error)
                    .allowHostAccess(HostAccess.NONE)
                    .allowHostClassLookup(name -> false)
                    .allowIO(IOAccess.NONE)
                    .allowNativeAccess(false)
                    .allowCreateThread(false)
                    .allowEnvironmentAccess(
                            org.graalvm.polyglot.EnvironmentAccess.NONE
                    );

            try (Context context = builder.build()) {

                context.eval(languageId, code);

            }

            long executionTime =
                    (System.nanoTime() - startTime) / 1_000_000;

            return new ExecutionResponse(
                    true,
                    language,
                    output.toString(StandardCharsets.UTF_8),
                    error.toString(StandardCharsets.UTF_8),
                    executionTime
            );

        } catch (PolyglotException ex) {

            long executionTime =
                    (System.nanoTime() - startTime) / 1_000_000;

            return new ExecutionResponse(
                    false,
                    language,
                    output.toString(StandardCharsets.UTF_8),
                    ex.getMessage(),
                    executionTime
            );

        } catch (Exception ex) {

            long executionTime =
                    (System.nanoTime() - startTime) / 1_000_000;

            return new ExecutionResponse(
                    false,
                    language,
                    output.toString(StandardCharsets.UTF_8),
                    ex.getMessage(),
                    executionTime
            );
        }
    }
}


package com.withsky.PolyglotMesh.config;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.SandboxPolicy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Component
public class SandboxConfig {

    // Guest CPU execution limit
    private static final String MAX_CPU_TIME = "2s";

    // Guest heap memory limit
    private static final String MAX_HEAP_MEMORY = "64MB";

    // Maximum isolate memory
    private static final String MAX_ISOLATE_MEMORY = "256MB";

    // Maximum guest AST depth
    private static final String MAX_AST_DEPTH = "64";

    // Maximum guest stack frames
    private static final String MAX_STACK_FRAMES = "32";

    // Maximum guest threads
    private static final String MAX_THREADS = "1";

    // Maximum output
    private static final String MAX_OUTPUT_SIZE = "64KB";

    // Maximum error output
    private static final String MAX_ERROR_SIZE = "16KB";

    public Context.Builder createBuilder(
            String language,
            ByteArrayOutputStream output,
            ByteArrayOutputStream error
    ) {

        if (language == null || output == null || error == null) {
            throw new IllegalArgumentException(
                    "Language and output streams cannot be null"
            );
        }

        String languageId = language.toLowerCase(Locale.ROOT);

        // Select supported language
        switch (languageId) {
            case "python":
                languageId = "python";
                break;

            case "javascript":
                languageId = "js";
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported language: " + language
                );
        }

        // Create a regular Polyglot context builder.
        // GraalPy does not require GraalPyResources for this approach.
        Context.Builder builder = Context.newBuilder(languageId);

        return builder

                // GraalVM sandbox policy
                .sandbox(SandboxPolicy.UNTRUSTED)

                // Restricted host access
                .allowHostAccess(HostAccess.UNTRUSTED)

                // Prevent guest access to host classes
                .allowHostClassLookup(className -> false)

                // No guest input from the host
                .in(new ByteArrayInputStream(new byte[0]))

                // Capture standard output and error
                .out(output)
                .err(error)

                // Maximum isolate memory
                .option(
                        "engine.MaxIsolateMemory",
                        MAX_ISOLATE_MEMORY
                )

                // Guest heap memory
                .option(
                        "sandbox.MaxHeapMemory",
                        MAX_HEAP_MEMORY
                )

                // Guest CPU execution time
                .option(
                        "sandbox.MaxCPUTime",
                        MAX_CPU_TIME
                )

                // Prevent deeply nested expressions
                .option(
                        "sandbox.MaxASTDepth",
                        MAX_AST_DEPTH
                )

                // Prevent excessive recursion
                .option(
                        "sandbox.MaxStackFrames",
                        MAX_STACK_FRAMES
                )

                // Restrict guest threads
                .option(
                        "sandbox.MaxThreads",
                        MAX_THREADS
                )

                // Limit standard output
                .option(
                        "sandbox.MaxOutputStreamSize",
                        MAX_OUTPUT_SIZE
                )

                // Limit error output
                .option(
                        "sandbox.MaxErrorStreamSize",
                        MAX_ERROR_SIZE
                );
    }
}

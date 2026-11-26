
package com.withsky.PolyglotMesh.config;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.SandboxPolicy;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Component
public class SandboxConfig {

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

        switch (languageId) {
            case "python":
                languageId = "python";
                break;

            case "javascript":
            case "js":
                languageId = "js";
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported GraalVM language: " + language
                );
        }

        SandboxPolicy policy =
                "python".equals(languageId)
                        ? SandboxPolicy.TRUSTED
                        : SandboxPolicy.CONSTRAINED;

        return Context.newBuilder(languageId)
                .sandbox(policy)
                .allowHostClassLookup(name -> false)
                .allowIO(IOAccess.NONE)
                .allowEnvironmentAccess(EnvironmentAccess.NONE)
                .allowNativeAccess(false)
                .allowCreateThread(false)
                .allowCreateProcess(false)
                .in(new ByteArrayInputStream(new byte[0]))
                .out(output)
                .err(error);
    }
}

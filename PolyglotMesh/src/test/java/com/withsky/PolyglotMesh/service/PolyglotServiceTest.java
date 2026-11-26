
package com.withsky.PolyglotMesh.service;

import com.withsky.PolyglotMesh.config.SandboxConfig;
import com.withsky.PolyglotMesh.dto.ExecutionRequest;
import com.withsky.PolyglotMesh.dto.ExecutionResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PolyglotServiceTest {

    private final SandboxConfig sandboxConfig =
            new SandboxConfig();

    private final JavaExecutionService javaExecutionService =
            new JavaExecutionService(
                    "eclipse-temurin:21-jdk",
                    10
            );

    private final PolyglotService service =
            new PolyglotService(
                    sandboxConfig,
                    javaExecutionService
            );

    @Test
    void shouldExecutePythonSuccessfully() {

        ExecutionRequest request = new ExecutionRequest(
                "python",
                "print('Hello from secure Python')"
        );

        ExecutionResponse response = service.execute(request);

        assertTrue(response.success());

        assertTrue(
                response.output().contains(
                        "Hello from secure Python"
                )
        );
    }

    @Test
    void shouldExecuteJavaScriptSuccessfully() {

        ExecutionRequest request = new ExecutionRequest(
                "javascript",
                "console.log('Hello from secure JavaScript');"
        );

        ExecutionResponse response = service.execute(request);

        assertTrue(response.success());

        assertTrue(
                response.output().contains(
                        "Hello from secure JavaScript"
                )
        );
    }

    @Test
    void shouldBlockJavaHostAccess() {

        ExecutionRequest request = new ExecutionRequest(
                "javascript",
                "Java.type('java.lang.System');"
        );

        ExecutionResponse response = service.execute(request);

        assertFalse(response.success());

        assertNotNull(response.error());
    }

    @Test
    void shouldNotShareJavaScriptState() {

        ExecutionRequest first = new ExecutionRequest(
                "javascript",
                "globalThis.secretValue = 123;"
        );

        ExecutionRequest second = new ExecutionRequest(
                "javascript",
                "console.log(typeof globalThis.secretValue);"
        );

        ExecutionResponse firstResponse = service.execute(first);

        ExecutionResponse secondResponse = service.execute(second);

        assertTrue(firstResponse.success());
        assertTrue(secondResponse.success());

        assertTrue(
                secondResponse.output().contains("undefined")
        );
    }

    @Test
    @Timeout(15)
    void shouldStopInfiniteJavaScriptLoop() {

        ExecutionRequest request = new ExecutionRequest(
                "javascript",
                "while (true) {}"
        );

        ExecutionResponse response = service.execute(request);

        assertFalse(response.success());

        assertTrue(response.resourceExhausted());
    }
}

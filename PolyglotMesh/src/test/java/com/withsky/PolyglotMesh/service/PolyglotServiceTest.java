
package com.withsky.PolyglotMesh.service;

import com.withsky.PolyglotMesh.config.SandboxConfig;
import com.withsky.PolyglotMesh.dto.ExecutionRequest;
import com.withsky.PolyglotMesh.dto.ExecutionResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolyglotServiceTest {

    private final SandboxConfig sandboxConfig = new SandboxConfig();

    @Mock
    private JavaExecutionService javaExecutionService;

    @Mock
    private CExecutionService cExecutionService;

    private PolyglotService createService() {
        return new PolyglotService(
                sandboxConfig,
                javaExecutionService,
                cExecutionService
        );
    }

    @Test
    void shouldExecutePythonSuccessfully() {

        PolyglotService service = createService();

        ExecutionRequest request = new ExecutionRequest(
                "python",
                "print('Hello from secure Python')"
        );

        ExecutionResponse response = service.execute(request);

        assertTrue(response.success());
        assertTrue(response.output().contains("Hello from secure Python"));
    }

    @Test
    void shouldExecuteJavaScriptSuccessfully() {

        PolyglotService service = createService();

        ExecutionRequest request = new ExecutionRequest(
                "javascript",
                "console.log('Hello from secure JavaScript');"
        );

        ExecutionResponse response = service.execute(request);

        assertTrue(response.success());
        assertTrue(response.output().contains("Hello from secure JavaScript"));
    }

    @Test
    void shouldExecuteJavaThroughDockerService() {

        PolyglotService service = createService();

        ExecutionRequest request = new ExecutionRequest(
                "java",
                "public class Main { public static void main(String[] args) {} }"
        );

        ExecutionResponse expected = new ExecutionResponse(
                true,
                "java",
                "Hello from Java",
                null,
                100,
                false
        );

        when(javaExecutionService.execute(request.code()))
                .thenReturn(expected);

        ExecutionResponse response = service.execute(request);

        assertTrue(response.success());
        assertEquals("Hello from Java", response.output());

        verify(javaExecutionService).execute(request.code());
        verifyNoInteractions(cExecutionService);
    }

    @Test
    void shouldExecuteCThroughDockerService() {

        PolyglotService service = createService();

        String code = """
                #include <stdio.h>
                int main() {
                    printf("Hello from C Compiler!");
                    return 0;
                }
                """;

        ExecutionRequest request = new ExecutionRequest("c", code);

        ExecutionResponse expected = new ExecutionResponse(
                true,
                "c",
                "Hello from C Compiler!",
                null,
                100,
                false
        );

        when(cExecutionService.execute(code)).thenReturn(expected);

        ExecutionResponse response = service.execute(request);

        assertTrue(response.success());
        assertEquals("Hello from C Compiler!", response.output());

        verify(cExecutionService).execute(code);
        verifyNoInteractions(javaExecutionService);
    }

    @Test
    void shouldBlockJavaHostAccess() {

        PolyglotService service = createService();

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

        PolyglotService service = createService();

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

        assertTrue(secondResponse.output().contains("undefined"));
    }

    @Test
    @Timeout(15)
    void shouldStopInfiniteJavaScriptLoop() {

        PolyglotService service = createService();

        ExecutionRequest request = new ExecutionRequest(
                "javascript",
                "while (true) {}"
        );

        ExecutionResponse response = service.execute(request);

        assertFalse(response.success());
        assertTrue(response.resourceExhausted());
    }
}

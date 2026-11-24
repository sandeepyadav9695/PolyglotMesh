
package com.withsky.PolyglotMesh;

import com.withsky.PolyglotMesh.exception.ExecutionTimeoutException;
import com.withsky.PolyglotMesh.exception.RateLimitException;
import com.withsky.PolyglotMesh.monitoring.ExecutionMetrics;
import com.withsky.PolyglotMesh.service.ExecutionGuardService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionGuardServiceTest {

    private ExecutionGuardService guardService;
    private ThreadPoolExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new ThreadPoolExecutor(
                2,
                4,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(20)
        );

        guardService = new ExecutionGuardService(
                executor,
                new ExecutionMetrics(new SimpleMeterRegistry())
        );
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void shouldExecuteSuccessfully() {
        String result = guardService.execute(
                "client-1",
                () -> "Hello PolyglotMesh"
        );

        assertEquals("Hello PolyglotMesh", result);
    }

    @Test
    void shouldRejectAfterRateLimit() {
        for (int i = 0; i < 10; i++) {
            guardService.execute("client-2", () -> "success");
        }

        assertThrows(
                RateLimitException.class,
                () -> guardService.execute(
                        "client-2",
                        () -> "extra request"
                )
        );
    }

    @Test
    void shouldTimeoutLongRunningTask() {
        assertThrows(
                ExecutionTimeoutException.class,
                () -> guardService.execute(
                        "client-3",
                        () -> {
                            try {
                                Thread.sleep(10_000);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                            return "finished";
                        }
                )
        );
    }
}

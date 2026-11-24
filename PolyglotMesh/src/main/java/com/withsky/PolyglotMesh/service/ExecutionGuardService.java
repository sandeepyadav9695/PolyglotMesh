package com.withsky.PolyglotMesh.service;
import com.withsky.PolyglotMesh.exception.ExecutionTimeoutException;
import com.withsky.PolyglotMesh.exception.RateLimitException;
import com.withsky.PolyglotMesh.monitoring.ExecutionMetrics;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Service
public class ExecutionGuardService {

    private static final int MAX_REQUESTS_PER_CLIENT = 10;
    private static final long EXECUTION_TIMEOUT_SECONDS = 2;

    private final ThreadPoolExecutor executor;
    private final ExecutionMetrics metrics;

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts =
            new ConcurrentHashMap<>();

    public ExecutionGuardService(
            ThreadPoolExecutor executor,
            ExecutionMetrics metrics
    ) {
        this.executor = executor;
        this.metrics = metrics;
    }

    public <T> T execute(String clientId, Supplier<T> task) {

        if (clientId == null || clientId.isBlank() || task == null) {
            throw new IllegalArgumentException(
                    "Client ID and task are required"
            );
        }

        int requests = requestCounts
                .computeIfAbsent(clientId, key -> new AtomicInteger())
                .incrementAndGet();

        if (requests > MAX_REQUESTS_PER_CLIENT) {
            metrics.failure();
            throw new RateLimitException("Rate limit exceeded for client: " + clientId);
        }

        long startTime = System.nanoTime();
        Future<T> future = null;

        try {
            future = executor.submit(task::get);

            T result = future.get(
                    EXECUTION_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );

            metrics.success();
            return result;

        } catch (TimeoutException ex) {

            if (future != null) {
                future.cancel(true);
            }

            metrics.timeout();
            throw new ExecutionTimeoutException(
                    "Code execution timed out"
            );

        } catch (InterruptedException ex) {

            if (future != null) {
                future.cancel(true);
            }

            Thread.currentThread().interrupt();
            metrics.failure();

            throw new RuntimeException(
                    "Execution was interrupted", ex
            );

        } catch (ExecutionException ex) {

            metrics.failure();

            Throwable cause = ex.getCause();

            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new RuntimeException(
                    "Execution failed", cause
            );

        } catch (RuntimeException ex) {

            metrics.failure();
            throw ex;

        } finally {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startTime
            );
            metrics.recordDuration(elapsed);
        }
    }
}

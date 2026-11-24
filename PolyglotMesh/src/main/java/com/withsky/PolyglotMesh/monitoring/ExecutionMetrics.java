
package com.withsky.PolyglotMesh.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ExecutionMetrics {

    private final Counter successfulExecutions;
    private final Counter failedExecutions;
    private final Counter timedOutExecutions;
    private final Timer executionTimer;

    public ExecutionMetrics(MeterRegistry registry) {

        successfulExecutions = Counter.builder(
                        "polyglot.executions.success")
                .description("Successful code executions")
                .register(registry);

        failedExecutions = Counter.builder(
                        "polyglot.executions.failure")
                .description("Failed code executions")
                .register(registry);

        timedOutExecutions = Counter.builder(
                        "polyglot.executions.timeout")
                .description("Timed out code executions")
                .register(registry);

        executionTimer = Timer.builder(
                        "polyglot.execution.duration")
                .description("Code execution duration")
                .register(registry);
    }

    public void success() {
        successfulExecutions.increment();
    }

    public void failure() {
        failedExecutions.increment();
    }

    public void timeout() {
        timedOutExecutions.increment();
    }

    public void recordDuration(long milliseconds) {
        executionTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }
}

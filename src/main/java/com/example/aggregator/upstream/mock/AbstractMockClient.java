package com.example.aggregator.upstream.mock;

import com.example.aggregator.config.AggregatorConfig;

import java.util.random.RandomGenerator;

abstract class AbstractMockClient {

    private final AggregatorConfig.MockServiceConfig mockConfig;

    protected AbstractMockClient(AggregatorConfig.MockServiceConfig mockConfig) {
        this.mockConfig = mockConfig;
    }

    protected void simulateLatency() {
        double jitter = 0.8 + RandomGenerator.getDefault().nextDouble() * 0.4;
        try {
            Thread.sleep(Math.round(mockConfig.latencyMs() * jitter));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during latency simulation", e);
        }
    }

    protected void simulateReliability(String serviceName) {
        if (RandomGenerator.getDefault().nextDouble() >= mockConfig.reliability()) {
            throw new RuntimeException(serviceName + ": simulated transient failure");
        }
    }
}
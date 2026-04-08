package com.example.aggregator.upstream.mock;

import com.example.aggregator.config.AggregatorConfig;
import com.example.aggregator.model.CustomerProfile;
import com.example.aggregator.upstream.api.CustomerClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Mock Customer Service.
 * Latency and reliability are configurable via application.yaml (aggregator.mocks.customer).
 */
@Component
public class MockCustomerClient implements CustomerClient {

    private static final Map<String, CustomerProfile> CUSTOMERS = Map.of(
            "C001", new CustomerProfile("C001", "DEALER"),
            "C002", new CustomerProfile("C002", "WORKSHOP"),
            "C003", new CustomerProfile("C003", "STANDARD"),
            "C004", new CustomerProfile("C004", "DEALER"),
            "C005", new CustomerProfile("C005", "WORKSHOP")
    );

    private final AggregatorConfig.MockServiceConfig mockConfig;

    public MockCustomerClient(AggregatorConfig config) {
        this.mockConfig = config.mocks().customer();
    }

    @Override
    public Optional<CustomerProfile> getCustomer(String customerId) {
        simulateLatency();
        simulateReliability("CustomerService");

        return Optional.ofNullable(CUSTOMERS.get(customerId));
    }

    private void simulateLatency() {
        double jitter = 0.8 + RandomGenerator.getDefault().nextDouble() * 0.4;
        try {
            Thread.sleep(Math.round(mockConfig.latencyMs() * jitter));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during latency simulation", e);
        }
    }

    private void simulateReliability(String serviceName) {
        if (RandomGenerator.getDefault().nextDouble() >= mockConfig.reliability()) {
            throw new RuntimeException(serviceName + ": simulated transient failure");
        }
    }
}

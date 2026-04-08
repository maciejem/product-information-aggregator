package com.example.aggregator.upstream.mock;

import com.example.aggregator.config.AggregatorConfig;
import com.example.aggregator.model.CustomerProfile;
import com.example.aggregator.upstream.api.CustomerClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Mock Customer Service.
 * Latency and reliability are configurable via application.yaml (aggregator.mocks.customer).
 */
@Component
public class MockCustomerClient extends AbstractMockClient implements CustomerClient {

    private static final Map<String, CustomerProfile> CUSTOMERS = Map.of(
            "C001", new CustomerProfile("C001", "DEALER"),
            "C002", new CustomerProfile("C002", "WORKSHOP"),
            "C003", new CustomerProfile("C003", "STANDARD"),
            "C004", new CustomerProfile("C004", "DEALER"),
            "C005", new CustomerProfile("C005", "WORKSHOP")
    );

    public MockCustomerClient(AggregatorConfig config) {
        super(config.mocks().customer());
    }

    @Override
    public Optional<CustomerProfile> getCustomer(String customerId) {
        simulateLatency();
        simulateReliability("CustomerService");

        return Optional.ofNullable(CUSTOMERS.get(customerId));
    }

}

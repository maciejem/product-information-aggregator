package com.example.aggregator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aggregator")
public record AggregatorConfig(Timeouts timeouts, Mocks mocks) {

    public record Timeouts(
            long catalogMs,
            long pricingMs,
            long availabilityMs,
            long customerMs
    ) {}

    public record Mocks(
            MockServiceConfig catalog,
            MockServiceConfig pricing,
            MockServiceConfig availability,
            MockServiceConfig customer
    ) {}

    public record MockServiceConfig(
            int latencyMs,
            double reliability
    ) {}
}

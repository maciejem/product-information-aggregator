package com.example.aggregator.upstream.mock;

import com.example.aggregator.config.AggregatorConfig;
import com.example.aggregator.model.Market;
import com.example.aggregator.model.StockInfo;
import com.example.aggregator.upstream.api.AvailabilityClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * Mock Availability Service.
 * Latency and reliability are configurable via application.yaml (aggregator.mocks.availability).
 */
@Component
public class MockAvailabilityClient extends AbstractMockClient implements AvailabilityClient {

    private static final Map<String, String> REGIONAL_WAREHOUSES = Map.of(
            "NL", "WH-AMS-01", "DE", "WH-FRA-01", "BE", "WH-AMS-01",
            "PL", "WH-WAW-01", "GB", "WH-LON-01", "SE", "WH-STO-01"
    );
    private static final Map<String, String> DELIVERY_ESTIMATES = Map.of(
            "WH-AMS-01", "1-2 business days",
            "WH-FRA-01", "1-2 business days",
            "WH-WAW-01", "2-3 business days",
            "WH-LON-01", "2-3 business days",
            "WH-STO-01", "3-4 business days"
    );

    public MockAvailabilityClient(AggregatorConfig config) {
        super(config.mocks().availability());
    }

    @Override
    public Optional<StockInfo> getAvailability(String productId, Market market) {
        simulateLatency();
        simulateReliability("AvailabilityService");

        String warehouseId = REGIONAL_WAREHOUSES.getOrDefault(market.region(), "WH-AMS-01");
        String delivery = DELIVERY_ESTIMATES.getOrDefault(warehouseId, "3-5 business days");
        int quantity = RandomGenerator.getDefault().nextInt(0, 50);

        return Optional.of(new StockInfo(quantity > 0, quantity, warehouseId, delivery));
    }

}

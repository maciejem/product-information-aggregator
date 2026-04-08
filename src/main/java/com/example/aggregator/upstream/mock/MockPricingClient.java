package com.example.aggregator.upstream.mock;

import com.example.aggregator.config.AggregatorConfig;
import com.example.aggregator.model.Market;
import com.example.aggregator.model.PriceInfo;
import com.example.aggregator.upstream.api.PricingClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Mock Pricing Service.
 * Latency and reliability are configurable via application.yaml (aggregator.mocks.pricing).
 */
@Component
public class MockPricingClient extends AbstractMockClient implements PricingClient {

    private static final Map<String, String> MARKET_CURRENCIES = Map.of(
            "NL", "EUR", "DE", "EUR", "BE", "EUR", "FR", "EUR",
            "PL", "PLN", "GB", "GBP", "SE", "SEK"
    );
    private static final Map<String, BigDecimal> BASE_PRICES = Map.of(
            "EUR", new BigDecimal("49.95"),
            "PLN", new BigDecimal("229.00"),
            "GBP", new BigDecimal("42.50"),
            "SEK", new BigDecimal("549.00")
    );
    private static final Map<String, BigDecimal> CUSTOMER_DISCOUNTS = Map.of(
            "C001", new BigDecimal("0.15"),
            "C002", new BigDecimal("0.10"),
            "C004", new BigDecimal("0.15"),
            "C005", new BigDecimal("0.10")
    );

    public MockPricingClient(AggregatorConfig config) {
        super(config.mocks().pricing());
    }

    @Override
    public Optional<PriceInfo> getPrice(String productId, Market market, String customerId) {
        simulateLatency();
        simulateReliability("PricingService");

        String currency = MARKET_CURRENCIES.getOrDefault(market.region(), "EUR");
        BigDecimal basePrice = BASE_PRICES.getOrDefault(currency, BASE_PRICES.get("EUR"));
        BigDecimal discount = customerId != null
                ? CUSTOMER_DISCOUNTS.getOrDefault(customerId, BigDecimal.ZERO)
                : BigDecimal.ZERO;

        return Optional.of(PriceInfo.of(basePrice, discount, currency));
    }

}

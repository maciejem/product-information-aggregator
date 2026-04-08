package com.example.aggregator.upstream.mock;

import com.example.aggregator.config.AggregatorConfig;
import com.example.aggregator.exception.ProductNotFoundException;
import com.example.aggregator.model.Market;
import com.example.aggregator.model.ProductDetails;
import com.example.aggregator.upstream.api.CatalogClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Mock Catalog Service.
 * Latency and reliability are configurable via application.yaml (aggregator.mocks.catalog).
 */
@Component
public class MockCatalogClient implements CatalogClient {

    private static final Set<String> KNOWN_PRODUCTS = Set.of(
            "BRAKE-PAD-001", "BRAKE-PAD-002",
            "FILTER-OIL-010", "FILTER-AIR-011",
            "BEARING-FRONT-100"
    );

    private final AggregatorConfig.MockServiceConfig mockConfig;

    public MockCatalogClient(AggregatorConfig config) {
        this.mockConfig = config.mocks().catalog();
    }

    @Override
    public ProductDetails getProduct(String productId, Market market) {
        simulateLatency();
        simulateReliability("CatalogService");

        if (!KNOWN_PRODUCTS.contains(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return new ProductDetails(
                productId,
                localizedName(market),
                localizedDescription(market),
                List.of(
                        "https://cdn.example.com/products/%s/main.jpg".formatted(productId),
                        "https://cdn.example.com/products/%s/detail.jpg".formatted(productId)
                ),
                Map.of("material", "semi-metallic", "thickness", "12mm", "oem_number", "BP-" + productId)
        );
    }

    private String localizedName(Market market) {
        return switch (market.language()) {
            case "nl" -> "Remblok Premium";
            case "de" -> "Bremsbelag Premium";
            case "pl" -> "Klocek hamulcowy Premium";
            default   -> "Premium Brake Pad";
        };
    }

    private String localizedDescription(Market market) {
        return switch (market.language()) {
            case "nl" -> "Hoogwaardige remblok geschikt voor zware toepassingen.";
            case "de" -> "Hochwertige Bremsbelaege fuer anspruchsvolle Anwendungen.";
            case "pl" -> "Wysokiej jakosci klocek hamulcowy do wymagajacych zastosowan.";
            default   -> "High-quality brake pad suitable for heavy-duty applications.";
        };
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

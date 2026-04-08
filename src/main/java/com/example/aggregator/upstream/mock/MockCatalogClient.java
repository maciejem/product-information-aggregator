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

/**
 * Mock Catalog Service.
 * Latency and reliability are configurable via application.yaml (aggregator.mocks.catalog).
 */
@Component
public class MockCatalogClient extends AbstractMockClient implements CatalogClient {

    private static final Set<String> KNOWN_PRODUCTS = Set.of(
            "BRAKE-PAD-001", "BRAKE-PAD-002",
            "FILTER-OIL-010", "FILTER-AIR-011",
            "BEARING-FRONT-100"
    );

    public MockCatalogClient(AggregatorConfig config) {
        super(config.mocks().catalog());
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

}

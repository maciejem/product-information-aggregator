package com.example.aggregator.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Aggregated product response. Optional sections are omitted from JSON when null.
 * dataAvailability is always present so the frontend knows which sections to expect.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
        String productId,
        String name,
        String description,
        List<String> imageUrls,
        Map<String, String> specs,
        String marketCode,
        PriceInfo price,
        StockInfo stock,
        CustomerContext customerContext,
        DataAvailability dataAvailability
) {
    public record CustomerContext(String segment, boolean personalized) {}

    public record DataAvailability(
            boolean priceAvailable,
            boolean stockAvailable,
            boolean customerContextAvailable
    ) {}
}

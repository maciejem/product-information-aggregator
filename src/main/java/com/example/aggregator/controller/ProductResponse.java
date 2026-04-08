package com.example.aggregator.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
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
        PriceResponse price,
        StockResponse stock,
        CustomerContext customerContext,
        DataAvailability dataAvailability
) {
    public record PriceResponse(BigDecimal basePrice, BigDecimal discountRate, BigDecimal finalPrice, String currency) {}

    public record StockResponse(boolean inStock, int quantity, String warehouseId, String estimatedDelivery) {}

    public record CustomerContext(String segment, boolean personalized) {}

    public record DataAvailability(
            boolean priceAvailable,
            boolean stockAvailable,
            boolean customerContextAvailable
    ) {}
}
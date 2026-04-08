package com.example.aggregator.model;

public record AggregatedProductData(
        ProductDetails product,
        String marketCode,
        PriceInfo price,       // null when pricing service unavailable
        StockInfo stock,       // null when availability service unavailable
        CustomerProfile customer // null when no customerId or customer service unavailable
) {}
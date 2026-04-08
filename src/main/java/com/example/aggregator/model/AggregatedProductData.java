package com.example.aggregator.model;

import java.util.Optional;

public record AggregatedProductData(
        ProductDetails product,
        String marketCode,
        Optional<PriceInfo> price,
        Optional<StockInfo> stock,
        Optional<CustomerProfile> customer
) {}

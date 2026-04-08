package com.example.aggregator.upstream.api;

import com.example.aggregator.model.Market;
import com.example.aggregator.model.ProductDetails;

/** Required upstream. Throws ProductNotFoundException or RuntimeException on failure. */
public interface CatalogClient {
    ProductDetails getProduct(String productId, Market market);
}

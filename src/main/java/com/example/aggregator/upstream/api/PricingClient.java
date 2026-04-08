package com.example.aggregator.upstream.api;

import com.example.aggregator.model.Market;
import com.example.aggregator.model.PriceInfo;

import java.util.Optional;

/** Optional upstream. Returns empty on failure — price shown as unavailable. */
public interface PricingClient {
    Optional<PriceInfo> getPrice(String productId, Market market, String customerId);
}

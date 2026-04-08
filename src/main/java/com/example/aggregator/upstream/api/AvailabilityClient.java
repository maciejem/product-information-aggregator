package com.example.aggregator.upstream.api;

import com.example.aggregator.model.Market;
import com.example.aggregator.model.StockInfo;

import java.util.Optional;

/** Optional upstream. Returns empty on failure — stock shown as unknown. */
public interface AvailabilityClient {
    Optional<StockInfo> getAvailability(String productId, Market market);
}

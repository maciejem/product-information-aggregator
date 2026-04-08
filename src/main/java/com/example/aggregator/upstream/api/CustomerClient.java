package com.example.aggregator.upstream.api;

import com.example.aggregator.model.CustomerProfile;

import java.util.Optional;

/** Optional upstream. Returns empty on failure or absent customerId — non-personalised response. */
public interface CustomerClient {
    Optional<CustomerProfile> getCustomer(String customerId);
}

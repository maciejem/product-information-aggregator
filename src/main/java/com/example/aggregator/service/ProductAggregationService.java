package com.example.aggregator.service;

import com.example.aggregator.config.AggregatorConfig;
import com.example.aggregator.exception.CatalogServiceException;
import com.example.aggregator.exception.ProductNotFoundException;
import com.example.aggregator.model.*;
import com.example.aggregator.upstream.api.AvailabilityClient;
import com.example.aggregator.upstream.api.CatalogClient;
import com.example.aggregator.upstream.api.CustomerClient;
import com.example.aggregator.upstream.api.PricingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Aggregates data from four upstream services into a single product response.
 *
 * All four calls start simultaneously at t=0 and run in parallel.
 * Catalog is joined first — if it failed we throw immediately without waiting
 * for optional services that would be useless anyway.
 * If catalog succeeded, the optional services have been running the whole time
 * and are typically already done by the time we collect them.
 *
 * Timeline (happy path):
 *   t=0ms   all four calls launched
 *   t=50ms  Catalog done → joined immediately, optional services still running
 *   t=60ms  Customer done
 *   t=80ms  Pricing done
 *   t=100ms Availability done → response assembled
 */
@Service
public class ProductAggregationService {

    private static final Logger log = LoggerFactory.getLogger(ProductAggregationService.class);

    private final CatalogClient      catalogClient;
    private final PricingClient      pricingClient;
    private final AvailabilityClient availabilityClient;
    private final CustomerClient     customerClient;
    private final Executor           executor;
    private final AggregatorConfig   config;

    public ProductAggregationService(
            CatalogClient catalogClient,
            PricingClient pricingClient,
            AvailabilityClient availabilityClient,
            CustomerClient customerClient,
            @Qualifier("upstreamCallExecutor") Executor executor,
            AggregatorConfig config
    ) {
        this.catalogClient      = catalogClient;
        this.pricingClient      = pricingClient;
        this.availabilityClient = availabilityClient;
        this.customerClient     = customerClient;
        this.executor           = executor;
        this.config             = config;
    }

    public AggregatedProductData aggregate(String productId, String marketCode, String customerId) {
        Market market = Market.of(marketCode);

        // All four start simultaneously
        CompletableFuture<ProductDetails> catalogFuture = CompletableFuture
                .supplyAsync(() -> catalogClient.getProduct(productId, market), executor)
                .orTimeout(config.timeouts().catalogMs(), TimeUnit.MILLISECONDS);

        CompletableFuture<PriceInfo> priceFuture = CompletableFuture
                .supplyAsync(() -> fetchPrice(productId, market, customerId), executor)
                .orTimeout(config.timeouts().pricingMs(), TimeUnit.MILLISECONDS)
                .exceptionally(t -> null);

        CompletableFuture<StockInfo> availabilityFuture = CompletableFuture
                .supplyAsync(() -> fetchAvailability(productId, market), executor)
                .orTimeout(config.timeouts().availabilityMs(), TimeUnit.MILLISECONDS)
                .exceptionally(t -> null);

        CompletableFuture<CustomerProfile> customerFuture = CompletableFuture
                .supplyAsync(() -> fetchCustomer(customerId), executor)
                .orTimeout(config.timeouts().customerMs(), TimeUnit.MILLISECONDS)
                .exceptionally(t -> null);

        // Fail fast — if catalog failed there is no point collecting optional results
        ProductDetails product = getCatalog(catalogFuture, productId);

        return new AggregatedProductData(
                product,
                marketCode,
                priceFuture.join(),
                availabilityFuture.join(),
                customerFuture.join()
        );
    }

    private ProductDetails getCatalog(CompletableFuture<ProductDetails> future, String productId) {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ProductNotFoundException pnfe) throw pnfe;
            throw new CatalogServiceException(productId, cause);
        }
    }

    // ── Optional — failure returns null, response degrades gracefully ──────────

    private PriceInfo fetchPrice(String productId, Market market, String customerId) {
        try {
            return pricingClient.getPrice(productId, market, customerId).orElse(null);
        } catch (Exception e) {
            log.warn("[PricingService] Unavailable: {}", e.getMessage());
            return null;
        }
    }

    private StockInfo fetchAvailability(String productId, Market market) {
        try {
            return availabilityClient.getAvailability(productId, market).orElse(null);
        } catch (Exception e) {
            log.warn("[AvailabilityService] Unavailable: {}", e.getMessage());
            return null;
        }
    }

    private CustomerProfile fetchCustomer(String customerId) {
        if (customerId == null) return null;
        try {
            return customerClient.getCustomer(customerId).orElse(null);
        } catch (Exception e) {
            log.warn("[CustomerService] Unavailable: {}", e.getMessage());
            return null;
        }
    }
}
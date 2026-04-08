package com.example.aggregator.controller;

import com.example.aggregator.model.AggregatedProductData;
import com.example.aggregator.service.ProductAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Single endpoint that aggregates product data into one response.
 *
 * GET /products/{productId}?market=nl-NL
 * GET /products/{productId}?market=de-DE&customerId=C001
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductAggregationService aggregationService;

    public ProductController(ProductAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable String productId,
            @RequestParam String market,
            @RequestParam(required = false) String customerId
    ) {
        log.info("Aggregating product={} market={} customer={}",
                productId, market, customerId != null ? customerId : "anonymous");

        long start = System.currentTimeMillis();

        AggregatedProductData data = aggregationService.aggregate(productId, market, customerId);
        ProductResponse response = buildResponse(data);

        log.info("Done product={} durationMs={} price={} stock={} customer={}",
                productId, System.currentTimeMillis() - start,
                response.dataAvailability().priceAvailable(),
                response.dataAvailability().stockAvailable(),
                response.dataAvailability().customerContextAvailable());

        return ResponseEntity.ok(response);
    }

    private ProductResponse buildResponse(AggregatedProductData data) {
        return new ProductResponse(
                data.product().id(),
                data.product().name(),
                data.product().description(),
                data.product().imageUrls(),
                data.product().specs(),
                data.marketCode(),
                data.price() != null ? new ProductResponse.PriceResponse(
                        data.price().basePrice(), data.price().discountRate(),
                        data.price().finalPrice(), data.price().currency()) : null,
                data.stock() != null ? new ProductResponse.StockResponse(
                        data.stock().inStock(), data.stock().quantity(),
                        data.stock().warehouseId(), data.stock().estimatedDelivery()) : null,
                data.customer() != null ? new ProductResponse.CustomerContext(data.customer().segment(), true) : null,
                new ProductResponse.DataAvailability(
                        data.price() != null,
                        data.stock() != null,
                        data.customer() != null
                )
        );
    }
}
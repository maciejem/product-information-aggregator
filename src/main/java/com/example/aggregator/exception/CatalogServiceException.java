package com.example.aggregator.exception;

public class CatalogServiceException extends RuntimeException {
    public CatalogServiceException(String productId, Throwable cause) {
        super("Catalog service failed for product: " + productId, cause);
    }
}

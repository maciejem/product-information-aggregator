package com.example.aggregator.model;

public record StockInfo(boolean inStock, int quantity, String warehouseId, String estimatedDelivery) {}

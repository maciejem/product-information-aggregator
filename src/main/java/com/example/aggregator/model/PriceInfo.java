package com.example.aggregator.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PriceInfo(BigDecimal basePrice, BigDecimal discountRate, BigDecimal finalPrice, String currency) {

    public static PriceInfo of(BigDecimal basePrice, BigDecimal discountRate, String currency) {
        BigDecimal finalPrice = basePrice.subtract(basePrice.multiply(discountRate))
                .setScale(2, RoundingMode.HALF_UP);
        return new PriceInfo(basePrice, discountRate, finalPrice, currency);
    }
}

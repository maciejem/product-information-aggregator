package com.example.aggregator.model;

import java.util.List;
import java.util.Map;

public record ProductDetails(
        String id,
        String name,
        String description,
        List<String> imageUrls,
        Map<String, String> specs
) {}

package com.example.aggregator;

import com.example.aggregator.controller.ProductController;
import com.example.aggregator.exception.CatalogServiceException;
import com.example.aggregator.exception.InvalidMarketException;
import com.example.aggregator.exception.ProductNotFoundException;
import com.example.aggregator.model.*;
import com.example.aggregator.service.ProductAggregationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductAggregationService aggregationService;

    @Test
    void getProduct_returnsOk() throws Exception {
        when(aggregationService.aggregate(eq("BRAKE-PAD-001"), eq("nl-NL"), eq(null)))
                .thenReturn(sampleData());

        mockMvc.perform(get("/products/BRAKE-PAD-001").param("market", "nl-NL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("BRAKE-PAD-001"))
                .andExpect(jsonPath("$.marketCode").value("nl-NL"))
                .andExpect(jsonPath("$.dataAvailability.priceAvailable").value(true));
    }

    @Test
    void getProduct_withCustomerId_passesItToService() throws Exception {
        when(aggregationService.aggregate(eq("BRAKE-PAD-001"), eq("de-DE"), eq("C001")))
                .thenReturn(sampleData());

        mockMvc.perform(get("/products/BRAKE-PAD-001")
                        .param("market", "de-DE")
                        .param("customerId", "C001"))
                .andExpect(status().isOk());
    }

    @Test
    void getProduct_invalidMarket_returns400() throws Exception {
        when(aggregationService.aggregate(any(), any(), any()))
                .thenThrow(new InvalidMarketException("invalid"));

        mockMvc.perform(get("/products/BRAKE-PAD-001").param("market", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Market Code"));
    }

    @Test
    void getProduct_unknownProduct_returns404() throws Exception {
        when(aggregationService.aggregate(any(), any(), any()))
                .thenThrow(new ProductNotFoundException("UNKNOWN-001"));

        mockMvc.perform(get("/products/UNKNOWN-001").param("market", "nl-NL"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product Not Found"));
    }

    @Test
    void getProduct_catalogDown_returns503() throws Exception {
        when(aggregationService.aggregate(any(), any(), any()))
                .thenThrow(new CatalogServiceException("BRAKE-PAD-001", new RuntimeException("timeout")));

        mockMvc.perform(get("/products/BRAKE-PAD-001").param("market", "nl-NL"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Service Unavailable"));
    }

    private AggregatedProductData sampleData() {
        return new AggregatedProductData(
                new ProductDetails(
                        "BRAKE-PAD-001",
                        "Remblok Premium",
                        "Hoogwaardige remblok geschikt voor zware toepassingen.",
                        List.of("https://cdn.example.com/products/BRAKE-PAD-001/main.jpg"),
                        Map.of("material", "semi-metallic", "thickness", "12mm")
                ),
                "nl-NL",
                Optional.of(PriceInfo.of(new BigDecimal("49.95"), BigDecimal.ZERO, "EUR")),
                Optional.of(new StockInfo(true, 12, "WH-AMS-01", "1-2 business days")),
                Optional.empty()
        );
    }
}

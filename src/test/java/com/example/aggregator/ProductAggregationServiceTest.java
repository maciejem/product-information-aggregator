package com.example.aggregator;

import com.example.aggregator.exception.ProductNotFoundException;
import com.example.aggregator.model.AggregatedProductData;
import com.example.aggregator.service.ProductAggregationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "aggregator.mocks.catalog.reliability=1.0",
        "aggregator.mocks.pricing.reliability=1.0",
        "aggregator.mocks.availability.reliability=1.0",
        "aggregator.mocks.customer.reliability=1.0",
        "aggregator.mocks.catalog.latency-ms=0",
        "aggregator.mocks.pricing.latency-ms=0",
        "aggregator.mocks.availability.latency-ms=0",
        "aggregator.mocks.customer.latency-ms=0"
})
class ProductAggregationServiceTest {

    @Autowired
    private ProductAggregationService aggregationService;

    // ── Localization ──────────────────────────────────────────────────────────

    @Test
    void aggregate_nlMarket_returnsProductWithDutchName() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "nl-NL", null);

        assertThat(data.product().id()).isEqualTo("BRAKE-PAD-001");
        assertThat(data.product().name()).isEqualTo("Remblok Premium");
        assertThat(data.marketCode()).isEqualTo("nl-NL");
    }

    @Test
    void aggregate_deMarket_returnsProductWithGermanName() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "de-DE", null);

        assertThat(data.product().name()).isEqualTo("Bremsbelag Premium");
    }

    @Test
    void aggregate_plMarket_returnsProductWithPolishName() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "pl-PL", null);

        assertThat(data.product().name()).isEqualTo("Klocek hamulcowy Premium");
    }

    // ── Pricing / currency ────────────────────────────────────────────────────

    @Test
    void aggregate_nlMarket_priceInEUR() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "nl-NL", null);

        assertThat(data.price()).isNotNull();
        assertThat(data.price().currency()).isEqualTo("EUR");
    }

    @Test
    void aggregate_plMarket_priceInPLN() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "pl-PL", null);

        assertThat(data.price()).isNotNull();
        assertThat(data.price().currency()).isEqualTo("PLN");
    }

    @Test
    void aggregate_gbMarket_priceInGBP() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "en-GB", null);

        assertThat(data.price()).isNotNull();
        assertThat(data.price().currency()).isEqualTo("GBP");
    }

    @Test
    void aggregate_seMarket_priceInSEK() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "sv-SE", null);

        assertThat(data.price()).isNotNull();
        assertThat(data.price().currency()).isEqualTo("SEK");
    }

    // ── Customer discounts ────────────────────────────────────────────────────

    @Test
    void aggregate_dealerCustomer_returns15PercentDiscount() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "nl-NL", "C001");

        assertThat(data.customer()).isNotNull();
        assertThat(data.customer().segment()).isEqualTo("DEALER");

        assertThat(data.price()).isNotNull();
        assertThat(data.price().discountRate()).isEqualByComparingTo(new BigDecimal("0.15"));
        assertThat(data.price().finalPrice()).isLessThan(data.price().basePrice());
    }

    @Test
    void aggregate_workshopCustomer_returns10PercentDiscount() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "nl-NL", "C002");

        assertThat(data.customer()).isNotNull();
        assertThat(data.customer().segment()).isEqualTo("WORKSHOP");

        assertThat(data.price()).isNotNull();
        assertThat(data.price().discountRate()).isEqualByComparingTo(new BigDecimal("0.10"));
        assertThat(data.price().finalPrice()).isLessThan(data.price().basePrice());
    }

    @Test
    void aggregate_standardCustomer_returnsNoDiscount() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "nl-NL", "C003");

        assertThat(data.customer()).isNotNull();
        assertThat(data.customer().segment()).isEqualTo("STANDARD");

        assertThat(data.price()).isNotNull();
        assertThat(data.price().discountRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(data.price().finalPrice()).isEqualByComparingTo(data.price().basePrice());
    }

    // ── Customer context ──────────────────────────────────────────────────────

    @Test
    void aggregate_anonymousUser_customerContextAbsent() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "nl-NL", null);

        assertThat(data.customer()).isNull();
    }

    @Test
    void aggregate_unknownCustomerId_customerContextAbsent() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "de-DE", "C999");

        assertThat(data.customer()).isNull();
    }

    // ── Warehouse routing ─────────────────────────────────────────────────────

    @Test
    void aggregate_nlMarket_stockRoutedToAmsterdamWarehouse() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "nl-NL", null);

        assertThat(data.stock()).isNotNull();
        assertThat(data.stock().warehouseId()).isEqualTo("WH-AMS-01");
        assertThat(data.stock().estimatedDelivery()).isEqualTo("1-2 business days");
    }

    @Test
    void aggregate_deMarket_stockRoutedToFrankfurtWarehouse() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "de-DE", null);

        assertThat(data.stock()).isNotNull();
        assertThat(data.stock().warehouseId()).isEqualTo("WH-FRA-01");
        assertThat(data.stock().estimatedDelivery()).isEqualTo("1-2 business days");
    }

    @Test
    void aggregate_plMarket_stockRoutedToWarsawWarehouse() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "pl-PL", null);

        assertThat(data.stock()).isNotNull();
        assertThat(data.stock().warehouseId()).isEqualTo("WH-WAW-01");
        assertThat(data.stock().estimatedDelivery()).isEqualTo("2-3 business days");
    }

    @Test
    void aggregate_gbMarket_stockRoutedToLondonWarehouse() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "en-GB", null);

        assertThat(data.stock()).isNotNull();
        assertThat(data.stock().warehouseId()).isEqualTo("WH-LON-01");
        assertThat(data.stock().estimatedDelivery()).isEqualTo("2-3 business days");
    }

    // ── Optional service availability ─────────────────────────────────────────

    @Test
    void aggregate_noCustomerId_priceAndStockPresentCustomerAbsent() {
        AggregatedProductData data = aggregationService.aggregate("FILTER-OIL-010", "de-DE", null);

        assertThat(data.price()).isNotNull();
        assertThat(data.stock()).isNotNull();
        assertThat(data.customer()).isNull();
    }

    // ── Error cases ───────────────────────────────────────────────────────────

    @Test
    void aggregate_unknownProduct_throwsProductNotFoundException() {
        assertThatThrownBy(() ->
                aggregationService.aggregate("DOES-NOT-EXIST", "nl-NL", null)
        ).isInstanceOf(ProductNotFoundException.class);
    }
}
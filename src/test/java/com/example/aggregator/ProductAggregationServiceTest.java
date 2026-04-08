package com.example.aggregator;

import com.example.aggregator.exception.ProductNotFoundException;
import com.example.aggregator.model.AggregatedProductData;
import com.example.aggregator.service.ProductAggregationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProductAggregationServiceTest {

    @Autowired
    private ProductAggregationService aggregationService;

    @Test
    void anonymousRequest_returnsProductWithMarketData() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "nl-NL", null);

        assertThat(data.product().id()).isEqualTo("BRAKE-PAD-001");
        assertThat(data.marketCode()).isEqualTo("nl-NL");
        assertThat(data.product().name()).isEqualTo("Remblok Premium");
        assertThat(data.customer()).isEmpty();
    }

    @Test
    void dealerRequest_returnsPriceWithDiscount() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "nl-NL", "C001");

        assertThat(data.customer()).isPresent();
        assertThat(data.customer().get().segment()).isEqualTo("DEALER");

        if (data.price().isPresent()) {
            assertThat(data.price().get().discountRate()).isPositive();
            assertThat(data.price().get().finalPrice()).isLessThan(data.price().get().basePrice());
            assertThat(data.price().get().currency()).isEqualTo("EUR");
        }
    }

    @Test
    void polishMarket_returnsPLNCurrency() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "pl-PL", null);

        assertThat(data.product().name()).isEqualTo("Klocek hamulcowy Premium");
        if (data.price().isPresent()) {
            assertThat(data.price().get().currency()).isEqualTo("PLN");
        }
    }

    @Test
    void unknownProduct_throwsProductNotFoundException() {
        assertThatThrownBy(() ->
                aggregationService.aggregate("DOES-NOT-EXIST", "nl-NL", null)
        ).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void dataAvailability_alwaysPresent() {
        AggregatedProductData data = aggregationService.aggregate("FILTER-OIL-010", "de-DE", null);

        assertThat(data.price()).isNotNull();
        assertThat(data.stock()).isNotNull();
        assertThat(data.customer()).isNotNull();
    }

    @Test
    void unknownCustomerId_returnsNonPersonalisedResponse() {
        AggregatedProductData data = aggregationService.aggregate("BRAKE-PAD-001", "de-DE", "C999");

        assertThat(data.customer()).isEmpty();
    }
}

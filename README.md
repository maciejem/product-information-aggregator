# Product Information Aggregator

A Spring Boot service that combines data from multiple upstream services into a single market-aware product response.

## How to run

**Prerequisites:** Java 21, Maven 3.8+

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

### Example requests

```bash
# Anonymous request
curl "http://localhost:8080/products/BRAKE-PAD-001?market=nl-NL"

# Authenticated customer — personalised pricing
curl "http://localhost:8080/products/BRAKE-PAD-001?market=de-DE&customerId=C001"

# Polish market
curl "http://localhost:8080/products/FILTER-OIL-010?market=pl-PL&customerId=C004"
```

### Available product IDs
`BRAKE-PAD-001`, `BRAKE-PAD-002`, `FILTER-OIL-010`, `FILTER-AIR-011`, `BEARING-FRONT-100`

### Available customer IDs
`C001` (DEALER, 15% discount), `C002` (WORKSHOP, 10%), `C003` (STANDARD), `C004` (DEALER, 15%), `C005` (WORKSHOP, 10%)

### Supported markets
`nl-NL`, `de-DE`, `pl-PL`, `gb-GB`, `se-SE`, `be-BE`, `fr-FR`

---

## Key design decisions

### Catalog is fetched first, optional services run in parallel

Catalog is a required service — without a product there is nothing to return. It is fetched synchronously before the optional services start. This is a deliberate fail-fast decision:

- If the product does not exist, there is no point fetching price, stock, or customer data for it
- If the catalog is down, the same applies
- Exceptions propagate directly with no wrapping — clean and simple

Once we have the product, Pricing, Availability, and Customer all start simultaneously:

```
t=0ms   Catalog starts (~50ms)
t=50ms  Catalog done → Pricing (~80ms), Availability (~100ms), Customer (~60ms) start
t=60ms  Customer done
t=80ms  Pricing done
t=100ms Availability done → response assembled (~150ms total)
```

### Required vs optional

Only Catalog is required. The three optional services degrade gracefully — any failure (exception or timeout) returns empty, recorded in `dataAvailability` so the frontend can show targeted fallback messages per section rather than a generic error.

| Service      | Required | On failure              | Timeout |
|--------------|----------|-------------------------|---------|
| Catalog      | Yes      | 503 / 404               | 200ms   |
| Pricing      | No       | Price unavailable        | 300ms   |
| Availability | No       | Stock unknown            | 350ms   |
| Customer     | No       | Non-personalised response| 200ms   |

### Upstream services are interfaces

`CatalogClient`, `PricingClient`, `AvailabilityClient`, and `CustomerClient` are interfaces in `upstream/api/`. The aggregator depends only on these abstractions. Replacing a mock with a real HTTP client means implementing the interface — no changes to the aggregator or any other class.

### Response structure

The service returns two layers:

- `AggregatedProductData` — internal DTO returned by the service layer, carries raw Optional fields
- `ProductResponse` — the HTTP response shape, built by the controller from the internal DTO

This separation means the service layer is testable without knowing about JSON serialisation, and the controller owns the mapping concern.

### Virtual threads

The executor uses Java 21 virtual threads (`Executors.newVirtualThreadPerTaskExecutor()`). Virtual threads park cheaply during blocking I/O, making them ideal for this pattern where each request spawns multiple concurrent upstream calls. This gives the same scalability as reactive programming with simpler, more readable code.

### Realistic mock behaviour

Each mock simulates:
- **Latency** with ±20% random jitter so responses are not artificially uniform
- **Reliability** via random failure injection based on the spec (e.g. Availability fails ~1 in 50 calls)

Occasional failures and partial responses are expected and intentional — they demonstrate the degradation behaviour working correctly.

---

## What I would do differently with more time

**Add a circuit breaker.** With Resilience4j `@CircuitBreaker`, repeated failures open the circuit and return the fallback immediately without waiting for each timeout. Currently if Availability is completely down, every request waits 350ms before falling back.

**Cache catalog responses.** Product content changes rarely. A short-lived cache (e.g. 5 minutes) would significantly reduce load on the catalog service for repeated product views.

---

## Design question — Option A: Adding a Related Products service

> *The Assortment team wants to add a 'Related Products' service (200ms latency, 90% reliability). How would your design accommodate this? Should it be required or optional?*

**It should be optional.** A customer arriving at a product page needs the product itself, its price, and stock status. Related products are an enhancement — their absence should never prevent the page from loading. 90% reliability is also significantly lower than the other services, so treating it as required would meaningfully degrade overall availability.

**Accommodating it requires four steps:**

1. Add a `RelatedProductsClient` interface in `upstream/api/`
2. Add a `MockRelatedProductsClient` in `upstream/mock/`
3. Add one `CompletableFuture` in `ProductAggregationService.aggregate()`:

```java
CompletableFuture<Optional<List<String>>> relatedFuture = CompletableFuture
        .supplyAsync(() -> fetchRelatedProducts(productId, market), executor)
        .orTimeout(300, TimeUnit.MILLISECONDS)
        .exceptionally(t -> Optional.empty());
```

4. Add a `relatedProducts` field to `AggregatedProductData` and `ProductResponse`
5. Cache related products responses.

Steps 1 and 2 require no changes to existing classes. The aggregator change is additive — one new future, joined alongside the existing three. The pattern is identical to how Pricing, Availability, and Customer are handled.

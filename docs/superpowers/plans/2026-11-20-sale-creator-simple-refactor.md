# SaleCreatorService Simple Modularization Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract cohesive responsibilities from `SaleCreatorService` (100 lines, 16 deps) into focused `@Service` classes, each with a single purpose. The orchestrator stays but becomes thin (~30 lines) delegating to injected collaborators.

**Architecture:** Keep `SaleCreatorService` as the orchestrator. Extract 5-6 domain-specific services:
- `SaleValidationService` — payment method + detail validation (stock, prices, offers)
- `SalePricingService` — offer discount, total, status, partial payment validation
- `SaleEntityBuilderService` — builds Sale entity with refs (user, commerce, subtotal)
- `SalePersistenceService` — saves sale + details, returns saved sale + itemCount
- `SaleTransactionService` — calculates revenue/outflow, creates transaction if needed
- `SaleGiftService` — gift handling (already exists as `SaleGiftHandlerService`, keep)

**Tech Stack:** Spring Boot 3, Java 25, Lombok, Spring Transactional, existing mappers/validators.

**Spec:** Current `SaleCreatorService.java` (lines 50-118) is the spec. No external spec.

## Global Constraints

- All services use `org.springframework.transaction.annotation.Transactional` (not Jakarta).
- Constructor injection via `@AllArgsConstructor` (Lombok).
- Business errors throw `BusinessException` with `ErrorCode`.
- Cross-tenant scoping via `*FinderByIdAndCommerceIdService` returning 404.
- `EntityManager.getReference()` for wiring relations by ID.
- No field injection. One class per use case (`*Service`).
- DTOs are records in `dto/request` and `dto/response`.
- Existing tests must pass; `@SpringBootTest contextLoads` requires MySQL.

## Review Focus

1. **Partial payment > total** — reject with `INVALID_PARAMETER` *after* total calculated.
2. **Gift sale** — status = GIFTED, total = 0, no discount, no stock decrement, no transaction.
3. **Stock decrement atomicity** — only runs if *all* validations pass.
4. **Transaction creation** — only when revenue/outflow > 0.
5. **Offer discount** — only from active payment-method offer for the commerce.

---

### Task 1: Create `SaleValidationService`

**Files:**
- Create: `src/main/java/com/gestion/service/sale/SaleValidationService.java`

**Interfaces:**
- Consumes: `SalePaymentMethodValidatorService`, `SaleDetailMergerService`, `SaleDetailValidatorService`
- Produces: `SaleValidationService` with method `validateAndMerge(SaleRequest, Long commerceId) -> SaleValidationResult`

- [ ] **Step 1.1: Write `SaleValidationResult` record**

```java
public record SaleValidationResult(
    Map<Long, Integer> quantitiesByProductId,
    List<SaleDetail> details,
    BigDecimal subtotal
) {}
```

- [ ] **Step 1.2: Write `SaleValidationService`**

```java
@Service
@AllArgsConstructor
public class SaleValidationService {
    private final SalePaymentMethodValidatorService paymentMethodValidator;
    private final SaleDetailMergerService merger;
    private final SaleDetailValidatorService detailValidator;

    public SaleValidationResult validateAndMerge(SaleRequest request, Long commerceId) {
        paymentMethodValidator.validate(request);
        Map<Long, Integer> quantities = merger.mergeDetails(request.details());
        SaleDetailValidationResult result = detailValidator.validate(commerceId, quantities);
        return new SaleValidationResult(quantities, result.details(), result.subtotal());
    }
}
```

- [ ] **Step 1.3: Compile verification**

Run: `.\mvnw.cmd -q -DskipTests compile`
Expected: SUCCESS

---

### Task 2: Create `SalePricingService`

**Files:**
- Create: `src/main/java/com/gestion/service/sale/SalePricingService.java`

**Interfaces:**
- Consumes: `OfferFinderByPaymentMethodAndCommerceIdService`, `SaleDiscountApplierService`, `SaleStatusResolverService`, `SalePartialPaymentValidatorService`
- Produces: `SalePricingService` with method `applyPricing(Sale, SaleRequest, Long commerceId, BigDecimal subtotal) -> SalePricingResult`

- [ ] **Step 2.1: Write `SalePricingResult` record**

```java
public record SalePricingResult(
    BigDecimal total,
    SaleStatus status
) {}
```

- [ ] **Step 2.2: Write `SalePricingService`**

```java
@Service
@AllArgsConstructor
public class SalePricingService {
    private final OfferFinderByPaymentMethodAndCommerceIdService offerFinder;
    private final SaleDiscountApplierService discountApplier;
    private final SaleStatusResolverService statusResolver;
    private final SalePartialPaymentValidatorService partialPaymentValidator;

    public SalePricingResult applyPricing(Sale sale, SaleRequest request, Long commerceId, BigDecimal subtotal) {
        if (sale.getStatus() == SaleStatus.GIFTED) {
            return new SalePricingResult(BigDecimal.ZERO, SaleStatus.GIFTED);
        }

        offerFinder.findActiveByPaymentMethodAndCommerceId(sale.getPaymentMethod(), commerceId)
                .ifPresent(offer -> {
                    sale.setPaymentMethodDiscountType(offer.getDiscountType());
                    sale.setPaymentMethodDiscount(offer.getValue());
                });

        BigDecimal total = discountApplier.applyDiscount(subtotal, sale.getPaymentMethodDiscountType(), sale.getPaymentMethodDiscount());
        SaleStatus status = statusResolver.resolveStatus(request);
        partialPaymentValidator.validate(request, total);

        return new SalePricingResult(total, status);
    }
}
```

- [ ] **Step 2.3: Compile verification**

---

### Task 3: Create `SaleEntityBuilderService`

**Files:**
- Create: `src/main/java/com/gestion/service/sale/SaleEntityBuilderService.java`

**Interfaces:**
- Consumes: `EntityManager`, `SaleMapper`
- Produces: `SaleEntityBuilderService` with method `build(SaleRequest, Long userId, Long commerceId, BigDecimal subtotal) -> Sale`

- [ ] **Step 3.1: Write `SaleEntityBuilderService`**

```java
@Service
@AllArgsConstructor
public class SaleEntityBuilderService {
    private final EntityManager entityManager;

    public Sale build(SaleRequest request, Long userId, Long commerceId, BigDecimal subtotal) {
        Sale sale = SaleMapper.toEntity(request);
        sale.setUser(entityManager.getReference(User.class, userId));
        sale.setCommerce(entityManager.getReference(Commerce.class, commerceId));
        sale.setSubtotal(subtotal);
        return sale;
    }
}
```

- [ ] **Step 3.2: Compile verification**

---

### Task 4: Create `SalePersistenceService`

**Files:**
- Create: `src/main/java/com/gestion/service/sale/SalePersistenceService.java`

**Interfaces:**
- Consumes: `JpaSaleRepository`, `SaleDetailCreatorService`
- Produces: `SalePersistenceService` with method `persist(Sale, List<SaleDetail>) -> SalePersistenceResult`

- [ ] **Step 4.1: Write `SalePersistenceResult` record**

```java
public record SalePersistenceResult(
    Sale savedSale,
    int itemCount
) {}
```

- [ ] **Step 4.2: Write `SalePersistenceService`**

```java
@Service
@AllArgsConstructor
public class SalePersistenceService {
    private final JpaSaleRepository saleRepository;
    private final SaleDetailCreatorService detailCreator;

    @Transactional
    public SalePersistenceResult persist(Sale sale, List<SaleDetail> details) {
        Sale saved = saleRepository.save(sale);
        detailCreator.createDetails(saved, details);
        int itemCount = details.stream().mapToInt(SaleDetail::getQuantity).sum();
        return new SalePersistenceResult(saved, itemCount);
    }
}
```

- [ ] **Step 4.3: Compile verification**

---

### Task 5: Create `SaleTransactionService`

**Files:**
- Create: `src/main/java/com/gestion/service/sale/SaleTransactionService.java`

**Interfaces:**
- Consumes: `SaleRevenueOutflowCalculatorService`, `TransactionCreatorService`
- Produces: `SaleTransactionService` with method `createIfNeeded(SaleRequest, Sale, Long commerceId) -> void`

- [ ] **Step 5.1: Write `SaleTransactionService`**

```java
@Service
@AllArgsConstructor
public class SaleTransactionService {
    private final SaleRevenueOutflowCalculatorService calculator;
    private final TransactionCreatorService transactionCreator;

    public void createIfNeeded(SaleRequest request, Sale savedSale, Long commerceId) {
        SaleRevenueOutflowCalculatorService.Result result = calculator.calculate(request, savedSale);
        if (result.revenue().compareTo(BigDecimal.ZERO) > 0 || result.outflow().compareTo(BigDecimal.ZERO) > 0) {
            TransactionRequest txRequest = new TransactionRequest(result.revenue(), result.outflow(), savedSale.getId());
            transactionCreator.createTransaction(txRequest, commerceId);
        }
    }
}
```

- [ ] **Step 5.2: Compile verification**

---

### Task 6: Refactor `SaleCreatorService` to use new services

**Files:**
- Modify: `src/main/java/com/gestion/service/sale/SaleCreatorService.java` (replace 16 deps with 6 new services + existing `SaleGiftHandlerService`, `SaleSellPointResolverService`, `SaleClientResolverService`, `SaleStockUpdaterService`, `CommerceAvailabilityValidatorService`)

**Interfaces:**
- Consumes: `SaleValidationService`, `SaleEntityBuilderService`, `SalePricingService`, `SalePersistenceService`, `SaleTransactionService`, `SaleGiftHandlerService`, `SaleSellPointResolverService`, `SaleClientResolverService`, `SaleStockUpdaterService`, `CommerceAvailabilityValidatorService`
- Produces: Thin orchestrator (~30 lines)

- [ ] **Step 6.1: Rewrite `SaleCreatorService`**

```java
@Service
@AllArgsConstructor
public class SaleCreatorService {
    private final CommerceAvailabilityValidatorService commerceAvailabilityValidator;
    private final SaleValidationService validationService;
    private final SaleEntityBuilderService entityBuilder;
    private final SaleGiftHandlerService giftHandler;
    private final SalePricingService pricingService;
    private final SaleSellPointResolverService sellPointResolver;
    private final SaleClientResolverService clientResolver;
    private final SaleStockUpdaterService stockUpdater;
    private final SalePersistenceService persistenceService;
    private final SaleTransactionService transactionService;

    @Transactional
    public SaleSummaryResponse createSale(SaleRequest request, Long userId, Long commerceId, Long sellPointId, Role role) {
        commerceAvailabilityValidator.checkCommerceIsAvailable(commerceId);

        // 1. Validate payment method + merge details + validate stock/prices
        SaleValidationResult validation = validationService.validateAndMerge(request, commerceId);

        // 2. Build sale entity
        Sale sale = entityBuilder.build(request, userId, commerceId, validation.subtotal());

        // 3. Handle gift (sets status/total if gift)
        giftHandler.handleIfGift(request, sale, role);

        // 4. Apply pricing (discount, total, status, partial payment validation)
        SalePricingResult pricing = pricingService.applyPricing(sale, request, commerceId, validation.subtotal());
        sale.setTotal(pricing.total());
        sale.setStatus(pricing.status());

        // 5. Resolve sell point & client
        sale.setSellPoint(sellPointResolver.resolveSellPoint(sellPointId));
        sale.setClient(clientResolver.resolveClient(request.clientId(), commerceId));

        // 6. Decrement stock (skips if GIFTED)
        if (sale.getStatus() != SaleStatus.GIFTED) {
            stockUpdater.applyStockDecrement(validation.details());
        }

        // 7. Persist sale + details
        SalePersistenceResult persistence = persistenceService.persist(sale, validation.details());

        // 8. Create transaction if revenue/outflow > 0
        transactionService.createIfNeeded(request, persistence.savedSale(), commerceId);

        // 9. Build response
        return SaleMapper.toSummaryResponse(persistence.savedSale(), persistence.itemCount());
    }
}
```

- [ ] **Step 6.2: Compile verification**

Run: `.\mvnw.cmd -q -DskipTests compile`
Expected: SUCCESS

---

### Task 7: Run tests

- [ ] **Step 7.1: Run full test suite**

Run: `.\mvnw.cmd test`
Expected: All tests PASS

---

### Task 8: Verify no behavioral changes

- [ ] **Step 8.1: Manual checklist** (Review Focus items)
  1. Partial payment > total → 400 INVALID_PARAMETER
  2. Gift sale → status GIFTED, total 0, no stock decrement, no transaction
  3. Stock decrement only after all validations
  4. Transaction only when revenue/outflow > 0
  5. Offer discount only from active payment-method offer

---

## Self-Review

- **Spec coverage:** Every line of original `createSale` mapped to a service method. No gaps.
- **Step scan:** Each task creates one service (20-40 lines) or modifies one file. No task does two things.
- **Type consistency:** Result records match original local variables. Service signatures clear.
- **Review Focus:** All 5 critical behaviors preserved in Tasks 1, 2, 4, 5, 6.
- **Proportion:** Plan ~250 lines for ~100 lines original — reasonable for 6 new focused services.

---

**Plan complete and saved to `docs/superpowers/plans/2026-11-20-sale-creator-simple-refactor.md`. Please review the plan. Does it capture what you want?**
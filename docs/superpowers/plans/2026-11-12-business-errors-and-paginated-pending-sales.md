# P2 Closure: Business Errors and Paginated Pending Sales Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the internal Spring `AccessDeniedException` leak with a proper business error code, and paginate the pending-sales endpoint while removing its N+1 queries.

**Architecture:** Two independent changes that both touch the public API contract, delivered in one wave so the front adapts once. P2-15 routes the "authenticated user has no commerce" case through a dedicated `BusinessException` with its own `ErrorCode` and a shared validator service, so the client never receives an internal message. P2-17 replaces the unpaged list with a `Page`, fetching the sales of a page in one query, their details in one batched query, and the account totals in one aggregate query.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring Data JPA / Hibernate 7, MySQL 8, Lombok, Jackson 3

**Spec:** `improvements.md` — rows **P2-15** and **P2-17** of the "P2 — Conviene arreglarlo" table. The current API contract that these two change is documented in `README.md` (sección "Códigos de error" and sección "Clientes y cuentas corrientes"). The plan argues from that spec, so the spec travels with it; executors read both.

## Global Constraints

- Build with the wrapper only: `.\mvnw.cmd <goal>` (Windows). Java 25 required. There is no global Maven/Gradle.
- Typecheck is compile: `.\mvnw.cmd -q -DskipTests compile`. Tests: `.\mvnw.cmd test`. Verify in that order — a compile failure blocks everything after it.
- **This project has no automated test suite.** The suite was deleted on 2026-11-12 by explicit team decision; `src/test/` contains only `ProyectoFinalApplicationTests` (a `@SpringBootTest` `contextLoads` smoke test that boots the full Spring context). Every "test" step in this plan is therefore a **scripted check against a running instance** (MySQL on `localhost:3306`, db `gestion`, user `root`/`admin`; app on `localhost:8080`; MinIO only matters for uploads).
- **No commits without the user's explicit instruction** (`AGENTS.md`). The commit steps below are written but must not be executed on their own.
- Layer: `controller/<resource>/` → `service/<resource>/` → `repository/Jpa*Repository` → `model`. Mappers in `mappers/` are static-method classes, not beans.
- One class per endpoint, named `Resource` + HTTP verb + `Controller`. One class per service use case, named `Entity` + action + `Service`.
- DTOs are records. Requests live in `dto/request`, responses in `dto/response`, accessed as `request.field()` (no getters).
- Injection via constructor with Lombok `@AllArgsConstructor`. Never field injection.
- Business errors throw an exception extending `exception/BusinessException` carrying an `ErrorCode` from `enums/ErrorCode` (key + HTTP status + default message). The single `BusinessException` handler in `exception/handler/GlobalExceptionHandler` maps them; any other exception surfaces as `code=INTERNAL_ERROR` (500).
- `@EntityGraph(attributePaths = ...)` on a repository method whose result feeds a response mapping several relations — otherwise the lazy proxies trigger one query per relation per row.
- Roles are `SUDO`, `OWNER`, `CASHIER`. The JWT travels in the **`token` cookie**, not in an `Authorization` header.
- Entity-by-id lookups that serve an endpoint go through an `XFinderByIdAndCommerceIdService` and answer **404** (never 403) when the row belongs to another commerce.
- Comments and docs are written in Spanish. Commit messages are in English, conventional commits with a scope.
- `spring.jpa.show-sql=true` is active: every statement is logged, which is what the query-count checks below rely on.

## Review Focus

The five failure modes most likely to bite someone using this API. Each one is pinned by a check in the task that owns the code.

1. **An authenticated user whose JWT carries no `commerceId` reaches a commerce-scoped write endpoint** — expected: `403` with the business code `COMMERCE_NOT_AVAILABLE` and the curated message "The authenticated user has no commerce associated". Today it returns `403 ACCESS_DENIED` with the raw internal text `"Commerce ID is null"`.
2. **A role denial (CASHIER calling an OWNER-only endpoint)** — expected: `403` with the code `ACCESS_DENIED` and the curated message "Access denied", never a message authored by Spring. A careless fix that changes the `AccessDeniedException` handler could silently turn every role denial into a 500.
3. **A client whose pending sales do not fit in one page** — expected: `pendingSalesCount` and `totalPending` describe **all** of that client's pending sales, while `pendingSales.content` holds only the requested page. A paginated list that also paginates the totals is the easy mistake here.
4. **A client with no pending sales, or a `clientId` that does not exist** — expected: `pendingSalesCount: 0`, `totalPending: 0` and an empty page — **not** `404`. The README documents this explicitly and the front relies on it.
5. **A sale whose `client` is null ("Consumidor final")** — expected: it belongs to no client, so it must not appear in this endpoint nor alter the client's count. The aggregate query groups by `s.client.id`, so a null client must not leak into the totals.

---

## File Structure

### Task 1 — Business error for "the authenticated user has no commerce"

| File | Responsibility |
|---|---|
| `src/main/java/com/gestion/enums/ErrorCode.java` (modify) | Adds the `COMMERCE_NOT_AVAILABLE` code. Key + HTTP status + default message, nothing else. |
| `src/main/java/com/gestion/exception/CommerceNotAvailableException.java` (create) | The `BusinessException` subclass for this case. Follows the sibling shape `(ErrorCode code, String message)`. |
| `src/main/java/com/gestion/service/commerce/CommerceAvailabilityValidatorService.java` (create) | The single place that raises it. Six services call one method instead of six copies of a guard. |
| `src/main/java/com/gestion/exception/handler/GlobalExceptionHandler.java` (modify) | Stops forwarding Spring's raw message to the client. |
| `src/main/java/com/gestion/service/{sellpoint/SellPointCreatorService, client/ClientCreatorService, category/CategoryCreatorService, sale/SaleCreatorService, cashier/CashierCreatorService, cashier/CashierUpdaterService}.java` (modify) | Each swaps its own copy of the guard for the validator call. |

### Task 2 — Paginated pending sales without N+1

| File | Responsibility |
|---|---|
| `src/main/java/com/gestion/repository/JpaSaleRepository.java` (modify) | Adds the paginated pending-sales query with its `@EntityGraph`. |
| `src/main/java/com/gestion/repository/JpaSaleDetailRepository.java` (modify) | Adds the batched "details of many sales" query with its `@EntityGraph`. |
| `src/main/java/com/gestion/dto/response/sale/PendingSalesWithDetailsResponse.java` (modify) | Carries a `Page<SaleResponse>` instead of a `List<SaleResponse>`. |
| `src/main/java/com/gestion/service/client/FindPendingSaleWithDetailsByClientIdService.java` (modify) | Assembles the page: sales + details + account totals, in three queries. |
| `src/main/java/com/gestion/controller/client/GetPendingSaleWithDetailsController.java` (modify) | Accepts the pagination parameters and builds the `Pageable`. |

`README.md` and `improvements.md` are updated inside the task that changes the contract they describe.

### Explicitly out of scope

- **P3-23** (move `FindPendingSaleWithDetailsByClientIdService` to `service/sale/` and rename the controller): the service is rewritten here, but the rename belongs to the P3 wave and would mix a mechanical rename into a behavioural change.
- **P1-8** (`@Version` on `Product`) and **P1-7** (N+1 in products and offers): separate items with their own decisions.
- **P2-20's expiry change**: the presigned URL expiry stays at 1 hour.

---

### Task 1: Business error for a user with no commerce

**Files:**
- Modify: `src/main/java/com/gestion/enums/ErrorCode.java`
- Create: `src/main/java/com/gestion/exception/CommerceNotAvailableException.java`
- Create: `src/main/java/com/gestion/service/commerce/CommerceAvailabilityValidatorService.java`
- Modify: `src/main/java/com/gestion/exception/handler/GlobalExceptionHandler.java`
- Modify: `src/main/java/com/gestion/service/sellpoint/SellPointCreatorService.java`
- Modify: `src/main/java/com/gestion/service/client/ClientCreatorService.java`
- Modify: `src/main/java/com/gestion/service/category/CategoryCreatorService.java`
- Modify: `src/main/java/com/gestion/service/sale/SaleCreatorService.java`
- Modify: `src/main/java/com/gestion/service/cashier/CashierCreatorService.java`
- Modify: `src/main/java/com/gestion/service/cashier/CashierUpdaterService.java`
- Modify: `README.md`, `improvements.md`

**Interfaces:**
- Consumes: nothing from other tasks.
- Produces:
  - `ErrorCode.COMMERCE_NOT_AVAILABLE` — `403`, default message `"The authenticated user has no commerce associated"`.
  - `CommerceNotAvailableException(ErrorCode code, String message)` extends `BusinessException`.
  - `CommerceAvailabilityValidatorService.checkCommerceIsAvailable(Long commerceId)` — void; throws `CommerceNotAvailableException` when `commerceId` is `null`, does nothing otherwise.
  - The 6 services keep their existing public method signatures; only the body of their `commerceId == null` guard changes.

- [ ] **Step 1: Add the error code, the exception and the validator**

  In `ErrorCode.java`, add immediately after the `ACCESS_DENIED(403, "Access denied"),` line:

  ```java
  COMMERCE_NOT_AVAILABLE(403, "The authenticated user has no commerce associated"),
  ```

  Create `src/main/java/com/gestion/exception/CommerceNotAvailableException.java`:

  ```java
  package com.gestion.exception;

  import com.gestion.enums.ErrorCode;

  public class CommerceNotAvailableException extends BusinessException {

      public CommerceNotAvailableException(ErrorCode code, String message) {
          super(code, message);
      }
  }
  ```

  Create `src/main/java/com/gestion/service/commerce/CommerceAvailabilityValidatorService.java`:

  ```java
  package com.gestion.service.commerce;

  import com.gestion.enums.ErrorCode;
  import com.gestion.exception.CommerceNotAvailableException;
  import org.springframework.stereotype.Service;

  @Service
  public class CommerceAvailabilityValidatorService {

      public void checkCommerceIsAvailable(Long commerceId) {
          if (commerceId == null) {
              throw new CommerceNotAvailableException(ErrorCode.COMMERCE_NOT_AVAILABLE,
                      ErrorCode.COMMERCE_NOT_AVAILABLE.getDefaultMessage());
          }
      }
  }
  ```

  The message is taken from the `ErrorCode` so the six call sites cannot drift apart.

  Verify: `.\mvnw.cmd -q -DskipTests compile` → `BUILD SUCCESS`.

- [ ] **Step 2: Record the current behaviour before changing it**

  Build the scenario (the API cannot create a user without a commerce, so it is created and then detached):

  ```bash
  # 1. as SUDO, create a commerce with an owner whose credentials you choose
  curl -s -c sudo.txt -H "Content-Type: application/json" \
    -X POST http://localhost:8080/api/auth/login \
    -d '{"email":"admin@admin.com","password":"admin123"}'
  curl -s -b sudo.txt -F username=Owner -F email=owner-p215@p2b.test \
    -F password=ownerp215123 -F phoneNumber=1234567890 \
    -F name=Comercio P215 -F address=Direccion P215 -F cuit=20987654321 \
    -F profitMultiplier=1.5 http://localhost:8080/api/commerces

  # 2. detach the owner from its commerce (the FK allows null)
  mysql -h localhost -P 3306 -u root -padmin -D gestion \
    -e "UPDATE users SET commerce_id = NULL WHERE email='owner-p215@p2b.test';"

  # 3. log in: the JWT now carries commerceId = null
  curl -s -c owner.txt -H "Content-Type: application/json" \
    -X POST http://localhost:8080/api/auth/login \
    -d '{"email":"owner-p215@p2b.test","password":"ownerp215123"}'

  # 4. call a commerce-scoped OWNER endpoint
  curl -s -b owner.txt -H "Content-Type: application/json" \
    -X POST http://localhost:8080/api/clients \
    -d '{"name":"Verificacion","lastName":"P215","phoneNumber":"1234567890","dni":"30111222","address":"Calle 123"}'
  ```

  Verify: the last call answers `403` with `"code":"ACCESS_DENIED"` and a message that is the raw internal text — `"Commerce ID is null"` or `"Authenticated user does not have a commerce associated"` depending on the service that answers first. That leak is the defect.

  Also confirm the role-denial path for the later step: as a CASHIER of that commerce, `POST /api/categories` answers `403` with `"code":"ACCESS_DENIED"` (role denied by `@PreAuthorize`, before any service runs).

- [ ] **Step 3: Route the six guards through the validator**

  In each of the six services, delete the `if (commerceId == null) { throw new AccessDeniedException(...); }` block, add the field `private final CommerceAvailabilityValidatorService commerceAvailabilityValidatorService;`, add the import `com.gestion.service.commerce.CommerceAvailabilityValidatorService`, remove the now-unused import `org.springframework.security.access.AccessDeniedException`, and put this line where the block used to be, as the first statement of the method:

  ```java
  commerceAvailabilityValidatorService.checkCommerceIsAvailable(commerceId);
  ```

  The six call sites are `SellPointCreatorService.createSellPoint`, `ClientCreatorService.createClient`, `CategoryCreatorService.createCategory`, `SaleCreatorService.createSale`, `CashierCreatorService.createCashier` and `CashierUpdaterService.updateCashier`.

  Verify: `.\mvnw.cmd -q -DskipTests compile` → `BUILD SUCCESS`. `grep -rn "AccessDeniedException" src/main/java` must now match only `GlobalExceptionHandler` and `config/SecurityConfig`.

- [ ] **Step 4: Stop forwarding Spring's raw message**

  In `GlobalExceptionHandler.handleAccessDeniedException`, replace the argument `exception.getMessage()` with `ErrorCode.ACCESS_DENIED.getDefaultMessage()`:

  ```java
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException exception) {
      // el mensaje crudo de Spring puede filtrar detalles internos: se usa el del codigo
      return buildResponse(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage());
  }
  ```

  Verify: `.\mvnw.cmd -q -DskipTests compile` → `BUILD SUCCESS`.

- [ ] **Step 5: Run the check again — it must now pass**

  Repeat step 2 steps 3 and 4 (log in and call `POST /api/clients`).

  Verify: the response is exactly

  ```json
  {"statusCode":403,"code":"COMMERCE_NOT_AVAILABLE","message":"The authenticated user has no commerce associated"}
  ```

  No occurrence of "Commerce ID is null" anywhere in the response.

- [ ] **Step 6: Check the role-denial path did not regress**

  As a CASHIER of a commerce, call `POST /api/categories` (OWNER only).

  Verify: `403` with `"code":"ACCESS_DENIED"` and `"message":"Access denied"` — the curated message, not an internal one, and not a `500`.

- [ ] **Step 7: Update the docs**

  In `README.md`, in the "Códigos de error" table, add a row under the 403 group:

  ```
  | `COMMERCE_NOT_AVAILABLE` | 403 | El usuario autenticado no tiene un comercio asociado |
  ```

  In `improvements.md`, change the P2-15 row's "Pendiente" cell to "**RESUELTO**" and append to its Problem cell: the fix replaced Spring's `AccessDeniedException` with `CommerceNotAvailableException` + `ErrorCode.COMMERCE_NOT_AVAILABLE`, raised from a single `CommerceAvailabilityValidatorService` used by the six services, and the handler no longer forwards Spring's raw message.

  Verify: `git diff README.md improvements.md` shows only those two edits.

- [ ] **Step 8: Run the smoke test**

  `.\mvnw.cmd test` → `Tests run: 1, Failures: 0, Errors: 0` and `BUILD SUCCESS` (the `contextLoads` test boots the whole context, which is what proves the new bean is wired).

- [ ] **Step 9: Clean up the scenario data**

  ```bash
  mysql -h localhost -P 3306 -u root -padmin -D gestion -e "
    DELETE FROM users WHERE email='owner-p215@p2b.test';
    DELETE FROM commerces WHERE id = <commerceIdFromStep2>;"
  ```

  The commerce table is `commerces`, not `commerce`. The owner was detached from its commerce in
  step 2, so it must be deleted by email rather than by commerce id.

  Verify: `SELECT COUNT(*) FROM users WHERE email='owner-p215@p2b.test';` returns `0`.

- [ ] **Step 10: Commit**

  ```bash
  git add src/main/java/com/gestion/enums/ErrorCode.java \
          src/main/java/com/gestion/exception/CommerceNotAvailableException.java \
          src/main/java/com/gestion/service/commerce/CommerceAvailabilityValidatorService.java \
          src/main/java/com/gestion/exception/handler/GlobalExceptionHandler.java \
          src/main/java/com/gestion/service/sellpoint/SellPointCreatorService.java \
          src/main/java/com/gestion/service/client/ClientCreatorService.java \
          src/main/java/com/gestion/service/category/CategoryCreatorService.java \
          src/main/java/com/gestion/service/sale/SaleCreatorService.java \
          src/main/java/com/gestion/service/cashier/CashierCreatorService.java \
          src/main/java/com/gestion/service/cashier/CashierUpdaterService.java \
          README.md improvements.md
  git commit -m "fix(errors): replace Spring AccessDeniedException with a business error"
  ```

  **Only when the user explicitly asks for it.**

---

### Task 2: Paginated pending sales without N+1

**Files:**
- Modify: `src/main/java/com/gestion/repository/JpaSaleRepository.java`
- Modify: `src/main/java/com/gestion/repository/JpaSaleDetailRepository.java`
- Modify: `src/main/java/com/gestion/dto/response/sale/PendingSalesWithDetailsResponse.java`
- Modify: `src/main/java/com/gestion/service/client/FindPendingSaleWithDetailsByClientIdService.java`
- Modify: `src/main/java/com/gestion/controller/client/GetPendingSaleWithDetailsController.java`
- Modify: `README.md`, `improvements.md`

**Interfaces:**
- Consumes: `PageableFactory.create(int page, int size, String sortBy, String sortDir, Set<String> allowedSortProperties)` and `SortableProperties.SALE` — both already exist and are used by `SalesGetController`.
- Produces:
  - `JpaSaleRepository.findPendingByClientIdAndCommerceId(Long commerceId, Long clientId, Pageable pageable)` → `Page<Sale>`, with `@EntityGraph(attributePaths = {"client", "sellPoint", "user"})`.
  - `JpaSaleDetailRepository.findAllBySaleIdIn(List<Long> saleIds)` → `List<SaleDetail>`, with `@EntityGraph(attributePaths = "product")`.
  - `PendingSalesWithDetailsResponse(Long pendingSalesCount, BigDecimal totalPending, Page<SaleResponse> pendingSales)`.
  - `FindPendingSaleWithDetailsByClientIdService.findPendingSalesWithDetailsByClientId(Long clientId, Long commerceId, Pageable pageable)` → `PendingSalesWithDetailsResponse`. The third parameter is new.
  - `GET /api/clients/{clientId}/pending-sales-with-details` gains `page` (default `0`), `size` (default `10`), `sortBy` (default `createdAt`), `sortDir` (default `asc`).

- [ ] **Step 1: Add the two repository methods**

  In `JpaSaleRepository.java`, add:

  ```java
  /**
   * Ventas pendientes de un cliente. El EntityGraph trae las relaciones que lee el mapper de
   * respuesta (cliente, punto de venta y usuario): sin el, cada venta de la pagina dispara una
   * query por relacion.
   */
  @EntityGraph(attributePaths = {"client", "sellPoint", "user"})
  @Query("""
          SELECT s FROM Sale s
          WHERE s.commerce.id = :commerceId
            AND s.client.id = :clientId
            AND s.status = com.gestion.enums.SaleStatus.PENDING
          """)
  Page<Sale> findPendingByClientIdAndCommerceId(@Param("commerceId") Long commerceId,
                                                  @Param("clientId") Long clientId,
                                                  Pageable pageable);
  ```

  In `JpaSaleDetailRepository.java`, add:

  ```java
  @EntityGraph(attributePaths = "product")
  List<SaleDetail> findAllBySaleIdIn(List<Long> saleIds);
  ```

  Verify: `.\mvnw.cmd -q -DskipTests compile` → `BUILD SUCCESS` (nothing calls them yet).

- [ ] **Step 2: Paginate the service and change the response shape**

  These three files change together — the DTO and the service cannot compile apart.

  In `PendingSalesWithDetailsResponse.java`, replace the `List<SaleResponse> pendingSales` component with `Page<SaleResponse> pendingSales` and add the import `org.springframework.data.domain.Page`. Keep `pendingSalesCount` and `totalPending` with their current types (`Long` and `BigDecimal`).

  In `FindPendingSaleWithDetailsByClientIdService.java`, add the field `private final ClientAccountSummaryService clientAccountSummaryService;` (import `com.gestion.service.client.ClientAccountSummaryService` — same package, no import needed) and replace the body of `findPendingSalesWithDetailsByClientId` with:

  ```java
  @Transactional(readOnly = true)
  public PendingSalesWithDetailsResponse findPendingSalesWithDetailsByClientId(Long clientId,
                                                                               Long commerceId,
                                                                               Pageable pageable) {
      Page<Sale> pendingSales = jpaSaleRepository
              .findPendingByClientIdAndCommerceId(commerceId, clientId, pageable);

      // los detalles de toda la pagina salen en una sola query y se agrupan en memoria
      List<Long> saleIds = pendingSales.getContent().stream().map(Sale::getId).toList();
      Map<Long, List<SaleDetail>> detailsBySaleId = saleIds.isEmpty()
              ? Map.of()
              : jpaSaleDetailRepository.findAllBySaleIdIn(saleIds).stream()
                      .collect(Collectors.groupingBy(detail -> detail.getSale().getId()));

      Page<SaleResponse> saleResponses = pendingSales.map(sale ->
              SaleMapper.toResponse(sale, detailsBySaleId.getOrDefault(sale.getId(), List.of())));

      // el conteo y el total son de TODAS las ventas pendientes del cliente, no de la pagina
      ClientAccountSummary summary = clientAccountSummaryService
              .findByClientIds(List.of(clientId), commerceId)
              .get(clientId);

      return new PendingSalesWithDetailsResponse(
              summary.pendingSalesCount(),
              summary.totalPending(),
              saleResponses
      );
  }
  ```

  Required imports: `com.gestion.dto.project.ClientAccountSummary`, `com.gestion.model.SaleDetail`, `org.springframework.data.domain.Page`, `org.springframework.data.domain.Pageable`, `java.util.Map`, `java.util.stream.Collectors`. Remove the `Pageable.unpaged(Sort.by(...))` construction and the now-unused `Sort` import.

  In `GetPendingSaleWithDetailsController.java`, add the parameters and build the `Pageable`
  exactly as `SalesGetController.searchSales` does — same parameter names, same defaults, same
  `PageableFactory.create(...)` call:

  ```java
  @GetMapping("/{clientId}/pending-sales-with-details")
  @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
  public ResponseEntity<PendingSalesWithDetailsResponse> getPendingSalesWithDetails(
          @PathVariable Long clientId,
          @AuthenticationPrincipal UserPrincipal authenticatedUser,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "createdAt") String sortBy,
          @RequestParam(defaultValue = "asc") String sortDir) {
      Pageable pageable = PageableFactory.create(page, size, sortBy, sortDir, SortableProperties.SALE);
      return ResponseEntity.ok(
              findPendingSaleWithDetailsByClientIdService
                      .findPendingSalesWithDetailsByClientId(clientId, authenticatedUser.getCommerceId(), pageable));
  }
  ```

  The only difference from `SalesGetController` is the default sort direction: `asc`, because this
  endpoint has always returned the oldest pending sale first.

  Required imports: `com.gestion.utils.PageableFactory`, `com.gestion.utils.SortableProperties`, `org.springframework.data.domain.Pageable`, `org.springframework.web.bind.annotation.RequestParam`.

  Verify: `.\mvnw.cmd -q -DskipTests compile` → `BUILD SUCCESS`.

- [ ] **Step 3: Check the new behaviour against a live instance**

  As an OWNER of a commerce that has a client with pending sales:

  ```bash
  curl -s -b owner.txt "http://localhost:8080/api/clients/<clientId>/pending-sales-with-details?page=0&size=1"
  ```

  Verify:
  - `pendingSales` is now an object with `content` and `page`, not an array.
  - `pendingSales.content` holds at most one sale when `size=1`.
  - `pendingSalesCount` and `totalPending` still describe **all** the client's pending sales — compare them with the same values on `GET /api/clients/{dni}`, which the README documents as coming from the same account summary.
  - `pendingSales.page.totalElements` equals `pendingSalesCount`.

- [ ] **Step 4: Check the empty and non-existent client cases**

  ```bash
  curl -s -b owner.txt "http://localhost:8080/api/clients/999999/pending-sales-with-details"
  ```

  Verify: `200` (not `404`) with `"pendingSalesCount":0`, `"totalPending":0` and an empty `pendingSales.content`. The README states this behaviour explicitly, so changing it would break the front.

- [ ] **Step 5: Check a "Consumidor final" sale does not leak into a client's pending sales**

  A sale created without `clientId` belongs to no client. Create one as the same OWNER with
  `paymentMethod: CREDIT_ACCOUNT` (so it is `PENDING`, and would show up in a pending list if the
  filter were broken) and **no** `clientId` field, then query the pending sales of a real client of
  the same commerce.

  Verify: the sale without a client does not appear in `pendingSales.content`, and neither
  `pendingSalesCount` nor `totalPending` changes because of it. Both queries filter on
  `s.client.id = :clientId`, so a null client cannot slip into the group of another client.

- [ ] **Step 6: Check the pagination parameters are validated**

  ```bash
  curl -s -b owner.txt "http://localhost:8080/api/clients/<clientId>/pending-sales-with-details?page=-1"
  curl -s -b owner.txt "http://localhost:8080/api/clients/<clientId>/pending-sales-with-details?size=100000"
  curl -s -b owner.txt "http://localhost:8080/api/clients/<clientId>/pending-sales-with-details?sortBy=dropTable"
  ```

  Verify: each answers `400` with `"code":"INVALID_PARAMETER"` — `PageableFactory` rejects out-of-range values instead of clamping them silently.

- [ ] **Step 7: Check the N+1 is gone**

  With `spring.jpa.show-sql=true` on, count the `Hibernate: select` lines the request produces (the session revalidation in `JwtFilter` counts as one).

  Verify: the count is the **same** for a client with 1 pending sale and for a client with 5 — at most 4 (1 revalidation + 1 page of sales + 1 batched details + 1 account aggregate). Before the change the count grew with the number of sales.

- [ ] **Step 8: Update the docs**

  In `README.md`, in the "Clientes y cuentas corrientes" section:
  - the endpoint row becomes `| GET | `/api/clients/{clientId}/pending-sales-with-details` | CASHIER/OWNER | — | `PendingSalesWithDetailsResponse` · `page`, `size`, `sortBy`, `sortDir` |`;
  - the bullet describing the endpoint changes to say that `pendingSales` is now a paginated `Page<SaleResponse>` (`content` + `page`), that `pendingSalesCount` and `totalPending` cover **all** the client's pending sales regardless of the page, and that the default order is `createdAt` ascending.

  In `improvements.md`, change the P2-17 row's "Pendiente" cell to "**RESUELTO**" and append to its Problem cell: the endpoint now paginates with `PageableFactory`, the sales of a page come with their relations already loaded, the details come in one batched query, and the totals come from the existing account-summary aggregate.

  Verify: `git diff README.md improvements.md` shows only those two edits.

- [ ] **Step 9: Run the smoke test**

  `.\mvnw.cmd test` → `Tests run: 1, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

- [ ] **Step 10: Commit**

  ```bash
  git add src/main/java/com/gestion/repository/JpaSaleRepository.java \
          src/main/java/com/gestion/repository/JpaSaleDetailRepository.java \
          src/main/java/com/gestion/dto/response/sale/PendingSalesWithDetailsResponse.java \
          src/main/java/com/gestion/service/client/FindPendingSaleWithDetailsByClientIdService.java \
          src/main/java/com/gestion/controller/client/GetPendingSaleWithDetailsController.java \
          README.md improvements.md
  git commit -m "fix(sale): paginate pending sales and batch their detail queries"
  ```

  **Only when the user explicitly asks for it.**

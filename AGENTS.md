# AGENTS.md

Single-module Spring Boot backend for a stock/sales management system (UTN TUP, Programación IV).
Docs and comments are mostly Spanish; commit messages are English.

## Commands

- No global Maven/Gradle: always use the wrapper — `.\mvnw.cmd <goal>` (Windows) / `./mvnw <goal>` (bash). Java 25
  required.
- Typecheck = compile (no linter, formatter, static analysis, or CI config exists):
  `.\mvnw.cmd -q -DskipTests compile`
- Tests: `.\mvnw.cmd test`; single test: `.\mvnw.cmd test -Dtest=ProyectoFinalApplicationTests`
- Run app: `.\mvnw.cmd spring-boot:run`
- Verify in order: compile -> test. Compile failures block everything else.
- No commits without my explicit instruction.
- No pushes without my explicit instruction.

## Infrastructure (must be running manually)

- MySQL at `localhost:3306`, database `gestion`, user `root`/`admin`. Credentials are read from
  environment variables (`MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`). No H2/Testcontainers.
- **Profiles**: `application.properties` has **no defaults** for secrets/credentials (`JWT_SECRET`,
  `MYSQL_*`, `MINIO_*`): the app fails at startup if they are missing. Dev defaults (plus DEBUG
  security logs, `show-sql`, non-`Secure` cookie, CORS to `http://localhost:5173`) live in
  `application-dev.properties`; run locally with `SPRING_PROFILES_ACTIVE=dev`. The test uses
  `@ActiveProfiles("dev")`. `JwtProperties`/`AdminProperties`/`AuthCookieProperties` are `@Validated`.
- `docker compose up -d` starts **only** MinIO (console `http://localhost:9001`, credentials
  `MINIO_ACCESS_KEY`/`MINIO_SECRET_KEY` from `.env`, required by compose; bucket `gestionbucket`). MinIO credentials via `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`
  (dev defaults in `application-dev.properties`). Needed only for file uploads (product images, commerce logos).
- The only test is `@SpringBootTest contextLoads`; it boots the full context, so it **requires MySQL**
  (`ddl-auto=update` connects at startup). It does not require MinIO.
- Schema comes from JPA entities via `spring.jpa.hibernate.ddl-auto=update`; there is no Flyway/Liquibase,
  and the database must already exist.
- Copy `.env.example` to `.env` and fill in values, then export to shell before running
  (e.g. `set -a; source .env; set +a` in bash, or `$env:SPRING_APPLICATION_JSON` in PowerShell).

## Architecture

- Layer: `controller/<resource>/...` -> `service/<resource>/...` -> `repository/Jpa*Repository`
  (Spring Data interfaces) -> `model` (JPA entities). Mappers in `mappers/` are static-method classes,
  not beans.
- One class per endpoint, named `Resource` + HTTP verb + `Controller`
  (e.g. `ProductsGetController`, `CommerceToggleActivePatchController`).
  One class per service use case, named `Entity` + action + `Service`
  (e.g. `ProductCreatorService`, `CategoryFinderByIdService`). Follow both patterns for new code.
- DTOs are records: requests in `dto/request`, responses in `dto/response`, accessed as `request.field()`
  (no getters). Requests rely on `spring-boot-starter-validation` annotations; `GlobalExceptionHandler`
  returns every field error in `fields[]` under `code=VALIDATION_ERROR`.
- Injection via constructor with Lombok (`@AllArgsConstructor`; a few older controllers still use
  `@RequiredArgsConstructor`). Never field injection.
- Business errors: throw exceptions extending `exception/BusinessException` with an `ErrorCode` from
  `enums/ErrorCode` (key + HTTP status + default message). The single `BusinessException` handler in
  `exception/handler/GlobalExceptionHandler` maps them; any other exception surfaces as
  `code=INTERNAL_ERROR` (500) without leaking internals.
- `EntityManager.getReference(...)` proxies are used to wire relations by id without a lookup — the live
  example is `service/sale/SaleCreatorService.java` (README's claim that it is used in
  `ProductCreatorService` is stale). Only safe when the id already comes from a validated JWT.
- Cross-tenant scoping: entity-by-id lookups that serve an endpoint go through a
  `XFinderByIdAndCommerceIdService` using a `findByIdAndCommerceId` query (derived or JPQL) and answer **404** (never
  403) when the row belongs to another commerce. Live examples:
  `service/sale/SaleFinderByIdAndCommerceIdService`, `service/sellpoint/SellPointFinderByIdAndCommerceIdService`.
  Do not reuse unscoped finders (e.g. `SaleFinderByIdService`) in controllers.
- When a finder feeds a response that maps several relations, use `@EntityGraph(attributePaths = ...)`
  on the repository method to eager-load them (see `JpaSaleRepository.findByIdAndCommerceId`): lazy
  proxies would blow up outside the transaction/session.
- **Transactional annotations**: all services use `org.springframework.transaction.annotation.Transactional`
  (not `jakarta.transaction.Transactional`). Spring's annotation supports `readOnly`, propagation
  and rollback rules; Jakarta's does not.
- **Pagination validation**: list endpoints that build `Pageable` manually validate `page >= 0`,
  `1 <= size <= 100`, `sortBy` against an allow-list, and `sortDir` ∈ {`asc`, `desc`}. Invalid values
  return `400 INVALID_PARAMETER` (rejected, not clamped). See `PageableFactory` and `SortableProperties`.

## Security

- JWT is read from a **`token` cookie** (name configurable, `app.auth.cookie.name`), not an
  `Authorization` header (`config/JwtFilter.java`). Testing with curl/browser requires the cookie;
  there is no bearer-token path. Build/clear the cookie only through `AuthCookieBuilderService`
  (`ResponseCookie` with `Secure`/`SameSite` from config, `Max-Age` from `jwt.expiration`).
- **CSRF is enabled** (cookie `XSRF-TOKEN` + header `X-XSRF-TOKEN`, plain handler, no XOR) for every
  non-safe method except the login. curl tests of POST/PUT/PATCH/DELETE must send both the cookie
  and the header (`GET /api/auth/csrf` returns the token). Sessions are `STATELESS` (no JSESSIONID).
- Default is deny-all: only `OPTIONS /**`, `POST /api/auth/login` and `GET /api/auth/csrf` are public
  (`config/SecurityConfig.java`). New public endpoints must be added there explicitly.
- Security filters (`JwtFilter`, `LoginRateLimitFilter`) are `@Component` but their servlet
  auto-registration is disabled with `FilterRegistrationBean(enabled=false)`: they run only inside
  the security chain. Errors written from filters go through `SecurityErrorWriter`.
- Login rate limit: in-memory Caffeine counters in `AuthLoginAttemptLimiterService` (per IP in
  `LoginRateLimitFilter`, per email failures in `AuthLoginService`) → `429 TOO_MANY_LOGIN_ATTEMPTS`.
- Passwords on create/update use `@PasswordPolicy` (8 chars min, 72 UTF-8 bytes max); the login
  request only has `@NotBlank`.
- Roles: `SUDO`, `OWNER`, `CASHIER` (`enums/Role.java`). Per-endpoint rules are
  `@PreAuthorize("hasRole('...')")` on the controller (`@EnableMethodSecurity` is active).
- Controllers take `@AuthenticationPrincipal UserPrincipal` and services trust it instead of
  re-querying the database.
- **Session snapshot**: the JWT is only used to identify the user (`userId` + `ver` claims). On every
  authenticated request `JwtFilter` loads a `UserSessionSnapshot` projection (email, name, role,
  user/commerce `active`, `commerceId`, `sellPointId`, `version`) and builds the `UserPrincipal` from
  it, so role, sell point or deactivation changes apply on the next request. Deactivated
  user/commerce → `403 ACCOUNT_DISABLED`; user no longer exists → `401 UNAUTHORIZED`.
- **Token version**: `User` has a JPA `@Version` that travels as the `ver` claim; `JwtFilter` rejects
  with 401 when it no longer matches the snapshot. Any update to a `User` row (password, email, sell
  point, active...) therefore logs that user out everywhere; `POST /api/auth/logout-all` bumps it
  explicitly (`JpaUserRepository.incrementVersion`). Don't modify the current user's own `User` row as
  a side effect of an unrelated request, or you'll end their session.
- Never put PII (email, DNI) in exception messages: they are logged by `GlobalExceptionHandler`.
  `IllegalArgumentException` is **not** mapped to 400 (it surfaces as 500); throw
  `InvalidParameterException` for client errors.

## Workflow

- Working tree often carries in-progress changes across model/mapper/response in one feature;
  recompile after any mapper signature change — call sites live in many services.

# AGENTS.md

Single-module Spring Boot backend for a stock/sales management system (UTN TUP, Programación IV).
Docs and comments are mostly Spanish; commit messages are English.

## Commands

- No global Maven/Gradle: always use the wrapper — `.\mvnw.cmd <goal>` (Windows) / `./mvnw <goal>` (bash). Java 25 required.
- Typecheck = compile (no linter, formatter, static analysis, or CI config exists):
  `.\mvnw.cmd -q -DskipTests compile`
- Tests: `.\mvnw.cmd test`; single test: `.\mvnw.cmd test -Dtest=ProyectoFinalApplicationTests`
- Run app: `.\mvnw.cmd spring-boot:run`
- Verify in order: compile -> test. Compile failures block everything else.

## Infrastructure (must be running manually)

- MySQL at `localhost:3306`, database `gestion`, user `root`/`admin` — hardcoded in
  `src/main/resources/application.properties`. No profiles, no env-var wiring, no H2/Testcontainers.
- `docker compose up -d` starts **only** MinIO (console `http://localhost:9001`, `admin`/`admin123`,
  bucket `gestionbucket`). Needed only for file uploads (product images, commerce logos).
- The only test is `@SpringBootTest contextLoads`; it boots the full context, so it **requires MySQL**
  (`ddl-auto=update` connects at startup). It does not require MinIO.
- Schema comes from JPA entities via `spring.jpa.hibernate.ddl-auto=update`; there is no Flyway/Liquibase,
  and the database must already exist.

## Architecture

- Layer: `controller/<resource>/...` -> `service/<resource>/...` -> `repository/Jpa*Repository`
  (Spring Data interfaces) -> `model` (JPA entities). Mappers in `mappers/` are static-method classes,
  not beans.
- One class per endpoint, named `Resource` + HTTP verb + `Controller`
  (e.g. `ProductsGetController`, `CommerceToggleActivePatchController`).
  One class per service use case, named `Entity` + action + `Service`
  (e.g. `ProductCreatorService`, `CategoryFinderByIdService`). Follow both patterns for new code.
- DTOs are records: requests in `dto/request`, responses in `dto/response`, accessed as `request.field()`
  (no getters). Requests rely on `spring-boot-starter-validation` annotations; the first field error is
  what `GlobalExceptionHandler` returns.
- Injection via constructor with Lombok (`@AllArgsConstructor`; a few older controllers still use
  `@RequiredArgsConstructor`). Never field injection.
- Business errors: throw custom exceptions from `exception/`; each one must get a handler in
  `exception/handler/GlobalExceptionHandler`, otherwise it surfaces as a 500.
- `EntityManager.getReference(...)` proxies are used to wire relations by id without a lookup — the live
  example is `service/sale/SaleCreatorService.java` (README's claim that it is used in
  `ProductCreatorService` is stale). Only safe when the id already comes from a validated JWT.

## Security

- JWT is read from a **`token` cookie**, not an `Authorization` header (`config/JwtFilter.java`).
  Testing with curl/browser requires the cookie; there is no bearer-token path.
- Default is deny-all: only `OPTIONS /**` and `POST /api/auth/login` are public
  (`config/SecurityConfig.java`). New public endpoints must be added there explicitly.
- Roles: `SUDO`, `OWNER`, `CASHIER` (`enums/Role.java`). Per-endpoint rules are
  `@PreAuthorize("hasRole('...')")` on the controller (`@EnableMethodSecurity` is active).
- The JWT carries userId/commerceId/role; controllers take `@AuthenticationPrincipal UserPrincipal`
  and services trust it instead of re-querying the database.

## Workflow

- Conventional commits with scopes, e.g. `feat(offer): ...`, `refactor(services): ...`.
- Working tree often carries in-progress changes across model/mapper/response in one feature;
  recompile after any mapper signature change — call sites live in many services.

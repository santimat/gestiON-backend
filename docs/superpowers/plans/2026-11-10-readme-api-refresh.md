# README API Refresh — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reescribir el `README.md` del repo para que documente exactamente la API expuesta hoy (46 endpoints, reglas de negocio vigentes y códigos de error reales), reorganizada por flujo de producto y manteniendo el encuadre público actual.

**Architecture:** Un único archivo `README.md` se reescribe por secciones, en orden, de arriba hacia abajo. Cada tarea escribe un bloque de secciones y lo verifica contra el código fuente (controllers, DTOs, `ErrorCode`). No se toca código de aplicación.

**Tech Stack:** Markdown (GitHub-flavored), Spring Boot 4.1 / Java 25 (solo como referencia para verificar el comportamiento documentado).

**Spec:** `docs/superpowers/specs/2026-11-10-readme-api-refresh-design.md`

## Global Constraints

- Idioma del documento: **español**. Tono y formato de tablas iguales a los del README actual.
- Cada endpoint se documenta **exactamente una vez**, con: método, ruta, roles, request, response.
- Roles: siempre `SUDO` / `OWNER` / `CASHIER` / `CASHIER/OWNER` / `Auth` / `Público`.
- Bodies: `JSON` o `multipart` (multipart cuando el request lleva `MultipartFile`).
- Los keys de error se copian textualmente de `com.gestion.enums.ErrorCode`; nunca se traducen ni se inventan variantes.
- Los nombres de DTO se citan tal cual (`ProductRequest`, `CommerceWithOwnerResponse`, …).
- Encuadre público: se describe comportamiento observable, no clases internas ni nombres de servicios.
- No se agregan ejemplos de `curl`, Postman ni bloques de JSON de respuesta nuevos (salvo los ya existentes: formato de error, `SaleRequest`, paginado).
- No se modifica ningún archivo bajo `src/`.

## Review Focus

Los cinco casos donde una lectura errónea del código arruinaría el documento (cada uno se ancla a su tarea):

1. **Oferta activa sobre venta** — `SaleCreatorService:64-69`: la oferta `ACTIVE` del método de pago *pisa* el descuento manual del request. Si el README dice lo contrario, el front aplica el descuento dos veces. → Task 3 (flujo Ventas).
2. **`GET /api/sales/top-products` cuenta solo ventas `PAYED` y de los últimos 30 días** (`JpaSaleRepository:258-282`): no es "todo el histórico". → Task 2 (Dashboard).
3. **`GET /api/products/low-stock` devuelve solo productos activos con `currentStock <= minStock`** (`JpaProductRepository:51-63`): no incluye inactivos ni los que superan el mínimo. → Task 2 (Dashboard).
4. **`PATCH /api/commerces` es multipart y devuelve `CurrentCommerceResponse`** (`CommercePatchController:24-31`, `CommercePatchRequest`): no es el `PATCH /api/commerces/{id}` que sugiere la convención REST, ni devuelve el response de comercio con owner. → Task 2 (Comercios).
5. **`GET /api/clients/{dni}` vs `GET /api/clients/{clientId}/pending-sales-with-details`**: conviven dos identificadores distintos en el mismo recurso; documentarlos con el mismo nombre de path variable induce a error. → Task 3 (Clientes).
6. **Logout no revoca el token**: `AuthLogoutController` solo manda la cookie expirada; el JWT sigue siendo válido hasta su expiración (24 h). Afirmar que "invalida la sesión" es falso. → Task 1 (Autenticación).
7. **Tres contratos de paginación distintos**: `/api/commerces` toma `sortOrder` y `/api/cashiers` toma `sort=campo,direccion`; mandar `sortDir`/`sortBy` a esos dos endpoints se ignora en silencio y el orden sale mal. → Task 1 (Convenciones) + Task 2 (filas de Comercios y Cajeros).
8. **Las dos ofertas se acumulan**: la de producto baja el precio de la línea y entra en el subtotal; la de método de pago se aplica después sobre ese subtotal. Si el README solo menciona la segunda, el front calcula mal el total. → Task 3 (Ventas).

---

### Task 1: Cabecera, contexto y convenciones (secciones 1–5)

**Files:**
- Modify: `README.md` (secciones de título a "Convenciones de listado")

**Interfaces:**
- Consumes: spec §7 (estructura) y §8 (bloques 6.1–6.3 de referencia de formato).
- Produces: un `README.md` con encabezado, intro, Tecnologías, Arquitectura, Autenticación y Convenciones de listado completos, listo para que las tareas siguientes agreguen la sección "Endpoints".

- [ ] **Step 1: Reescribir el encabezado y la introducción**

Título `# API de gestión de stock y control de ventas` (se mantiene). Intro en 2–3 líneas: qué es el sistema, qué expone la API (comercios, productos, categorías, clientes, puntos de venta, ventas, ofertas) y a quién le sirve (quien consume la API). Base: sección 1 del README actual + §2/§7 del spec.

- [ ] **Step 2: Actualizar la sección "Tecnologías"**

Contenido exacto a dejar:
- Java 25 + Spring Boot 4.1 (Spring Security con JWT, Spring Data JPA)
- MySQL 8 (`ddl-auto=update`)
- MinIO (S3 compatible) para imágenes de productos y logos de comercio
- Lombok, Maven Wrapper
- Autenticación por JWT en cookie `token` (no header `Authorization`)

Verificar contra `pom.xml`: `<version>4.1.0</version>`, `<java.version>25</java.version>`.

- [ ] **Step 3: Ajustar la sección "Arquitectura"**

Mantener la cadena `Request → Controller → Service → Repository → MySQL` y las viñetas de Controller/Service/Repository/DTOs. Mantener el bloque JSON del formato de error tal cual (ya coincide con `ExceptionResponse` + `GlobalExceptionHandler`). No agregar nombres de clases concretas.

- [ ] **Step 4: Ajustar la sección "Autenticación" y la tabla de roles**

Dejar el flujo de 3 pasos (login → cookie `token`; JWT con `userId`, `commerceId`, `role`, `sellPointId` (`null` para OWNER/SUDO); logout) y la tabla de roles SUDO/OWNER/CASHIER. Conservar la nota de scoping: un recurso de otro comercio responde `404`, nunca `403`.

Con lifecycle completo, verificado en `AuthLoginPostController`, `AuthLogoutController` y `JwtService`:
- Cookie `token`: `HttpOnly`, `Path=/`, `Secure=false`, 24 h (`Max-Age=86400`).
- El JWT dura 24 h (`jwt.expiration=86400000`) y lleva `sub` (email), `name`, `userId`,
  `commerceId`, `role`, `active`, `sellPointId`.
- `POST /api/auth/logout` **solo expira la cookie**: no hay revocación del token en el servidor, así
  que **no** se puede afirmar que "invalida la sesión". Decirlo explícitamente.
- `401 UNAUTHORIZED` = sin cookie o token inválido; `403 ACCESS_DENIED` = rol insuficiente;
  `GET /api/auth/me` devuelve `403 ACCOUNT_DISABLED` si el comercio está deshabilitado.

- [ ] **Step 5: Ajustar "Convenciones de listado"**

Convención general: `page` (0-based), `size`, `sortBy`, `sortDir` (`asc`/`desc`), con filtros
opcionales como query params.

El bloque JSON de paginado va **corregido**: no hay serializador custom de `Page` ni
`spring.data.web.pageable.serialization-mode` en el repo, así que aplica la serialización directa de
Spring Data — `content` + metadatos, **sin envoltorio `page`**:

```json
{
  "content": [ ... ],
  "totalElements": 42,
  "totalPages": 5,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

(sumar `numberOfElements`, `empty`, `sort` y `pageable` en la respuesta real).

Agregar una tabla chica de defaults por recurso y las **dos excepciones** al contrato general:

| Listado | Parámetros de orden | `size` default | Orden default |
|---|---|---|---|
| `/api/commerces` | `sortBy` + **`sortOrder`** (`ASC`/`DESC`) | 5 | `updatedAt` / `DESC` |
| `/api/cashiers` | **`sort=campo,direccion`** (binds `Pageable`) | 20 | sin orden |
| `/api/clients` | `sortBy` + `sortDir` | 5 | `updatedAt` / `asc` |
| `/api/categories` | `sortBy` + `sortDir` | 10 | `updatedAt` / `asc` |
| `/api/products`, `/api/products/with-offer` | `sortBy` + `sortDir` | 10 | `updatedAt` / `desc` |
| `/api/sell-points` | `sortBy` + `sortDir` | 10 | `updatedAt` / `desc` |
| `/api/sell-points/{id}/cashiers` | `sortBy` + `sortDir` | 10 | `name` / `asc` |
| `/api/sales` | `sortBy` + `sortDir` | 10 | `createdAt` / `desc` |
| `/api/offers` | `sortBy` + `sortDir` | 10 | `endDate` / `desc` |

- [ ] **Step 6: Verificar la sección**

Run: `git diff README.md`
Expected: el diff hasta acá solo toca los bloques 1–5; no hay tablas de endpoints todavía.

---

### Task 2: Endpoints — autenticación, comercios y dashboard (flujos 6.1–6.3)

**Files:**
- Modify: `README.md` (sección "Endpoints", primer bloque)

**Interfaces:**
- Consumes: spec §8 bloques "Autenticación y sesión", "Comercios y configuración del negocio" y "Dashboard y estadísticas".
- Produces: el encabezado `## Endpoints` con su leyenda de roles y tipos de body, más las tablas de los tres primeros flujos. La Task 3 los continúa.

- [ ] **Step 1: Escribir el encabezado `## Endpoints` y la leyenda**

Leyenda a dejar (formato del README actual, en blockquote):
`> **Roles**: SUDO = solo administrador · OWNER = solo dueño · CASHIER/OWNER = ambos ·`
`> **Auth** = cualquier usuario autenticado · **Público** = sin sesión.`
`> **JSON** = body JSON · **multipart** = multipart/form-data (incluye archivo de imagen/logo).`

- [ ] **Step 2: Escribir el flujo "Autenticación y sesión"**

Tabla de 3 filas: `POST /api/auth/login` (Público, `UserLoginRequest` JSON → `AuthenticatedUserResponse` + cookie `token`), `POST /api/auth/logout` (Auth, — → texto + cookie expirada), `GET /api/auth/me` (Auth, — → `AuthenticatedUserResponse`).

- [ ] **Step 3: Escribir el flujo "Comercios y configuración del negocio"**

8 filas, en el orden de la spec §8. Puntos que no se pueden omitir:
- `GET /api/commerces` usa los query params `sortOrder`/`sortBy` (no `sortDir`).
- `POST /api/commerces` y `PUT /api/commerces/{commerceId}/user/{userId}` son **multipart** (llevan `businessLogo`).
- `PATCH /api/commerces` (sin path variable) es OWNER, **multipart**, request `CommercePatchRequest` (`name`, `profitMultiplier`, `image`) y response `CurrentCommerceResponse`. Anotarlo como **nuevo**.
- `GET /api/commerces/stats` devuelve `total`, `active`, `inactive`.
- `PATCH /api/commerces/profit-multiplier` es JSON.

- [ ] **Step 4: Escribir el flujo "Dashboard y estadísticas"**

3 filas, todas OWNER, anotadas como **nuevo**: `GET /api/sales/stats` (`?granularity=DAY|WEEK|MONTH` → `SalesStatsByPeriodResponse`), `GET /api/sales/top-products` (`?limit=` default 5 → `List<TopProductsSoldResponse>`), `GET /api/products/low-stock` (→ `List<ProductLowStockResponse>`).

Debajo de la tabla, tres viñetas de semántica:
- Períodos en `America/Argentina/Buenos_Aires`: `DAY` = hoy, `WEEK` = últimos 7 días, `MONTH` = últimos 30 días.
- `top-products` cuenta **solo ventas `PAYED`** de la ventana de 30 días.
- `low-stock` devuelve **solo productos activos** con `currentStock <= minStock`.

- [ ] **Step 5: Verificar contra el código**

Run:
```bash
grep -rn "Mapping(" src/main/java/com/gestion/controller/auth src/main/java/com/gestion/controller/commerce
```
Expected: aparecen `/api/auth/login`, `/api/auth/logout`, `/api/auth/me`, `/api/commerces`, `/api/commerces/current`, `/api/commerces/stats`, `/api/commerces/profit-multiplier`, `/api/commerces/{id}/toggle-active`, `/api/commerces/{commerceId}/user/{userId}` — todas presentes en las tablas escritas.

---

### Task 3: Endpoints — catálogo, clientes, equipo, ventas y ofertas (flujos 6.4–6.8)

**Files:**
- Modify: `README.md` (sección "Endpoints", continuación)

**Interfaces:**
- Consumes: spec §8 bloques "Catálogo", "Clientes y cuentas corrientes", "Equipo", "Ventas" y "Ofertas y descuentos".
- Produces: el resto de las tablas de endpoints. La Task 4 cierra el documento.

- [ ] **Step 1: Escribir el flujo "Catálogo: categorías y productos"**

Tabla con las 5 filas de categorías y las 5 de productos, en el orden de la spec §8. Requisitos:
- `GET /api/categories` acepta el filtro `categoryName`.
- `GET /api/products` y `GET /api/products/with-offer` comparten filtros (`categoryId`, `searchText`, `active`); `with-offer` además expone oferta vigente, precio con descuento y precio sugerido.
- `POST /api/products` y `PUT /api/products/{id}` son **multipart** (`image` opcional).
- Debajo de la tabla, enumerar los campos de `ProductRequest`/`ProductUpdateRequest`: `name`, `description`, `costPrice`, `salePrice`, `profitMultiplier` (opcional), `image` (opcional), `categoryId`, `currentStock`, `minStock`.

- [ ] **Step 2: Escribir el flujo "Clientes y cuentas corrientes"**

5 filas: listado (filtros `searchText`, `name`, `lastName`), `GET /api/clients/{dni}`, `POST`, `PUT /api/clients/{id}`, y `GET /api/clients/{clientId}/pending-sales-with-details` (nuevo, `PendingSalesWithDetailsResponse`).

Debajo:
- Nota de que conviven dos identificadores: el detalle por cliente usa **DNI** (`/api/clients/{dni}`) y las ventas pendientes usan **id de cliente** (`/api/clients/{clientId}/...`).
- `ClientResponse` incluye el resumen de cuenta corriente: `salesCount`, `pendingSalesCount`, `totalSpent`, `totalPending`.
- El DNI es único por comercio (`409 CLIENT_DNI_ALREADY_EXISTS`).

- [ ] **Step 3: Escribir el flujo "Equipo: cajeros y puntos de venta"**

9 filas (4 de cajeros + 5 de puntos de venta) en el orden de la spec §8. Requisitos:
- `GET /api/cashiers` acepta el filtro `searchText`.
- `PUT /api/cashiers/{id}`: `password` vacío = mantener la actual.
- `POST`/`PUT /api/sell-points` son JSON con `name` y `cashierIds`.
- Mantener la nota de que un cajero pertenece a un solo punto de venta y que el punto de venta sale del JWT al registrar ventas.

- [ ] **Step 4: Escribir el flujo "Ventas"**

4 filas: `GET /api/sales` (filtros `clientId`, `status`, `paymentMethod`, `searchText`), `GET /api/sales/{saleId}` (Auth → `SaleResponse`), `POST /api/sales` (`SaleRequest` JSON → `SaleSummaryResponse`, 201), `PATCH /api/sales/{id}/mark-paid`.

Debajo, conservar el bloque JSON de `SaleRequest` (actualizado: `details` es un `Set` → el ejemplo sigue siendo un array) y las viñetas de reglas, **corrigiendo el punto de ofertas**. Las reglas a documentar, todas verificadas en `SaleCreatorService` y `SaleDetailsValidatorService`:

- Precios, subtotal y total se calculan **siempre en el servidor**; el request solo envía productos y
  cantidades. Los ítems repetidos del mismo producto se consolidan.
- `paymentMethod`: `CASH`, `CARD`, `TRANSFER`, `CREDIT_ACCOUNT`. El fiado (`CREDIT_ACCOUNT`) genera la
  venta `PENDING`; el resto, `PAYED`.
- `discountType` (`PERCENTAGE` / `FIXED_AMOUNT`) y `discount` van juntos; fijo ≤ subtotal
  (`400 DISCOUNT_EXCEEDS_SUBTOTAL`), porcentual ≤ 100% (`400 DISCOUNT_PERCENTAGE_EXCEEDS_100`),
  `discount` > 0 y `observations` ≤ 200 caracteres.
- **Si el método de pago de la venta tiene una oferta `ACTIVE`, el descuento de esa oferta prevalece
  sobre el descuento manual del request** y el manual se ignora.
- **Las dos ofertas se acumulan**: la oferta de producto (`offerType = PRODUCT`) baja el precio
  unitario de la línea y el subtotal ya sale de esos precios; el descuento del método de pago se
  aplica después, sobre ese subtotal.
- El descuento de stock es todo-o-nada: si algún ítem no pasa la validación (producto inactivo o
  sin stock), no se descuenta ni se registra nada (`409 PRODUCT_INACTIVE`, `409 INSUFFICIENT_STOCK`).
- `mark-paid` solo admite la transición `PENDING → PAYED` (`409 SALE_NOT_PENDING` en otro caso).
- El punto de venta sale del JWT (`sellPointId`) y puede venir `null`; nunca se envía en el request.

- [ ] **Step 5: Escribir el flujo "Ofertas y descuentos"**

5 filas en el orden de la spec §8. Debajo, las viñetas de reglas:
- `OfferRequest`/`OfferUpdateRequest`: `value`, `discountType`, `startDate`, `endDate`,
  `paymentMethods` (opcional); fechas **obligatorias** en ISO-8601 (`Instant`); `value` positivo.
- `value` según `discountType`: `PERCENTAGE` ≤ 100% (`400 DISCOUNT_PERCENTAGE_EXCEEDS_100`);
  `FIXED_AMOUNT` es monto y no tiene máximo.
- Fechas: al crear, la fecha de inicio no puede estar en el pasado y la de fin no puede ser anterior
  a la de inicio (`400 INVALID_DATE`); al actualizar, se puede conservar la fecha de inicio original
  aunque ya haya pasado.
- `status` derivado de las fechas: `SCHEDULED` / `ACTIVE` / `EXPIRED`; no se envía en el request.
- `offerType` derivado: con métodos de pago → `PAYMENT_METHOD` (se aplica sobre el total de la
  venta); sin métodos → `PRODUCT` (se aplica sobre el precio unitario del producto asignado).
- No superposición: `409 OFFER_OVERLAP` (mismo alcance y períodos cruzados).
- Incompatibilidades: `409 OFFER_HAS_PRODUCTS` y `400 OFFER_HAS_PAYMENT_METHODS`.
- `PUT /api/offers/{id}/products` **reemplaza** el conjunto de productos asignado (no lo agrega).
- `GET /api/offers/payment-methods`: por cada método de pago con oferta vigente devuelve método,
  valor y tipo de descuento. Es lo que usa la vista de venta para mostrar el descuento en tiempo real.

- [ ] **Step 6: Verificar contra el código**

Run:
```bash
grep -rhn "@GetMapping\|@PostMapping\|@PutMapping\|@PatchMapping" src/main/java/com/gestion/controller | sort
```
Expected: cada ruta listada en §8 del spec aparece exactamente una vez en las tablas del README.

---

### Task 4: Novedades, códigos de error y puesta en marcha (secciones 7–9)

**Files:**
- Modify: `README.md` (cola del documento)

**Interfaces:**
- Consumes: spec §9 (novedades) y §10 (errores); `com.gestion.enums.ErrorCode` para los status.
- Produces: documento completo y cerrado.

- [ ] **Step 1: Escribir la sección "Novedades de la API"**

Lista de 7 ítems tomados de spec §9 **menos el de ofertas** (el cambio de ofertas es una corrección
de un README que ya afirmaba lo contrario, no una novedad; vive en las secciones de Ventas y
Ofertas). Ítems: estadísticas de venta por período, ranking de productos más vendidos, alertas de
stock bajo, ventas pendientes de un cliente con detalle, actualización parcial del comercio,
resumen de cuenta corriente en la respuesta de clientes, y filtros por texto/nombre en clientes y
cajeros. Sin fechas, versiones ni referencias a commits.

- [ ] **Step 2: Reescribir la sección "Códigos de error"**

Reemplazar la tabla actual por una tabla agrupada por familia, con las keys **concretas** de
`ErrorCode` (sin comodines: el consumidor switchea sobre `code`) y su status real:
- Validación de request: `VALIDATION_ERROR` 400, `INVALID_REQUEST_BODY` 400, `INVALID_PARAMETER` 400, `MISSING_PARAMETER` 400, `METHOD_NOT_ALLOWED` 405, `UNSUPPORTED_MEDIA_TYPE` 415.
- Autenticación y autorización: `INVALID_CREDENTIALS` 401, `UNAUTHORIZED` 401, `ACCOUNT_DISABLED` 403, `ACCESS_DENIED` 403.
- No encontrado: `USER_NOT_FOUND`, `CLIENT_NOT_FOUND`, `CATEGORY_NOT_FOUND`, `PRODUCT_NOT_FOUND`, `COMMERCE_NOT_FOUND`, `SELL_POINT_NOT_FOUND`, `SALE_NOT_FOUND`, `OFFER_NOT_FOUND`, `PRODUCTS_OFFER_NOT_FOUND`, `ENDPOINT_NOT_FOUND` — todas 404.
- Unicidad: `EMAIL_ALREADY_EXISTS`, `CLIENT_DNI_ALREADY_EXISTS`, `CATEGORY_NAME_ALREADY_EXISTS`, `SELL_POINT_NAME_ALREADY_EXISTS`, `COMMERCE_ADDRESS_ALREADY_EXISTS`, `COMMERCE_CUIT_ALREADY_EXISTS` — todas 409.
- Conflictos de operación: `INSUFFICIENT_STOCK` 409, `PRODUCT_INACTIVE` 409, `SALE_NOT_PENDING` 409, `DATA_INTEGRITY_VIOLATION` 409.
- Descuentos: `INVALID_DISCOUNT` 400, `INVALID_DISCOUNT_PREFIX_ZERO` 400, `DISCOUNT_WITHOUT_TYPE` 400, `TYPE_WITHOUT_DISCOUNT` 400, `DISCOUNT_EXCEEDS_SUBTOTAL` 400, `DISCOUNT_PERCENTAGE_EXCEEDS_100` 400, `PRODUCT_SALE_PRICE_BELOW_COST` 400.
- Ofertas: `INVALID_DATE` 400, `OFFER_OVERLAP` 409, `OFFER_HAS_PAYMENT_METHODS` 400, `OFFER_HAS_PRODUCTS` 409.
- Archivos: `INVALID_FILE_TYPE` 400, `FILE_TOO_LARGE` 413, `FILE_UPLOAD_FAILED` 500, `FILE_DELETE_FAILED` 500, `FILE_PROCESSING_FAILED` 500.
- Interno: `INTERNAL_ERROR` 500.

Conservar la explicación de que `code` es una key estable y `fields[]` solo aparece en
`VALIDATION_ERROR`.

- [ ] **Step 3: Ajustar la sección "Correr el proyecto"**

Dejar el bloque actual (`docker compose up -d` solo levanta MinIO; `./mvnw spring-boot:run` requiere JDK 25) y agregar los valores reales verificados en `application.properties` y `docker-compose.yml`: MySQL `localhost:3306`, base `gestion`, `root`/`admin`; MinIO API `localhost:9000`, consola `localhost:9001`, `admin`/`admin123`, bucket `gestionbucket`; usuario bootstrap `admin@admin.com` / `admin123`. Mantener la nota de que la base debe existir previamente y que el schema lo genera Hibernate con `ddl-auto=update`.

- [ ] **Step 4: Verificar la cola del documento**

Run: `git diff README.md`
Expected: el documento termina con "Correr el proyecto" y no quedan restos de las secciones viejas.

---

### Task 5: Verificación final

**Files:**
- Modify: `README.md` (solo correcciones que surjan de la verificación)

**Interfaces:**
- Consumes: el README completo de las Tasks 1–4 y el spec §13.
- Produces: README verificado, sin afirmaciones no verificables.

- [ ] **Step 1: Cotejar endpoints contra controllers**

Run:
```bash
grep -rhn "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@PatchMapping\|@DeleteMapping" src/main/java/com/gestion/controller | sort
```
Expected: la lista de rutas del código y la del README coinciden en cantidad (46 endpoints; `controller/user/UserPutController` no expone ninguno y no se documenta).

- [ ] **Step 2: Verificar que toda key de error citada exista**

Run:
```bash
grep -o "[A-Z][A-Z_]\{3,\}" README.md | sort -u
```
Expected: toda key listada aparece en `ErrorCode.java` (los enums `SUDO`/`OWNER`/`CASHIER` y los tipos `JSON`/`multipart` se excluyen por no ser keys de error).

- [ ] **Step 3: Verificar que todo DTO citado exista**

Run:
```bash
grep -o "[A-Z][A-Za-z]*Request\|[A-Z][A-Za-z]*Response" README.md | sort -u
```
Expected: cada nombre existe bajo `src/main/java/com/gestion/dto/request` o `dto/response`.

- [ ] **Step 4: Revisar consistencia de tablas markdown**

Run (PowerShell): leer `README.md` y, por cada línea que empieza con `|`, agrupar por tabla y contar separadores `|`; toda tabla debe tener el mismo número de columnas en todas sus filas.
Expected: cero tablas con conteos inconsistentes.

- [ ] **Step 5: Lectura final de punta a punta**

Run: `git diff README.md`
Expected: no queda ninguna afirmación que no se pueda respaldar con un controller, DTO, service o enum del repo. En particular, no debe quedar ninguna frase que diga que las ofertas no se aplican a la venta.

- [ ] **Step 6: Commit**

```bash
git add README.md docs/superpowers
git commit -m "docs(readme): refresh API reference and reorganize by product flow"
```

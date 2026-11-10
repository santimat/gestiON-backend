# Diseño: refresh y reorganización del README de la API

Fecha: 2026-11-10
Estado: propuesto (pendiente de aprobación)

## 1. Intención y por qué

El `README.md` es la puerta de entrada pública de esta API: describe el contrato HTTP que consumen
el frontend, QA y cualquier integrador. Hoy está desfasado respecto del backend — su última
actualización fue el commit `67643f4`, y desde entonces el proyecto sumó endpoints nuevos, validaciones
nuevas y cambió el comportamiento de las ofertas.

Un README desactualizado en una API pública no es un detalle de documentación: es la fuente de
verdad que el equipo de front usa para integrar, y hoy los lleva a construir contra endpoints que
no existen, a omitir campos obligatorios (`startDate`/`endDate` de las ofertas) y a asumir que las
ofertas no se aplican a la venta cuando sí se aplican.

Objetivo: que el README describa **exactamente** la API expuesta hoy, sin perder el encuadre actual
—documentación pública de consumo— y ganarle una organización por flujos que haga más fácil
encontrar lo que se busca.

## 2. Goals

- Documentar **todos** los endpoints expuestos por el backend, cada uno exactamente una vez, con
  método, ruta, roles, request y response.
- Corregir toda afirmación que hoy sea falsa (ofertas aplicadas a la venta, filtros de listados,
  fechas obligatorias, códigos de error).
- Reorganizar la sección de endpoints **por flujo de producto**, para que los endpoints de un mismo
  flujo queden juntos y en el orden en que se usan.
- Mantener el tono, el idioma (español), el formato de tablas y la convención de nombres de DTOs
  que ya usa el documento.
- Agregar una sección breve de novedades que destaque lo incorporado desde la última versión.

## 3. Non-goals

- No documentar internals: nombres de clases de servicio, repositorios, `EntityManager`, entidades
  JPA, etc. Se conserva el resumen de arquitectura actual, que es de lectura rápida.
- No tocar código de la aplicación. El cambio es 100% documental.
- No partir el documento en `README.md` + `docs/API.md` (enfoque C, descartado).
- No agregar colecciones de Postman, ejemplos de `curl` completos ni bloques de JSON de respuesta
  completos más allá de los que ya usa el documento (formato de error, `SaleRequest`, paginado).

## 4. Anti-goals

- No inventar endpoints, parámetros ni reglas que no existan en el código.
- No dejar ninguna fila de tabla que no se haya verificado contra un controller, un DTO o un service.
- No copiar la tabla de errores completa de `ErrorCode.java` si queda ilegible: se agrupa por
  familia, cubriendo los códigos que un consumidor puede recibir en un flujo normal.

## 5. Restricciones

- Toda fila del README se verifica contra el código: `controller/**` (ruta, verbo, `@PreAuthorize`),
  `dto/request/**` y `dto/response/**` (campos y tipos), `service/**` (reglas de negocio) y
  `enums/ErrorCode.java` (keys y status).
- Los roles se expresan siempre como `SUDO` / `OWNER` / `CASHIER`.
- Las keys de error se copian textualmente de `ErrorCode` (no se traducen ni se inventan variantes).
- El documento describe comportamiento observable, no implementación.

## 6. Enfoques evaluados

| Enfoque | Qué implica | Trade-off | Estado |
|---|---|---|---|
| **A — Refresh quirúrgico** | Misma estructura, se sincronizan endpoints y reglas | Diff mínimo, pero el orden actual no agrupa por flujo | Descartado por el usuario |
| **B — Refresh + reorganización por flujos** | Se reordena en bloques de producto y se agrega sección de novedades | Diff grande, pero más navegable para el consumidor | **Elegido** |
| **C — README público + `docs/API.md`** | El README queda como overview; el detalle se muda a otro archivo | Más mantenible, pero parte el foco actual en dos archivos | Descartado por el usuario |

## 7. Estructura propuesta del README

```
1.  Título + introducción (qué es, qué expone, para quién)
2.  Tecnologías
3.  Arquitectura (Request → Controller → Service → Repository → MySQL) + formato de error
4.  Autenticación (login / logout / me, cookie token, contenido del JWT, roles, scoping 404)
5.  Convenciones de listado (page / size / sortBy / sortDir + filtros opcionales)
6.  Endpoints, por flujo:
    6.1 Autenticación y sesión
    6.2 Comercios y configuración del negocio
    6.3 Dashboard y estadísticas
    6.4 Catálogo: categorías y productos
    6.5 Clientes y cuentas corrientes
    6.6 Equipo: cajeros y puntos de venta
    6.7 Ventas
    6.8 Ofertas y descuentos
7.  Novedades de la API
8.  Códigos de error
9.  Correr el proyecto
```

Cada bloque de endpoints conserva el formato de tabla actual:
`| Método | Ruta | Roles | Request | Response |`, con la leyenda de roles (`SUDO`, `OWNER`,
`CASHIER/OWNER`, `Auth`, `Público`) y de tipos de body (`JSON`, `multipart`) que ya existe.

## 8. Inventario verificado de endpoints

Todo lo que sigue está verificado contra los controllers, DTOs y services del repo a fecha de hoy.

### 6.1 Autenticación y sesión

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| POST | `/api/auth/login` | Público | `UserLoginRequest` (JSON: `email`, `password`) | `AuthenticatedUserResponse` + cookie `token` |
| POST | `/api/auth/logout` | Auth | — | texto (`Logout successfully`) + cookie `token` expirada |
| GET | `/api/auth/me` | Auth | — | `AuthenticatedUserResponse` |

### 6.2 Comercios y configuración del negocio

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/commerces` | SUDO | — | `Page<CommerceWithOwnerResponse>` (`page`, `size`, `sortOrder`, `sortBy`) |
| POST | `/api/commerces` | SUDO | `CommerceWithOwnerRequest` (multipart: `businessLogo`) | `CommerceWithOwnerResponse` (201) |
| PUT | `/api/commerces/{commerceId}/user/{userId}` | SUDO | `CommerceWithOwnerUpdateRequest` (multipart: `businessLogo`) | `CommerceWithOwnerResponse` |
| PATCH | `/api/commerces/{id}/toggle-active` | SUDO | — | `CommerceUpdateActiveResponse` |
| GET | `/api/commerces/stats` | SUDO | — | `CommerceStatsResponse` (`total`, `active`, `inactive`) |
| GET | `/api/commerces/current` | CASHIER/OWNER | — | `CurrentCommerceResponse` |
| PATCH | `/api/commerces` | OWNER | `CommercePatchRequest` (multipart: `name`, `profitMultiplier`, `image`) | `CurrentCommerceResponse` | ← **nuevo** |
| PATCH | `/api/commerces/profit-multiplier` | OWNER | `CommerceProfitMultiplierRequest` (JSON) | `CommerceProfitMultiplierResponse` |

### 6.3 Dashboard y estadísticas

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/sales/stats` | OWNER | `?granularity=DAY\|WEEK\|MONTH` | `SalesStatsByPeriodResponse` (`salesCount`, `averageSaleValue`, `productsSoldByPeriod`, `profit`) | ← **nuevo** |
| GET | `/api/sales/top-products` | OWNER | `?limit=` (default 5) | `List<TopProductsSoldResponse>` (`productId`, `productName`, `quantity`) | ← **nuevo** |
| GET | `/api/products/low-stock` | OWNER | — | `List<ProductLowStockResponse>` (`productId`, `productName`, `minStock`, `currentStock`) | ← **nuevo** |

Semántica de los períodos (`America/Argentina/Buenos_Aires`): `DAY` = hoy; `WEEK` = últimos 7 días;
`MONTH` = últimos 30 días. `top-products` siempre mira la ventana de 30 días y solo cuenta ventas
`PAYED`. `low-stock` devuelve productos **activos** con `currentStock <= minStock`.

### 6.4 Catálogo: categorías y productos

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/categories` | CASHIER/OWNER | — | `Page<CategoryResponse>` · filtro: `categoryName` |
| GET | `/api/categories/{categoryId}` | CASHIER/OWNER | — | `CategoryResponse` |
| POST | `/api/categories` | OWNER | `CategoryRequest` (JSON: `name`, `description`) | `CategoryResponse` (201) |
| PUT | `/api/categories/{categoryId}` | OWNER | `CategoryRequest` (JSON) | `CategoryResponse` |
| PATCH | `/api/categories/{categoryId}/toggle-active` | OWNER | — | `CategoryUpdateActiveResponse` |
| GET | `/api/products` | CASHIER/OWNER | — | `Page<ProductResponse>` · filtros: `categoryId`, `searchText`, `active` |
| GET | `/api/products/with-offer` | CASHIER/OWNER | — | `Page<ProductWithOfferResponse>` · mismos filtros · incluye oferta vigente, precio con descuento y precio sugerido |
| POST | `/api/products` | OWNER | `ProductRequest` (multipart, `image` opcional) | `ProductResponse` (201) |
| PUT | `/api/products/{id}` | OWNER | `ProductUpdateRequest` (multipart) | `ProductResponse` |
| PATCH | `/api/products/{id}/toggle-active` | OWNER | — | `ProductUpdateActiveResponse` |

`ProductRequest` / `ProductUpdateRequest`: `name`, `description`, `costPrice`, `salePrice`,
`profitMultiplier` (opcional), `image` (opcional), `categoryId`, `currentStock`, `minStock`.

### 6.5 Clientes y cuentas corrientes

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/clients` | CASHIER/OWNER | — | `Page<ClientResponse>` · filtros: `searchText`, `name`, `lastName` |
| GET | `/api/clients/{dni}` | CASHIER/OWNER | — | `ClientResponse` (por DNI dentro del comercio) |
| POST | `/api/clients` | CASHIER/OWNER | `ClientRequest` (JSON) | `ClientResponse` (201) |
| PUT | `/api/clients/{id}` | CASHIER/OWNER | `ClientRequest` (JSON) | `ClientResponse` |
| GET | `/api/clients/{clientId}/pending-sales-with-details` | CASHIER/OWNER | — | `PendingSalesWithDetailsResponse` (`pendingSalesCount`, `totalPending`, `pendingSales[]`) | ← **nuevo** |

`ClientResponse` incluye el resumen de cuenta corriente: `salesCount`, `pendingSalesCount`,
`totalSpent`, `totalPending`. El DNI es único **por comercio** (`409 CLIENT_DNI_ALREADY_EXISTS`).

### 6.6 Equipo: cajeros y puntos de venta

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/cashiers` | OWNER | — | `Page<UserResponse>` · filtro: `searchText` |
| POST | `/api/cashiers` | OWNER | `CashierRequest` (JSON) | `UserResponse` (201) |
| PUT | `/api/cashiers/{id}` | OWNER | `CashierUpdateRequest` (JSON; `password` vacío = mantener la actual) | `UserResponse` |
| PATCH | `/api/cashiers/{id}/toggle-active` | OWNER | — | `CashierUpdateActiveResponse` |
| GET | `/api/sell-points` | CASHIER/OWNER | — | `Page<SellPointResponse>` |
| GET | `/api/sell-points/{sellPointId}/cashiers` | CASHIER/OWNER | — | `Page<UserResponse>` (cajeros del punto) |
| POST | `/api/sell-points` | OWNER | `SellPointRequest` (JSON: `name`, `cashierIds`) | `SellPointResponse` (201) |
| PUT | `/api/sell-points/{sellPointId}` | OWNER | `SellPointUpdateRequest` (JSON) | `SellPointResponse` |
| PATCH | `/api/sell-points/{sellPointId}/toggle-active` | OWNER | — | `SellPointUpdateActiveResponse` |

Un cajero pertenece a un solo punto de venta; el punto se toma del JWT al registrar ventas, nunca
del request.

### 6.7 Ventas

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/sales` | CASHIER/OWNER | — | `Page<SaleSummaryResponse>` · filtros: `clientId`, `status`, `paymentMethod`, `searchText` (observaciones o nombre del cliente) |
| GET | `/api/sales/{saleId}` | Auth | — | `SaleResponse` (detalle con todas las líneas) |
| POST | `/api/sales` | CASHIER/OWNER | `SaleRequest` (JSON) | `SaleSummaryResponse` (201) |
| PATCH | `/api/sales/{id}/mark-paid` | CASHIER/OWNER | — | `SaleUpdateStatusResponse` |

Reglas de `POST /api/sales`:

- Los precios, el subtotal y el total se calculan **siempre en el servidor**; el request solo envía
  productos y cantidades. Los ítems repetidos del mismo producto se consolidan.
- Valores de `paymentMethod`: `CASH`, `CARD`, `TRANSFER`, `CREDIT_ACCOUNT`. El fiado
  (`CREDIT_ACCOUNT`) genera la venta con status `PENDING`; el resto, `PAYED`.
- `discountType` (`PERCENTAGE` / `FIXED_AMOUNT`) y `discount` van juntos; un descuento fijo no puede
  superar el subtotal (`400 DISCOUNT_EXCEEDS_SUBTOTAL`) y un descuento porcentual no puede superar
  el 100% (`400 DISCOUNT_PERCENTAGE_EXCEEDS_100`).
- **Si el método de pago de la venta tiene una oferta `ACTIVE`, el descuento de esa oferta prevalece
  sobre el descuento manual del request** y el manual se ignora.
- El descuento de stock es todo-o-nada: si algún ítem no pasa la validación (producto inactivo o
  sin stock), no se descuenta ni se registra nada (`409 PRODUCT_INACTIVE`, `409 INSUFFICIENT_STOCK`).
- `mark-paid` solo admite la transición `PENDING → PAYED` (`409 SALE_NOT_PENDING` en otro caso).

### 6.8 Ofertas y descuentos

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/offers` | CASHIER/OWNER | — | `Page<OfferResponse>` · filtros: `status`, `paymentMethod`, `offerType` |
| GET | `/api/offers/payment-methods` | CASHIER/OWNER | — | `List<OfferPaymentMethodResponse>` (métodos de pago con oferta vigente) |
| POST | `/api/offers` | OWNER | `OfferRequest` (JSON) | `OfferResponse` (201) |
| PUT | `/api/offers/{id}` | OWNER | `OfferUpdateRequest` (JSON) | `OfferResponse` |
| PUT | `/api/offers/{id}/products` | OWNER | `OfferProductIdsRequest` (JSON) | `200 OK` (asigna productos a la oferta) |

Reglas de ofertas:

- `OfferRequest` / `OfferUpdateRequest`: `value`, `discountType`, `startDate`, `endDate`,
  `paymentMethods` (opcional). Las fechas son **obligatorias** y en formato ISO-8601 (`Instant`).
- El `value` se interpreta según `discountType`: con `PERCENTAGE` es un porcentaje y no puede superar
  el 100% (`400 DISCOUNT_PERCENTAGE_EXCEEDS_100`); con `FIXED_AMOUNT` es un monto y no tiene máximo.
- El `status` de la oferta se **deriva de las fechas**: `SCHEDULED` (todavía no empieza), `ACTIVE`
  (dentro de la ventana) y `EXPIRED` (ya venció). No se envía en el request.
- El `offerType` también se deriva: con al menos un método de pago es `PAYMENT_METHOD`, sin métodos
  es `PRODUCT`.
- No se pueden superponer ofertas del mismo alcance: si el período se cruza con otra oferta que
  comparte algún método de pago (o, entre ofertas de producto, con el mismo `value` y
  `discountType`), responde `409 OFFER_OVERLAP`.
- Una oferta con productos asignados no puede tener métodos de pago
  (`409 OFFER_HAS_PRODUCTS`), y una oferta con métodos de pago no puede llevar productos
  (`400 OFFER_HAS_PAYMENT_METHODS`).
- `GET /api/offers/payment-methods` devuelve, por cada método de pago con oferta vigente, el método
  de pago, el valor y el tipo de descuento. Es el endpoint que la vista de venta usa para mostrar el
  descuento aplicado en tiempo real.

## 9. Novedades de la API

Sección breve (una lista o una tabla corta) que resume lo incorporado desde la última versión
documentada, para que el consumidor no tenga que diffear el README completo:

- Estadísticas de venta por período (`GET /api/sales/stats`).
- Ranking de productos más vendidos (`GET /api/sales/top-products`).
- Alertas de stock bajo (`GET /api/products/low-stock`).
- Ventas pendientes de un cliente con detalle (`GET /api/clients/{clientId}/pending-sales-with-details`).
- Actualización parcial del comercio (`PATCH /api/commerces`).
- Resumen de cuenta corriente en la respuesta de clientes (`salesCount`, `pendingSalesCount`,
  `totalSpent`, `totalPending`).
- Filtros por texto y nombre en el listado de clientes y en el de cajeros.
- Ofertas con ventana de fechas (`startDate`/`endDate`) y aplicación automática del descuento de la
  oferta activa sobre la venta.

## 10. Códigos de error

Se reorganiza la tabla actual por familia (autenticación/autorización, validación, not found,
conflicto, Discounts, ofertas, archivos, interno), cubriendo:

- `VALIDATION_ERROR` (400, con `fields[]`), `INVALID_REQUEST_BODY` (400), `INVALID_PARAMETER` (400),
  `MISSING_PARAMETER` (400), `METHOD_NOT_ALLOWED` (405), `UNSUPPORTED_MEDIA_TYPE` (415).
- `INVALID_CREDENTIALS` (401), `UNAUTHORIZED` (401), `ACCOUNT_DISABLED` (403), `ACCESS_DENIED` (403).
- `*_NOT_FOUND` (404), incluyendo `PRODUCTS_OFFER_NOT_FOUND`.
- `*_ALREADY_EXISTS` (409): email, DNI por comercio, nombre de categoría, nombre de punto de venta,
  dirección y CUIT de comercio.
- `INSUFFICIENT_STOCK` (409), `PRODUCT_INACTIVE` (409), `SALE_NOT_PENDING` (409),
  `DATA_INTEGRITY_VIOLATION` (409).
- `INVALID_DISCOUNT` (400), `INVALID_DISCOUNT_PREFIX_ZERO` (400), `DISCOUNT_WITHOUT_TYPE` (400),
  `TYPE_WITHOUT_DISCOUNT` (400), `DISCOUNT_EXCEEDS_SUBTOTAL` (400),
  `DISCOUNT_PERCENTAGE_EXCEEDS_100` (400), `PRODUCT_SALE_PRICE_BELOW_COST` (400).
- `INVALID_DATE` (400), `OFFER_OVERLAP` (409), `OFFER_HAS_PAYMENT_METHODS` (400),
  `OFFER_HAS_PRODUCTS` (409).
- `INVALID_FILE_TYPE` (400), `FILE_TOO_LARGE` (413), `FILE_UPLOAD_FAILED` (500),
  `FILE_DELETE_FAILED` (500), `FILE_PROCESSING_FAILED` (500).
- `INTERNAL_ERROR` (500).

## 11. Decisiones tomadas (no requieren confirmación)

- Se mantiene el idioma español, el tono y el formato de tablas del documento actual.
- Se conserva el bloque de arquitectura tal como está: es correcto y sirve de contexto rápido.
- La sección de novedades se ubica **después** de los endpoints, antes de errores: el lector que
  busca "qué cambió" la encuentra sin pasar por el catálogo completo.
- La sección "Correr el proyecto" se conserva, con los valores reales de `application.properties`
  (MySQL `gestion` / `root` / `admin`; MinIO `localhost:9000`, consola `:9001`, `admin`/`admin123`,
  bucket `gestionbucket`) y el usuario bootstrap `admin@admin.com` / `admin123`.
- No se agregan ejemplos de `curl` ni documentación de `docker-compose` más allá de la línea actual.
- No se documentan campos de response uno por uno salvo cuando aportan información no evidente
  (ej. `ClientResponse`, `PendingSalesWithDetailsResponse`, `ProductLowStockResponse`).

## 12. Fuera de alcance (lo dejo señalado, no lo toco)

- El comentario de `OfferCreatorService` (líneas 40-42) afirma que "las ofertas aún no se aplican a
  la venta", pero `SaleCreatorService` sí las aplica. Es un comentario desactualizado en el código,
  no del README: se reporta, no se modifica en este trabajo.
- `controller/user/UserPutController` no declara ningún handler: es un archivo vacío y no aporta
  endpoints. No se documenta.

## 12 bis. Decisiones posteriores al builder check

El builder check (revisión independiente del diseño contra el código) levantó seis puntos. Todos
quedan resueltos con evidencia del repo:

1. **Las dos ofertas se acumulan.** `SaleDetailsValidatorService:44-56` calcula el precio unitario
   de cada línea con el descuento de la oferta de producto (`offerType = PRODUCT`) vigente, y el
   subtotal sale de esos precios ya descontados; después `SaleCreatorService:64-74` aplica el
   descuento del método de pago **sobre ese subtotal**. O sea: la oferta de producto baja el precio
   de la línea y la oferta de método de pago baja el total de la venta, y **se acumulan**. Se
   documenta, porque `GET /api/products/with-offer` ya expone ese precio descontado.
   Además: `PUT /api/offers/{id}/products` **reemplaza** el conjunto de productos asignado
   (`OfferAssignerProductService:38-52`); al crear una oferta la fecha de inicio no puede estar en
   el pasado y la de fin no puede ser anterior a la de inicio (`400 INVALID_DATE`), mientras que al
   actualizarla se puede conservar la fecha de inicio original aunque ya haya pasado.
2. **Hay tres contratos de paginación distintos.** Convención general `page` / `size` / `sortBy` /
   `sortDir`, con dos excepciones que se dicen explícitamente en la fila del endpoint: `GET
   /api/commerces` usa `sortOrder` (default `DESC`) en lugar de `sortDir`, y `GET /api/cashiers`
   binds `Pageable` de Spring Data, así que toma `sort=campo,direccion` (default `size=20`, sin
   orden por defecto) en lugar de `sortBy`/`sortDir`. Los defaults por recurso van en una tabla
   chica en la sección de convenciones.
3. **El bloque JSON de paginado estaba desactualizado.** No hay serializador custom de `Page` ni
   `spring.data.web.pageable.serialization-mode` en el repo, así que aplica la serialización directa
   de Spring Data: `content` + metadatos (`totalElements`, `totalPages`, `size`, `number`, `first`,
   `last`, `numberOfElements`, `empty`, `sort`, `pageable`), sin envoltorio `page`.
4. **Se documentan las validaciones de request** que no son evidentes por el nombre del campo:
   `OfferRequest` (`value` positivo, fechas obligatorias, `Instant`/ISO-8601), `SaleRequest`
   (`details` no vacío, `discount` > 0, `observations` ≤ 200), `CommercePatchRequest` (`name` 2–100,
   `profitMultiplier` positivo), `CashierUpdateRequest` (`password` vacío = mantener) y el tope de
   10 MB por archivo (`413 FILE_TOO_LARGE`).
5. **La sección de autenticación se completa y se corrige.** El JWT dura 24 h (`jwt.expiration =
   86400000`) y lleva `sub` (email), `name`, `userId`, `commerceId`, `role`, `active`, `sellPointId`.
   La cookie es `HttpOnly`, `Path=/`, `Secure=false`, 24 h. `POST /api/auth/logout` **solo expira la
   cookie**: no hay revocación del token del lado del servidor, así que se retira la frase "invalida
   la sesión". Se distingue `401 UNAUTHORIZED` (sin cookie o token inválido) de `403 ACCESS_DENIED`
   (rol insuficiente) y se aclara que `GET /api/auth/me` devuelve `403 ACCOUNT_DISABLED` si el
   comercio está deshabilitado.
6. **La tabla de errores enumera las keys concretas**, sin comodines `*_NOT_FOUND` /
   `*_ALREADY_EXISTS`: el consumidor switchea sobre `code`, y el comodín no es accionable.
7. **"Novedades de la API" se ancla al contrato documentado anterior, no a commits.** El cambio de
   ofertas (ventana de fechas y aplicación en la venta) es una **corrección** de un README que ya
   afirmaba lo contrario, así que no va en Novedades: vive en las secciones de Ventas y Ofertas.
   Novedades queda con lo que sí es nuevo para el consumidor: estadísticas, top de productos, stock
   bajo, ventas pendientes por cliente, patch de comercio, resumen de cuenta corriente y filtros
   nuevos de clientes y cajeros.

## 13. Cómo verificar el resultado

1. Cada endpoint del README tiene un controller correspondiente (y viceversa): se recorre
   `controller/**` y se coteja contra la sección 8 de este diseño.
2. Cada key de error mentioned en el README existe en `ErrorCode.java` con ese status.
3. Cada nombre de DTO citado existe en `dto/request/**` o `dto/response/**`.
4. Los markdown quedan bien formados (tablas con el mismo número de columnas, links internos
   consistentes).
5. `git diff README.md` se lee de punta a punta buscando afirmaciones no verificables.

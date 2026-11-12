# API de gestión de stock y control de ventas

Sistema de gestión de stock y ventas para comercios (proyecto final de la materia Programación IV —
UTN TUP). Expone una API REST para administrar comercios, productos, categorías, clientes, puntos de
venta, ventas y ofertas. Este documento describe la API pública — endpoints, roles, payloads y
errores — para quien la consume.

## Tecnologías

- **Java 25** + **Spring Boot 4.1** (Spring Security con JWT, Spring Data JPA)
- **MySQL 8** como base de datos (schema generado por Hibernate `ddl-auto=update`)
- **MinIO** (S3 compatible) para el almacenamiento de imágenes de productos y logos de comercio
- **Lombok** para reducir boilerplate, **Maven Wrapper** para el build
- Autenticación por **JWT en cookie `token`** (no se usa header `Authorization`)

## Arquitectura

Cada request sigue la cadena:

```
Request → Controller → Service → Repository → MySQL
```

- **Controller** (`controller/<recurso>/...`): un archivo por endpoint, a cargo del mapeo HTTP,
  la validación de rol (`@PreAuthorize`) y la extracción del usuario autenticado del JWT.
- **Service** (`service/<recurso>/...`): un archivo por caso de uso, con toda la lógica de negocio
  (scoping por comercio, validaciones, cálculo de montos).
- **Repository** (`repository/Jpa*Repository`): interfaces Spring Data JPA con queries derivadas o
  JPQL.
- **DTOs**: requests y responses son *records* Java (`dto/request`, `dto/response`).
- **Errores**: todas las respuestas de error comparten un formato único:

```json
{
  "statusCode": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "Product with id 5 not found",
  "fields": [{ "field": "name", "message": "must not be blank" }]
}
```

`code` es una key estable para manejar el error en el cliente (los mensajes son en inglés, para
logs). `fields` solo aparece en errores de validación (`VALIDATION_ERROR`), y también cuando un
parámetro de un método del controller no cumple sus restricciones (por ejemplo `limit` con un
`@Min(1)`): se reporta igual, con el nombre del parámetro en `field`.

## Autenticación

1. `POST /api/auth/login` con `{ "email", "password" }` → devuelve el usuario y setea la **cookie
   `token`** con el JWT. Todas las demás requests deben incluir esa cookie (`credentials: include`
   en el navegador). La cookie es `HttpOnly`, `Path=/` y vive 24 horas (`Max-Age=86400`).
2. El JWT dura 24 horas y lleva los claims `sub` (email), `name`, `userId`, `commerceId`, `role`,
   `active` y `sellPointId` (este último es `null` para OWNER/SUDO). El backend confía en ese
   contenido **salvo el flag `active`**: en cada request autenticado se revalida contra la base
   (una query mínima que trae el `active` del usuario y del comercio), así que desactivar un
   usuario le corta el acceso de inmediato, sin esperar a que expire su token.
3. `POST /api/auth/logout` expira la cookie. **No hay revocación del token en el servidor**: un JWT
   ya emitido sigue siendo válido hasta su expiración, así que el logout es efectivamente local.
   Como tampoco es un endpoint público, requiere una cookie válida (`401` si no la hay).

Códigos de estado de autenticación:

- `401 INVALID_CREDENTIALS`: email o contraseña incorrectos en el login (mismo error en ambos casos,
  para no filtrar si el email existe).
- `401 UNAUTHORIZED`: no hay cookie `token`, o el token es inválido o expiró.
- `403 ACCESS_DENIED`: el rol del usuario no alcanza para el endpoint.
- `403 ACCOUNT_DISABLED`: el **usuario** está desactivado (su `toggle-active`) o el **comercio**
  del usuario está deshabilitado. Un usuario desactivado no puede iniciar sesión, y un token que
  se emitió antes de desactivarlo deja de servir en el siguiente request.

CORS: el origen habilitado es `http://localhost:5173` (el front de desarrollo), con credenciales y
`Set-Cookie` expuesto. Solo `POST /api/auth/login` y los preflight `OPTIONS` son públicos; el resto
de las rutas cae en deny-all.

### Roles

| Rol | Alcance |
|---|---|
| `SUDO` | Administrador global: gestiona comercios |
| `OWNER` | Dueño de un comercio: gestiona su catálogo, cajeros, puntos de venta, ventas y ofertas |
| `CASHIER` | Cajero: consulta catálogo/clientes y registra ventas |

Todo dato está **scopeado al comercio del JWT**: consultar un recurso de otro comercio responde
`404` (nunca `403`, para no filtrar su existencia). El `403` queda reservado para el rol
insuficiente, la cuenta o el comercio deshabilitados, y para un usuario sin comercio en el JWT
(el SUDO).

## Convenciones de listado

Los endpoints de listado aceptan `page` (0-based), `size`, `sortBy` y `sortDir` (`asc`/`desc`) y
responden con un `Page` de Spring Data serializado directo, **sin envoltorio**:

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

(junto a esos metadatos viajan también `numberOfElements`, `empty`, `sort` y `pageable`).

Los campos de filtro son opcionales; se envían como query params (`?status=PENDING&searchText=marta`).

Ningún listado filtra por `active` salvo que el endpoint lo pida explícitamente: `GET
/api/categories`, `GET /api/cashiers` y `GET /api/sell-points` devuelven también los dados de baja,
y en `GET /api/products` el filtro `active` es opcional (si se omite, trae los inactivos).

Defaults de paginación y orden por recurso:

| Listado | Parámetros de orden | `size` default | Orden default |
|---|---|---|---|
| `/api/commerces` | `sortBy` + `sortOrder` (**excepción 1**) | 5 | `updatedAt` / `DESC` |
| `/api/cashiers` | `sort=campo,direccion` (**excepción 2**) | 20 | sin orden |
| `/api/clients` | `sortBy` + `sortDir` | 5 | `updatedAt` / `asc` |
| `/api/categories` | `sortBy` + `sortDir` | 10 | `updatedAt` / `asc` |
| `/api/products` y `/api/products/with-offer` | `sortBy` + `sortDir` | 10 | `updatedAt` / `desc` |
| `/api/sell-points` | `sortBy` + `sortDir` | 10 | `updatedAt` / `desc` |
| `/api/sell-points/{sellPointId}/cashiers` | `sortBy` + `sortDir` | 10 | `name` / `asc` |
| `/api/sales` | `sortBy` + `sortDir` | 10 | `createdAt` / `desc` |
| `/api/offers` | `sortBy` + `sortDir` | 10 | `endDate` / `desc` |

1. `GET /api/commerces` usa **`sortOrder`** (no `sortDir`), con valores `ASC`/`DESC`.
2. `GET /api/cashiers` bindea el `Pageable` de Spring Data, así que el orden se pasa con
   **`sort=campo,direccion`** (ej.: `?sort=name,asc`) y no con `sortBy`/`sortDir` — enviarle esos
   parámetros no falla, pero el orden se ignora.

### Validación de la paginación

Los listados que arman el `Pageable` a mano (`/api/clients`, `/api/categories`, `/api/products`,
`/api/products/with-offer`, `/api/sell-points`, `/api/sell-points/{id}/cashiers`, `/api/sales`,
`/api/offers`, `/api/commerces`) validan sus parámetros:

| Parámetro | Valores válidos | Si viene inválido |
|---|---|---|
| `page` | `>= 0` | `400 INVALID_PARAMETER` |
| `size` | `1` a `100` | `400 INVALID_PARAMETER` |
| `sortBy` | una propiedad del recurso (allow-list) | `400 INVALID_PARAMETER` |
| `sortDir` / `sortOrder` | `asc` / `desc` | `400 INVALID_PARAMETER` |

Los parámetros fuera de rango **se rechazan, no se clampean**: si pedís `size=1000` recibís un
`400` y no una página de 100 elementos. El `size` tiene un tope porque la query agregada del
resumen de cuenta de los clientes expande `IN :clientIds` a un placeholder por id y MySQL corta en
65.535 placeholders.

`GET /api/cashiers` no pasa por esta validación: bindea el `Pageable` de Spring Data, que ya
aplica su propio `max-page-size`.

## Endpoints

> **Roles**: `SUDO` = solo administrador · `OWNER` = solo dueño · `CASHIER`/`OWNER` = ambos ·
> `Auth` = cualquier usuario autenticado · `Público` = sin sesión.
> `JSON` = body JSON · `multipart` = `multipart/form-data` (incluye archivo de imagen/logo).
> Los endpoints marcados con **nuevo** no existían en la versión anterior de este documento.

### Autenticación y sesión

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| POST | `/api/auth/login` | **Público** | `UserLoginRequest` (JSON: `email`, `password`) | `AuthenticatedUserResponse` + cookie `token` |
| POST | `/api/auth/logout` | Auth | — | texto (`Logout successfully`) + cookie `token` expirada |
| GET | `/api/auth/me` | Auth | — | `AuthenticatedUserResponse` |

### Comercios y configuración del negocio

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/commerces` | SUDO | — | `Page<CommerceWithOwnerResponse>` |
| POST | `/api/commerces` | SUDO | `CommerceWithOwnerRequest` (multipart) | `CommerceWithOwnerResponse` (201) |
| PUT | `/api/commerces/{commerceId}/user/{userId}` | SUDO | `CommerceWithOwnerUpdateRequest` (multipart) | `CommerceWithOwnerResponse` |
| PATCH | `/api/commerces/{id}/toggle-active` | SUDO | — | `CommerceUpdateActiveResponse` |
| GET | `/api/commerces/stats` | SUDO | — | `CommerceStatsResponse` (`total`, `active`, `inactive`) |
| GET | `/api/commerces/current` | CASHIER/OWNER | — | `CurrentCommerceResponse` |
| PATCH | `/api/commerces` | OWNER | `CommercePatchRequest` (multipart) | `CurrentCommerceResponse` **nuevo** |
| PATCH | `/api/commerces/profit-multiplier` | OWNER | `CommerceProfitMultiplierRequest` (JSON) | `CommerceProfitMultiplierResponse` |

- `GET /api/commerces` lista los **OWNER** con su comercio (una fila por dueño).
- `CommerceWithOwnerRequest` (crea comercio + owner juntos): `username`, `email`, `password`,
  `phoneNumber`, `name`, `address`, `cuit`, `businessLogo` (opcional), `profitMultiplier` (opcional,
  por defecto `1.0`). `CommerceWithOwnerUpdateRequest` es igual, sin `password`: el PUT no permite
  cambiar la contraseña del owner.
- El email, el CUIT y la dirección del comercio son únicos (`409 EMAIL_ALREADY_EXISTS`,
  `409 COMMERCE_CUIT_ALREADY_EXISTS`, `409 COMMERCE_ADDRESS_ALREADY_EXISTS`).
- `PATCH /api/commerces` **nuevo**: actualización parcial del comercio del OWNER autenticado. Todos
  los campos son opcionales y solo se actualizan los que llegan: `name` (2–100 caracteres),
  `profitMultiplier` (positivo), `image`. Si mandás `image`, **reemplaza** el logo anterior.
  Devuelve el comercio tal como lo ve `GET /api/commerces/current`.
- `PATCH /api/commerces/profit-multiplier` solo cambia el multiplicador de ganancia del comercio.

### Dashboard y estadísticas

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/sales/stats` | OWNER | `?granularity=` | `SalesStatsByPeriodResponse` **nuevo** |
| GET | `/api/sales/top-products` | OWNER | `?limit=` (default 5) | `List<TopProductsSoldResponse>` **nuevo** |
| GET | `/api/products/low-stock` | OWNER | — | `List<ProductLowStockResponse>` **nuevo** |

- `granularity` es **obligatorio** (si falta, `400 MISSING_PARAMETER`): `DAY` (hoy), `WEEK` (últimos
  7 días) o `MONTH` (últimos 30 días), calculados en la zona `America/Argentina/Buenos_Aires`.
  `SalesStatsByPeriodResponse` devuelve `salesCount`, `averageSaleValue`, `productsSoldByPeriod`
  (lista de `period` + `quantity`) y `profit`.
- Todas las estadísticas cuentan **solo ventas `PAYED`**. Ojo con `profit`: es la **suma de los
  totales** de esas ventas (ingresos del período), no el margen contra el costo.
- `productsSoldByPeriod` agrupa según la granularidad: por hora (`HH:00`), por día (`yyyy-MM-dd`) o
  en bloques de 7 días. Ojo con `MONTH`: los bloques arrancan en el día 30 de la ventana y `period`
  es la fecha de **inicio** del bloque, no un lunes de calendario.
- `top-products`: ranking por cantidad vendida, contando **solo ventas `PAYED`** de los últimos 30
  días (ventana fija, no depende de ninguna granularidad). Cada item trae `productId`, `productName`
  y `quantity`.
- `low-stock`: productos **activos** con `currentStock` menor o igual que `minStock`, con `productId`,
  `productName`, `minStock` y `currentStock`. No está paginado ni tiene un orden definido.

### Catálogo: categorías y productos

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/categories` | CASHIER/OWNER | — | `Page<CategoryResponse>` · filtro: `categoryName` |
| GET | `/api/categories/{categoryId}` | CASHIER/OWNER | — | `CategoryResponse` |
| POST | `/api/categories` | OWNER | `CategoryRequest` (JSON) | `CategoryResponse` (201) |
| PUT | `/api/categories/{categoryId}` | OWNER | `CategoryRequest` (JSON) | `CategoryResponse` |
| PATCH | `/api/categories/{categoryId}/toggle-active` | OWNER | — | `CategoryUpdateActiveResponse` |
| GET | `/api/products` | CASHIER/OWNER | — | `Page<ProductResponse>` · filtros: `categoryId`, `searchText`, `active` |
| GET | `/api/products/with-offer` | CASHIER/OWNER | — | `Page<ProductWithOfferResponse>` · mismos filtros · incluye oferta vigente, precio con descuento y precio sugerido |
| POST | `/api/products` | OWNER | `ProductRequest` (multipart) | `ProductResponse` (201) |
| PUT | `/api/products/{id}` | OWNER | `ProductUpdateRequest` (multipart) | `ProductResponse` |
| PATCH | `/api/products/{id}/toggle-active` | OWNER | — | `ProductUpdateActiveResponse` |

- `CategoryRequest`: `name` (hasta 50), `description` (hasta 200). El nombre es único **por
  comercio** (`409 CATEGORY_NAME_ALREADY_EXISTS`). El listado acepta `?categoryName=` (búsqueda
  parcial, case-insensitive) y calcula `productCount` (productos de la categoría); el detalle por id
  devuelve `productCount` en `0`.
- `ProductRequest` / `ProductUpdateRequest`: `name` (hasta 100), `description` (hasta 255),
  `costPrice`, `salePrice`, `profitMultiplier` (opcional), `image` (opcional), `categoryId`,
  `currentStock`, `minStock`. Ambos son `multipart` por la imagen; el resto de los campos viaja como
  partes de texto.
- `salePrice` y `costPrice` deben ser positivos, igual que `currentStock` y `minStock` (no se puede
  crear un producto con stock 0). `salePrice` no puede ser menor que `costPrice`
  (`400 PRODUCT_SALE_PRICE_BELOW_COST`).
- En `GET /api/products` y `GET /api/products/with-offer`, `searchText` busca en el nombre, la
  descripción y el nombre de la categoría. `suggestedPrice` se calcula como `costPrice ×
  profitMultiplier` (el del producto o, si no viene, el del comercio).
- Los archivos suben a MinIO con un tope de 10 MB (`413 FILE_TOO_LARGE`); solo se aceptan imágenes
  `jpg`, `jpeg`, `png` y `webp` (detección por contenido, `400 INVALID_FILE_TYPE` en otro caso).

### Clientes y cuentas corrientes

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/clients` | CASHIER/OWNER | — | `Page<ClientResponse>` · filtros: `searchText`, `name`, `lastName` |
| GET | `/api/clients/{dni}` | CASHIER/OWNER | — | `ClientResponse` (por DNI dentro del comercio) |
| POST | `/api/clients` | CASHIER/OWNER | `ClientRequest` (JSON) | `ClientResponse` (201) |
| PUT | `/api/clients/{id}` | CASHIER/OWNER | `ClientRequest` (JSON) | `ClientResponse` |
| GET | `/api/clients/{clientId}/pending-sales-with-details` | CASHIER/OWNER | — | `PendingSalesWithDetailsResponse` **nuevo** |

- **Ojo con los identificadores**: el detalle por cliente usa el **DNI** (`/api/clients/{dni}`), la
  actualización usa el **id de cliente** (`/api/clients/{id}`) y las ventas pendientes también
  (`/api/clients/{clientId}/...`). No son intercambiables.
- **Los filtros de búsqueda no son independientes**: `searchText` busca (parcial, case-insensitive)
  contra nombre, apellido o DNI. Si no mandás `searchText`, los parámetros `name` y `lastName` se
  **unen en un único texto** que se busca igual contra nombre, apellido o DNI: mandar
  `?name=Juan&lastName=Perez` busca el literal "Juan Perez" y no matchea un cliente que se llame
  Juan Pérez. Y si mandás `searchText`, los otros dos se ignoran.
- `ClientRequest`: `name`, `lastName`, `phoneNumber` y `dni` son obligatorios y admiten entre 2 y 20
  caracteres; `address` es obligatoria y admite entre 2 y 50.
- El resumen de cuenta corriente (`salesCount`, `pendingSalesCount`, `totalSpent`, `totalPending`)
  viene calculado en **todos** los endpoints que devuelven un cliente (listado, detalle, alta y
  edición). Un cliente sin ventas devuelve `0` en los contadores y `0.00` en los montos.
- `GET /api/clients/{clientId}/pending-sales-with-details` **nuevo** devuelve `pendingSalesCount`,
  `totalPending` y `pendingSales` (lista de `SaleResponse`), ordenadas por fecha de creación
  (ascendente). Ojo:
  **no valida que el cliente exista** — si no tiene ventas pendientes (o no existe) responde
  `pendingSalesCount: 0`, `totalPending: 0` y `pendingSales: []`, no `404`.
- El DNI es único **por comercio** (`409 CLIENT_DNI_ALREADY_EXISTS`).

### Equipo: cajeros y puntos de venta

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/cashiers` | OWNER | — | `Page<UserResponse>` · filtro: `searchText` |
| POST | `/api/cashiers` | OWNER | `CashierRequest` (JSON) | `UserResponse` (201) |
| PUT | `/api/cashiers/{id}` | OWNER | `CashierUpdateRequest` (JSON) | `UserResponse` |
| PATCH | `/api/cashiers/{id}/toggle-active` | OWNER | — | `CashierUpdateActiveResponse` |
| GET | `/api/sell-points` | CASHIER/OWNER | — | `Page<SellPointResponse>` |
| GET | `/api/sell-points/{sellPointId}/cashiers` | CASHIER/OWNER | — | `Page<UserResponse>` (cajeros del punto) |
| POST | `/api/sell-points` | OWNER | `SellPointRequest` (JSON) | `SellPointResponse` (201) |
| PUT | `/api/sell-points/{sellPointId}` | OWNER | `SellPointUpdateRequest` (JSON) | `SellPointResponse` |
| PATCH | `/api/sell-points/{sellPointId}/toggle-active` | OWNER | — | `SellPointUpdateActiveResponse` |

- `CashierRequest`: `name`, `email`, `password` (3–50), `phoneNumber` (10–20). Los cajeros son
  usuarios (`role = CASHIER`) del comercio del OWNER autenticado.
- `CashierUpdateRequest`: igual, pero `password` vacío significa **mantener la actual**.
- `SellPointRequest` / `SellPointUpdateRequest`: `name` (hasta 100) y `cashierIds` (lista opcional de
  ids de cajeros). El nombre es único por comercio (`409 SELL_POINT_NAME_ALREADY_EXISTS`). Un cajero
  reasignado a otro punto deja de pertenecer al anterior. Un `cashierIds` inexistente o de otro
  comercio responde `404 USER_NOT_FOUND`.
- En el PUT, el `cashierIds` **reemplaza** el conjunto de cajeros del punto: los que no vengas en la
  lista quedan desasignados (no se mergean).
- `GET /api/sell-points` devuelve **también los puntos inactivos** (no filtra por `active`).
- Un cajero pertenece a un solo punto de venta; el punto se toma del JWT al registrar ventas, nunca
  del request.

### Ventas

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/sales` | CASHIER/OWNER | — | `Page<SaleSummaryResponse>` · filtros: `clientId`, `status`, `paymentMethod`, `searchText` (observaciones o nombre del cliente) |
| GET | `/api/sales/{saleId}` | CASHIER/OWNER | — | `SaleResponse` (detalle con todas las líneas) |
| POST | `/api/sales` | CASHIER/OWNER | `SaleRequest` (JSON) | `SaleSummaryResponse` (201) |
| PATCH | `/api/sales/{id}/mark-paid` | CASHIER/OWNER | — | `SaleUpdateStatusResponse` |

- `GET /api/sales/{saleId}` está scopeado al comercio del JWT, así que un usuario sin comercio (el
  SUDO) obtiene `404 SALE_NOT_FOUND`.
- Una venta sin `clientId` queda registrada como "Consumidor final".

`SaleRequest`:

```json
{
  "paymentMethod": "CASH",
  "details": [{ "productId": 3, "quantity": 2 }],
  "clientId": 7,
  "discountType": "PERCENTAGE",
  "discount": 10,
  "observations": "..."
}
```

- Los precios, el subtotal y el total se calculan **siempre en el servidor**; el request solo envía
  productos y cantidades (`quantity` mínimo 1). Los ítems repetidos del mismo producto se consolidan
  sumando cantidades. `details` no puede venir vacío y `observations` admite hasta 200 caracteres.
- Valores de `paymentMethod`: `CASH`, `CARD`, `TRANSFER`, `CREDIT_ACCOUNT`. El fiado
  (`CREDIT_ACCOUNT`) genera la venta con status `PENDING`; el resto, `PAYED`.
- `discountType` (`PERCENTAGE` / `FIXED_AMOUNT`) y `discount` van juntos; `discount` debe ser mayor
  que cero, un descuento fijo no puede superar el subtotal (`400 DISCOUNT_EXCEEDS_SUBTOTAL`) y un
  descuento porcentual no puede superar el 100% (`400 DISCOUNT_PERCENTAGE_EXCEEDS_100`).
- **Si el método de pago de la venta tiene una oferta vigente, el descuento de esa oferta prevalece
  sobre el descuento manual del request** (el manual se ignora).
- **Las dos ofertas se acumulan**: la oferta de producto baja el precio unitario de la línea y el
  subtotal ya sale de esos precios descontados; el descuento del método de pago se aplica después,
  sobre ese subtotal.
- El descuento de stock es todo-o-nada: si algún ítem no pasa la validación (producto inactivo o
  sin stock), no se descuenta ni se registra nada (`409 PRODUCT_INACTIVE`, `409 INSUFFICIENT_STOCK`).
- El punto de venta sale del JWT (`sellPointId`) y puede ser `null`; nunca se envía en el request.
- `mark-paid` solo admite la transición `PENDING → PAYED` (`409 SALE_NOT_PENDING` en otro caso).

### Ofertas y descuentos

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/offers` | CASHIER/OWNER | — | `Page<OfferResponse>` · filtros: `status`, `paymentMethod`, `offerType` |
| GET | `/api/offers/payment-methods` | CASHIER/OWNER | — | `List<OfferPaymentMethodResponse>` (métodos de pago con oferta vigente) |
| POST | `/api/offers` | OWNER | `OfferRequest` (JSON) | `OfferResponse` (201) |
| PUT | `/api/offers/{id}` | OWNER | `OfferUpdateRequest` (JSON) | `OfferResponse` |
| PUT | `/api/offers/{id}/products` | OWNER | `OfferProductIdsRequest` (JSON) | `200 OK` (asigna productos a la oferta) |

- `OfferRequest` / `OfferUpdateRequest`: `value` (positivo), `discountType`, `startDate`, `endDate`
  y `paymentMethods` (opcional). Las fechas son **obligatorias**, en formato ISO-8601 (`Instant`).
- El `value` de la oferta se interpreta según `discountType`: con `PERCENTAGE` es un porcentaje y no
  puede superar el 100% (`400 DISCOUNT_PERCENTAGE_EXCEEDS_100`); con `FIXED_AMOUNT` es un monto y no
  tiene un máximo definido.
- Fechas: al crear, la fecha de inicio no puede estar en el pasado y la de fin no puede ser anterior
  a la de inicio (`400 INVALID_DATE`); al actualizar, se puede conservar la fecha de inicio original
  aunque ya haya pasado.
- El `status` de la oferta se **deriva de las fechas**: `SCHEDULED` (todavía no empieza), `ACTIVE`
  (dentro de la ventana) y `EXPIRED` (ya venció). No se envía en el request y se puede filtrar con
  `?status=`.
- Cada oferta tiene un `offerType` derivado de sus `paymentMethods`: con al menos un método de pago
  es `PAYMENT_METHOD` (se aplica sobre el total de la venta) y sin métodos es `PRODUCT` (se aplica
  sobre el precio unitario de los productos asignados). Se filtra con `?offerType=` y se refleja en
  `OfferResponse.offerType`.
- No se pueden superponer ofertas del mismo alcance: si el período de una oferta se cruza con otra
  que comparte algún método de pago (o, entre ofertas de producto, con el mismo `value` y
  `discountType`), responde `409 OFFER_OVERLAP`.
- Una oferta que ya tiene productos asignados no puede pasar a tener métodos de pago
  (`409 OFFER_HAS_PRODUCTS`), y no se le pueden asignar productos si ya tiene métodos de pago
  (`400 OFFER_HAS_PAYMENT_METHODS`).
- `PUT /api/offers/{id}/products` **reemplaza** el conjunto de productos asignado: desasigna los que
  tenía y asigna los del request (`productIds`).
- `GET /api/offers/payment-methods` devuelve, por cada método de pago con oferta vigente, el método
  de pago (key del enum), el valor y el tipo de descuento de la oferta. Pensado para la vista de
  venta, de modo de mostrar en tiempo real el descuento aplicado a cada método de pago.

## Novedades de la API

Lo incorporado respecto de la versión anterior de este documento:

- Estadísticas de venta por período (`GET /api/sales/stats`).
- Ranking de productos más vendidos (`GET /api/sales/top-products`).
- Alertas de stock bajo (`GET /api/products/low-stock`).
- Ventas pendientes de un cliente con detalle (`GET /api/clients/{clientId}/pending-sales-with-details`).
- Actualización parcial del comercio (`PATCH /api/commerces`).
- Resumen de cuenta corriente en la respuesta de clientes (`salesCount`, `pendingSalesCount`,
  `totalSpent`, `totalPending`), también en el detalle, el alta y la edición.
- Filtro de texto en el listado de clientes y en el de cajeros.
- Validación de los parámetros de paginación: `page`, `size`, `sortBy` y `sortDir` fuera de rango
  devuelven `400 INVALID_PARAMETER` en vez de `500` o de una página gigante.
- Revalidación de la sesión en cada request: desactivar un usuario le corta el acceso de
  inmediato, sin esperar a que expire su token.
- `PUT /api/commerces/{commerceId}/user/{userId}` ya no actualiza usuarios de otro comercio, y
  la unicidad de email, cuit y dirección se reporta con su código específico (`409`) y no con un
  `DATA_INTEGRITY_VIOLATION` genérico.
- Las credenciales se leen de variables de entorno (`.env`), con los valores de desarrollo como
  default.

## Códigos de error

| Code | Status | Cuándo |
|---|---|---|
| **Validación de request** | | |
| `VALIDATION_ERROR` | 400 | Body o parámetros inválidos (incluye `fields[]` por campo) |
| `INVALID_REQUEST_BODY` | 400 | Body malformado (JSON roto) |
| `INVALID_PARAMETER` | 400 | Un parámetro tiene un valor inválido (incluye `page`, `size`, `sortBy` y `sortDir` fuera de rango) |
| `MISSING_PARAMETER` | 400 | Falta un parámetro requerido |
| `METHOD_NOT_ALLOWED` | 405 | Verbo HTTP no soportado para la ruta |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Content type no soportado |
| **Autenticación y autorización** | | |
| `INVALID_CREDENTIALS` | 401 | Email o contraseña incorrectos |
| `UNAUTHORIZED` | 401 | Sin cookie `token` o token inválido |
| `ACCOUNT_DISABLED` | 403 | Comercio del usuario deshabilitado |
| `ACCESS_DENIED` | 403 | Rol insuficiente para el endpoint |
| **Recurso inexistente (404)** | | |
| `USER_NOT_FOUND` | 404 | Usuario inexistente |
| `CLIENT_NOT_FOUND` | 404 | Cliente inexistente (o de otro comercio) |
| `CATEGORY_NOT_FOUND` | 404 | Categoría inexistente (o de otro comercio) |
| `PRODUCT_NOT_FOUND` | 404 | Producto inexistente (o de otro comercio) |
| `COMMERCE_NOT_FOUND` | 404 | Comercio inexistente |
| `SELL_POINT_NOT_FOUND` | 404 | Punto de venta inexistente (o de otro comercio) |
| `SALE_NOT_FOUND` | 404 | Venta inexistente (o de otro comercio) |
| `OFFER_NOT_FOUND` | 404 | Oferta inexistente (o de otro comercio) |
| `PRODUCTS_OFFER_NOT_FOUND` | 404 | Algún producto del request no existe en el comercio |
| `ENDPOINT_NOT_FOUND` | 404 | Ruta inexistente |
| **Unicidad violada (409)** | | |
| `EMAIL_ALREADY_EXISTS` | 409 | Email ya registrado |
| `CLIENT_DNI_ALREADY_EXISTS` | 409 | DNI de cliente ya registrado en el comercio |
| `CATEGORY_NAME_ALREADY_EXISTS` | 409 | Nombre de categoría ya usado en el comercio |
| `SELL_POINT_NAME_ALREADY_EXISTS` | 409 | Nombre de punto de venta ya usado en el comercio |
| `COMMERCE_ADDRESS_ALREADY_EXISTS` | 409 | Dirección de comercio ya registrada |
| `COMMERCE_CUIT_ALREADY_EXISTS` | 409 | CUIT de comercio ya registrado |
| **Conflictos de operación (409)** | | |
| `INSUFFICIENT_STOCK` | 409 | Venta con stock insuficiente |
| `PRODUCT_INACTIVE` | 409 | Venta con un producto inactivo |
| `SALE_NOT_PENDING` | 409 | `mark-paid` sobre una venta que no está `PENDING` |
| `DATA_INTEGRITY_VIOLATION` | 409 | La operación viola una restricción de integridad |
| **Descuentos** | | |
| `INVALID_DISCOUNT` | 400 | Descuento inválido |
| `INVALID_DISCOUNT_PREFIX_ZERO` | 400 | Descuento igual a cero (el backend no llega a emitirlo hoy) |
| `DISCOUNT_WITHOUT_TYPE` | 400 | Monto de descuento sin `discountType` |
| `TYPE_WITHOUT_DISCOUNT` | 400 | `discountType` sin monto de descuento |
| `DISCOUNT_EXCEEDS_SUBTOTAL` | 400 | Descuento fijo mayor al subtotal de la venta |
| `DISCOUNT_PERCENTAGE_EXCEEDS_100` | 400 | Descuento porcentual mayor al 100% (venta u oferta) |
| `PRODUCT_SALE_PRICE_BELOW_COST` | 400 | Precio de venta menor que el costo |
| **Ofertas** | | |
| `INVALID_DATE` | 400 | Fecha de oferta en el pasado o fin anterior al inicio |
| `OFFER_OVERLAP` | 409 | Se superpone con otra oferta del mismo alcance |
| `OFFER_HAS_PAYMENT_METHODS` | 400 | Oferta con métodos de pago no puede llevar productos |
| `OFFER_HAS_PRODUCTS` | 409 | Oferta con productos asignados no puede llevar métodos de pago |
| **Archivos** | | |
| `INVALID_FILE_TYPE` | 400 | Tipo de archivo no permitido |
| `FILE_TOO_LARGE` | 413 | Archivo supera el límite de 10 MB |
| `FILE_UPLOAD_FAILED` | 500 | El archivo no pudo subirse |
| `FILE_DELETE_FAILED` | 500 | El archivo no pudo eliminarse |
| `FILE_PROCESSING_FAILED` | 500 | El archivo no pudo procesarse |
| **Interno** | | |
| `INTERNAL_ERROR` | 500 | Error no controlado |

## Correr el proyecto

```bash
# infraestructura: MySQL en localhost:3306 (base "gestion", root/admin) y MinIO (solo para imágenes)
docker compose up -d   # solo levanta MinIO

# credenciales: copiá .env.example a .env y completá los valores
cp .env.example .env

./mvnw spring-boot:run # requiere JDK 25
```

- **MySQL**: `localhost:3306`, base `gestion`, usuario `root` / `admin`. La base debe existir
  previamente; el schema lo genera Hibernate al arrancar (`ddl-auto=update`).
- **MinIO** (solo necesario para uploads de imágenes/logos): API en `localhost:9000`, consola web en
  `localhost:9001`, credenciales `admin` / `admin123`, bucket `gestionbucket`.
- Al iniciar la app se crea automáticamente el usuario SUDO `admin@admin.com` / `admin123`.
- **Credenciales**: `application.properties` lee las credenciales de variables de entorno
  (`MYSQL_*`, `JWT_*`, `MINIO_*`, `ADMIN_*`) y, si no están, usa los valores de desarrollo que
  están hardcodeados como default. El archivo `.env` **no** lo lee Spring Boot solo: hay que
  exportarlo al shell antes de arrancar la app, por ejemplo con
  `set -a; source .env; set +a` (bash) o `$env:SPRING_APPLICATION_JSON` (PowerShell). El `.env`
  está en `.gitignore`: no lo commitees.

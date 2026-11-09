# API de gestión de stock y control de ventas

Sistema de gestión de stock y ventas para comercios (proyecto final de la materia Programación IV —
UTN TUP). Expone una API REST para administrar comercios, productos, categorías, clientes, puntos de
venta y ventas.

## Tecnologías

- **Java 25** + **Spring Boot 4** (Spring Security con JWT, Spring Data JPA)
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
logs). `fields` solo aparece en errores de validación (`VALIDATION_ERROR`).

## Autenticación

1. `POST /api/auth/login` con `{ "email", "password" }` → devuelve el usuario y setea la **cookie
   `token`** con el JWT. Todas las demás requests deben incluir esa cookie (`credentials: include`
   en el navegador).
2. El JWT lleva `userId`, `commerceId`, `role` y `sellPointId` (este último es `null` para
   OWNER/SUDO). El backend confía en ese contenido sin volver a consultar la base.
3. `POST /api/auth/logout` invalida la sesión.

### Roles

| Rol | Alcance |
|---|---|
| `SUDO` | Administrador global: gestiona comercios |
| `OWNER` | Dueño de un comercio: gestiona su catálogo, cajeros, puntos de venta y ventas |
| `CASHIER` | Cajero: consulta catálogo/clientes y registra ventas |

Todo dato está **scopeado al comercio del JWT**: consultar un recurso de otro comercio responde
`404` (nunca 403, para no filtrar su existencia).

## Convenciones de listado

Los endpoints de listado aceptan `page` (0-based), `size`, `sortBy` y `sortDir` (`asc`/`desc`) y
responden paginado:

```json
{
  "content": [ ... ],
  "page": { "size": 10, "number": 0, "totalElements": 42, "totalPages": 5 }
}
```

Los campos de filtro son opcionales; se envían como query params (`?status=PENDING&searchText=marta`).

## Endpoints

> **Roles**: `SUDO` = solo administrador · `OWNER` = solo dueño · `CASHIER/OWNER` = ambos ·
> `Auth` = cualquier usuario autenticado · `Público` = sin sesión.
> `JSON` = body JSON · `multipart` = `multipart/form-data` (incluye archivo de imagen/logo).

### Autenticación

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| POST | `/api/auth/login` | **Público** | `UserLoginRequest` (JSON) | `AuthenticatedUserResponse` + cookie `token` |
| POST | `/api/auth/logout` | Auth | — | mensaje |
| GET | `/api/auth/me` | Auth | — | `AuthenticatedUserResponse` |

### Comercios

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/commerces` | SUDO | — | `Page<CommerceWithOwnerResponse>` |
| POST | `/api/commerces` | SUDO | `CommerceWithOwnerRequest` (multipart) | `CommerceWithOwnerResponse` (201) |
| PUT | `/api/commerces/{commerceId}/user/{userId}` | SUDO | `CommerceWithOwnerUpdateRequest` (multipart) | `CommerceWithOwnerResponse` |
| PATCH | `/api/commerces/{id}/toggle-active` | SUDO | — | `CommerceUpdateActiveResponse` |
| GET | `/api/commerces/stats` | SUDO | — | `CommerceStatsResponse` |
| GET | `/api/commerces/current` | CASHIER/OWNER | — | `CurrentCommerceResponse` |
| PATCH | `/api/commerces/profit-multiplier` | OWNER | `CommerceProfitMultiplierRequest` (JSON) | `CommerceProfitMultiplierResponse` |

### Categorías

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/categories` | CASHIER/OWNER | — | `Page<CategoryResponse>` |
| GET | `/api/categories/{categoryId}` | CASHIER/OWNER | — | `CategoryResponse` |
| POST | `/api/categories` | OWNER | `CategoryRequest` (JSON) | `CategoryResponse` (201) |
| PUT | `/api/categories/{categoryId}` | OWNER | `CategoryRequest` (JSON) | `CategoryResponse` |
| PATCH | `/api/categories/{categoryId}/toggle-active` | OWNER | — | `CategoryUpdateActiveResponse` |

### Productos

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/products` | CASHIER/OWNER | — | `Page<ProductResponse>` · filtros: `categoryId`, `searchText`, `active` |
| GET | `/api/products/with-offer` | CASHIER/OWNER | — | `Page<ProductWithOfferResponse>` · filtros: `categoryId`, `searchText`, `active` · incluye oferta vigente, precio con descuento y precio sugerido |
| POST | `/api/products` | OWNER | `ProductRequest` (multipart, campo `image` opcional) | `ProductResponse` (201) |
| PUT | `/api/products/{id}` | OWNER | `ProductUpdateRequest` (multipart) | `ProductResponse` |
| PATCH | `/api/products/{id}/toggle-active` | OWNER | — | `ProductUpdateActiveResponse` |

### Clientes

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/clients` | CASHIER/OWNER | — | `Page<ClientResponse>` |
| GET | `/api/clients/{dni}` | CASHIER/OWNER | — | `ClientResponse` (por DNI dentro del comercio) |
| POST | `/api/clients` | CASHIER/OWNER | `ClientRequest` (JSON) | `ClientResponse` (201) |
| PUT | `/api/clients/{id}` | CASHIER/OWNER | `ClientRequest` (JSON) | `ClientResponse` |

El DNI es único **por comercio** (`409 CLIENT_DNI_ALREADY_EXISTS` si se repite).

### Cajeros

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/cashiers` | OWNER | — | `Page<UserResponse>` |
| POST | `/api/cashiers` | OWNER | `CashierRequest` (JSON) | `UserResponse` (201) |
| PUT | `/api/cashiers/{id}` | OWNER | `CashierUpdateRequest` (JSON; `password` vacío = mantener la actual) | `UserResponse` |
| PATCH | `/api/cashiers/{id}/toggle-active` | OWNER | — | `CashierUpdateActiveResponse` |

Los cajeros son usuarios (`role = CASHIER`) del comercio del OWNER autenticado.

### Puntos de venta

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/sell-points` | CASHIER/OWNER | — | `Page<SellPointResponse>` |
| GET | `/api/sell-points/{sellPointId}/cashiers` | CASHIER/OWNER | — | `Page<UserResponse>` (cajeros del punto) |
| POST | `/api/sell-points` | OWNER | `SellPointRequest` (JSON: `name`, `cashierIds`) | `SellPointResponse` (201) |
| PUT | `/api/sell-points/{sellPointId}` | OWNER | `SellPointUpdateRequest` (JSON) | `SellPointResponse` |
| PATCH | `/api/sell-points/{sellPointId}/toggle-active` | OWNER | — | `SellPointUpdateActiveResponse` |

Un cajero pertenece a un solo punto de venta; el punto se toma del JWT al registrar ventas, nunca
del request.

### Ventas

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/sales` | CASHIER/OWNER | — | `Page<SaleSummaryResponse>` · filtros: `clientId`, `status`, `paymentMethod`, `searchText` (observaciones o nombre del cliente) |
| GET | `/api/sales/{saleId}` | Auth | — | `SaleResponse` (detalle con todas las líneas) |
| POST | `/api/sales` | CASHIER/OWNER | `SaleRequest` (JSON) | `SaleSummaryResponse` (201) |
| PATCH | `/api/sales/{id}/mark-paid` | CASHIER/OWNER | — | `SaleUpdateStatusResponse` |

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
  productos y cantidades. Los ítems repetidos del mismo producto se consolidan.
- Valores de `paymentMethod`: `CASH`, `CARD`, `TRANSFER`, `CREDIT_ACCOUNT`. El fiado
  (`CREDIT_ACCOUNT`) genera la venta con status `PENDING`; el resto, `PAYED`.
- `discountType` (`PERCENTAGE` / `FIXED_AMOUNT`) y `discount` van juntos; un descuento fijo no puede
  superar el subtotal (`400 DISCOUNT_EXCEEDS_SUBTOTAL`) y un descuento porcentual no puede superar
  el 100% (`400 DISCOUNT_PERCENTAGE_EXCEEDS_100`).
- El descuento de stock es todo-o-nada: si algún ítem no pasa la validación (producto inactivo o
  sin stock), no se descuenta ni se registra nada (`409 PRODUCT_INACTIVE`, `409 INSUFFICIENT_STOCK`).
- `mark-paid` solo admite la transición `PENDING → PAYED` (`409 SALE_NOT_PENDING` en otro caso).

### Ofertas

| Método | Ruta | Roles | Request | Response |
|---|---|---|---|---|
| GET | `/api/offers` | CASHIER/OWNER | — | `Page<OfferResponse>` · filtros: `status`, `paymentMethod`, `offerType` |
| GET | `/api/offers/payment-methods` | CASHIER/OWNER | — | `List<OfferPaymentMethodResponse>` (métodos de pago con oferta vigente) |
| POST | `/api/offers` | OWNER | `OfferRequest` (JSON) | `OfferResponse` (201) |
| PUT | `/api/offers/{id}` | OWNER | `OfferUpdateRequest` (JSON) | `OfferResponse` |
| PUT | `/api/offers/{id}/products` | OWNER | `OfferProductIdsRequest` (JSON) | `200 OK` (asigna productos a la oferta) |

- El `value` de la oferta se interpreta según `discountType`: con `PERCENTAGE` es un porcentaje y no
  puede superar el 100% (`400 DISCOUNT_PERCENTAGE_EXCEEDS_100`); con `FIXED_AMOUNT` es un monto y
  todavía no tiene un máximo porque las ofertas aún no se aplican a la venta.
- Cada oferta tiene un `offerType` derivado de sus `paymentMethods`: con al menos un método de pago es
  `PAYMENT_METHOD`, y sin métodos es `PRODUCT`. Se filtra con `?offerType=PRODUCT` o
  `?offerType=PAYMENT_METHOD`, y se refleja en `OfferResponse.offerType`.
- `GET /api/offers/payment-methods` devuelve, por cada método de pago con oferta vigente, el método de pago
  (key del enum), el valor y el tipo de descuento de la oferta. Pensado para la vista de venta, de modo de
  mostrar en tiempo real el descuento aplicado a cada método de pago.

## Códigos de error frecuentes

| Code | HTTP | Cuándo |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Body inválido (incluye `fields[]` por campo) |
| `INVALID_CREDENTIALS` | 401 | Email o contraseña incorrectos |
| `UNAUTHORIZED` | 401 | Sin cookie `token` o token inválido |
| `ACCOUNT_DISABLED` | 403 | Cuenta o comercio deshabilitado |
| `ACCESS_DENIED` | 403 | Rol insuficiente para el endpoint |
| `*_NOT_FOUND` | 404 | Recurso inexistente **o de otro comercio** |
| `*_ALREADY_EXISTS` | 409 | Unicidad violada (email, DNI por comercio, nombre por comercio) |
| `INSUFFICIENT_STOCK` / `PRODUCT_INACTIVE` | 409 | Venta con ítems no válidos |
| `SALE_NOT_PENDING` | 409 | `mark-paid` sobre una venta que no está `PENDING` |
| `DISCOUNT_EXCEEDS_SUBTOTAL` | 400 | Descuento fijo mayor al subtotal de la venta |
| `DISCOUNT_PERCENTAGE_EXCEEDS_100` | 400 | Descuento porcentual mayor al 100% (venta u oferta) |
| `INTERNAL_ERROR` | 500 | Error no controlado |

## Correr el proyecto

```bash
# infraestructura: MySQL en localhost:3306 (base "gestion", root/admin) y MinIO (solo para imágenes)
docker compose up -d   # solo levanta MinIO

./mvnw spring-boot:run # requiere JDK 25
```

La base debe existir previamente; el schema lo genera Hibernate al arrancar. El usuario admin por
defecto (`admin@admin.com`) se crea automáticamente al iniciar.

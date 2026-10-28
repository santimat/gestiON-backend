@formatter:off

# Sistema de gestion de stock y control de ventas

Este es un proyecto realizado para la materia programación IV de la Tecnicatura Universitaria en Programación (TUP),
UTN.

## Explicación de ciertas elecciones

### Uso de Records para la creación de los DTO (Data Transfer Object)

Se utilizan Records debido a simplicidad a la hora de crearlos, estos por defecto ya traen metodos utiles sin
necesidad de crearlos a mano. Además estos cuentan con la cualidad de ser inmutables por lo que nos asegura mayor
consistencia en los datos.
*** Cosas a saber ***

- Los atributos por defecto son final, por lo que no se pueden modificar una vez creados.
- La forma de acceder a los atributos es mediante metodos. Pero no con el prefijo get, sino con el nombre del
  atributo. Por ejemplo mirecord.nombre() para acceder al atributo nombre.

### Manejo de errores

Todas las respuestas de error del API comparten un único formato, definido por el DTO `ExceptionResponse`:

```json
{
  "statusCode": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "Product with id 5 not found"
}
```

- `statusCode`: código HTTP de la respuesta.
- `code`: **key estable** para mapear el error en el frontend (ver catálogo más abajo). Los mensajes son en
  inglés y están pensados para logs/debug; el frontend debe localizar los textos a partir de la key.
- `message`: mensaje humano del error.
- `fields`: solo aparece en errores de validación (`VALIDATION_ERROR`), con el detalle de cada campo:

```json
{
  "statusCode": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fields": [
    { "field": "name", "message": "must not be blank" }
  ]
}
```

Las excepciones de negocio extienden `BusinessException` y cargan un `ErrorCode` (enum central en
`enums/ErrorCode`) que define la key, el estado HTTP y un mensaje por defecto. El handler único de
`BusinessException` en `GlobalExceptionHandler` arma la respuesta a partir del enum; cualquier otra
excepción no controlada responde `INTERNAL_ERROR` (500) con mensaje genérico, registrando el detalle
en el log (nunca se filtran mensajes internos ni de la base de datos al cliente).

Los errores de seguridad (entry point 401 / access denied 403) y los del controlador `/api/auth/me`
responden con este mismo formato.

#### Catálogo de keys

| Key | HTTP | Cuándo |
|---|---|---|
| `INVALID_CREDENTIALS` | 401 | Email inexistente o contraseña incorrecta en el login |
| `UNAUTHORIZED` | 401 | Sin token, token ausente/inválido o sesión requerida |
| `ACCOUNT_DISABLED` | 403 | Comercio inactivo (credenciales válidas) o cuenta deshabilitada |
| `ACCESS_DENIED` | 403 | Rol insuficiente (`@PreAuthorize`) o acceso denegado |
| `USER_NOT_FOUND` | 404 | Usuario inexistente por id o email |
| `CLIENT_NOT_FOUND` | 404 | Cliente inexistente por DNI (dentro del comercio del JWT) |
| `CATEGORY_NOT_FOUND` | 404 | Categoría inexistente |
| `PRODUCT_NOT_FOUND` | 404 | Producto inexistente |
| `COMMERCE_NOT_FOUND` | 404 | Comercio inexistente |
| `SELL_POINT_NOT_FOUND` | 404 | Punto de venta inexistente (o de otro comercio) |
| `SALE_NOT_FOUND` | 404 | Venta inexistente |
| `ENDPOINT_NOT_FOUND` | 404 | Ruta inexistente |
| `EMAIL_ALREADY_EXISTS` | 409 | Email de usuario ya registrado |
| `CLIENT_DNI_ALREADY_EXISTS` | 409 | DNI de cliente ya registrado **para el comercio del JWT** (unicidad compuesta `commerce_id + dni`) |
| `CATEGORY_NAME_ALREADY_EXISTS` | 409 | Nombre de categoría ya registrado para el comercio |
| `SELL_POINT_NAME_ALREADY_EXISTS` | 409 | Nombre de punto de venta ya registrado para el comercio |
| `COMMERCE_ADDRESS_ALREADY_EXISTS` | 409 | Dirección de comercio ya registrada |
| `COMMERCE_CUIT_ALREADY_EXISTS` | 409 | CUIT de comercio ya registrado |
| `INSUFFICIENT_STOCK` | 409 | Stock insuficiente para la cantidad pedida |
| `PRODUCT_INACTIVE` | 409 | Producto inactivo, no disponible para la venta |
| `DATA_INTEGRITY_VIOLATION` | 409 | Violación de restricción de la base de datos |
| `VALIDATION_ERROR` | 400 | Fallo de validación del body (incluye `fields[]`) |
| `INVALID_DISCOUNT` | 400 | Descuento inválido (tipo no soportado) |
| `DISCOUNT_WITHOUT_TYPE` | 400 | Se envió monto de descuento sin tipo de descuento |
| `TYPE_WITHOUT_DISCOUNT` | 400 | Se envió tipo de descuento sin monto |
| `DISCOUNT_EXCEEDS_SUBTOTAL` | 400 | Descuento fijo mayor al subtotal de la venta |
| `INVALID_REQUEST_BODY` | 400 | JSON malformado o ilegible |
| `INVALID_PARAMETER` | 400 | Parámetro de path/query con tipo inválido (ej. texto donde va un id) |
| `MISSING_PARAMETER` | 400 | Parámetro requerido ausente |
| `INVALID_FILE_TYPE` | 400 | Tipo de archivo no permitido (solo jpg/png/jpeg/webp) |
| `FILE_TOO_LARGE` | 413 | Archivo mayor a 10 MB |
| `METHOD_NOT_ALLOWED` | 405 | Método HTTP no soportado por el endpoint |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Content-Type no soportado |
| `FILE_UPLOAD_FAILED` | 500 | Error al subir archivo a MinIO |
| `FILE_DELETE_FAILED` | 500 | Error al eliminar archivo de MinIO |
| `FILE_PROCESSING_FAILED` | 500 | Error al procesar/detectar el archivo |
| `INTERNAL_ERROR` | 500 | Error inesperado no controlado |

## Gestión de cajeros

Los cajeros son usuarios (`User`) con `role = CASHIER` dentro del comercio del OWNER autenticado. No son
una entidad aparte: comparten la tabla `users`. Su gestión está protegida con
`@PreAuthorize("hasRole('OWNER')")` y se scoping contra el `commerceId` que viene en el JWT
(`UserPrincipal`): un OWNER solo puede afectar usuarios de su propio comercio.

### Crear cajero

`POST /api/cashiers` con `CashierRequest` (`name`, `email`, `password`, `phoneNumber`). Valida que el
email no exista y responde `201` con el `UserResponse` del nuevo cajero.

### Actualizar cajero

`PUT /api/cashiers/{id}` con `CashierUpdateRequest`:

```json
{
  "name": "Nombre actualizado",
  "email": "cajero@comercio.com",
  "password": "",
  "phoneNumber": "2601234567"
}
```

Reglas:

- Se busca por `id + commerceId (JWT) + role = CASHIER` (`CashierFinderByIdAndCommerceIdService`): si no
  matchea un cajero del propio comercio responde `404 USER_NOT_FOUND`. Nunca matchea al OWNER ni usuarios
  de otro comercio.
- `password` es opcional: si llega vacío, con espacios o se omite, **se mantiene la contraseña actual**;
  si llega un valor, se guarda codificado con `PasswordEncoder`.
- El email se valida globalmente excluyendo al propio registro (`existsByEmailAndIdNot` en
  `CashierValidatorByEmailService`): si otro usuario ya lo usa responde `409 EMAIL_ALREADY_EXISTS`;
  conservar el email actual del cajero es válido.
- Solo actualiza `name`, `email` y `phoneNumber` (con validación del request). No modifica `role`,
  `active` ni `commerce`.
- Responde `200` con el `UserResponse` del cajero actualizado.

### Activar/desactivar cajero

`PATCH /api/cashiers/{id}/toggle-active` (`CashierToggleActivePatchController`). Igual que para categorías
(`CategoryToggleActivePatchController`): invierte el valor de `active` del cajero buscado por
`id + commerceId (JWT) + role = CASHIER` y responde `200` con `CashierUpdateActiveResponse`:

```json
{
  "id": 5,
  "active": false
}
```

Si el id no corresponde a un cajero del propio comercio responde `404 USER_NOT_FOUND`. No modifica
`role`, `email` ni ningún otro dato del usuario. Un cajero con `active = false` no puede iniciar sesión
(`ACCOUNT_DISABLED` a nivel login por el estado del comercio/cuenta).

## Puntos de venta (SellPoint)

Los puntos de venta son la unidad de trabajo del cajero: un comercio tiene muchos puntos de venta y
cada cajero (usuario `CASHIER`) pertenece a **un** punto. La relación vive en `users.sell_point_id`
(`@ManyToOne`, nullable — OWNER/SUDO no tienen punto) y viaja en el JWT (`sellPointId` claim).

### Claim `sellPointId` en el JWT

- Emisor: `JwtService` + `TokenPayloadMapper` — se completa desde `user.getSellPoint()` al loguear.
- OWNER/SUDO y los tokens emitidos antes de esta feature no tienen el claim (`null`).
- **Re-logueo requerido**: los cajeros creados antes del rollout tienen que iniciar sesión de nuevo
  para obtener el claim; mientras tanto sus ventas quedan sin punto de venta.

### Endpoints

| Endpoint | Descripción |
|---|---|
| `POST /api/sell-points` | Crea punto de venta (`SellPointRequest`: `name`, `cashierIds?`). Valida nombre único por comercio (`409 SELL_POINT_NAME_ALREADY_EXISTS`) y reasigna los cajeros indicados al punto |
| `GET /api/sell-points` | Lista paginada de puntos del comercio (orden por `updatedAt` desc) |
| `PUT /api/sell-points/{sellPointId}` | Actualiza nombre/cajeros (`SellPointUpdateRequest`) |
| `PATCH /api/sell-points/{sellPointId}/toggle-active` | Activa/desactiva el punto |
| `GET /api/sell-points/{sellPointId}/cashiers` | Cajeros asignados al punto (scoped `sellPointId + commerceId`; `404 SELL_POINT_NOT_FOUND` si no matchea) |

## Ventas (Sale)

### Crear venta

`POST /api/sales` con `SaleRequest`:

```json
{
  "paymentMethod": "CASH",
  "details": [
    { "productId": 3, "quantity": 2 }
  ],
  "clientId": 7,
  "observations": "...",
  "discountType": "PERCENTAGE",
  "discount": 10
}
```

Reglas de negocio (todo server-side, el front no decide montos):

- Roles: `CASHIER` u `OWNER` (`@PreAuthorize`). El punto de venta **no** viene en el request: se
  resuelve del claim `sellPointId` del JWT (`SaleSellPointResolverService`); OWNER/SUDO registran la
  venta sin punto.
- Ítems repetidos del mismo producto se consolidan sumando cantidades
  (`SaleDetailsMergerService`).
- **Validar todo antes de tocar stock**: se valida que cada producto exista en el comercio del JWT,
  esté activo (`PRODUCT_INACTIVE`) y tenga stock suficiente (`INSUFFICIENT_STOCK`). Si algo falla, no
  se descuenta stock ni se persiste nada (transacción all-or-nothing).
- Precios: cada `SaleDetail` congela el `salePrice` del producto al momento de la venta; subtotal =
  suma de ítems, calculado en servidor.
- Descuento (opcional): tipo y monto van juntos (`TYPE_WITHOUT_DISCOUNT` /
  `DISCOUNT_WITHOUT_TYPE`); un `FIXED_AMOUNT` no puede superar el subtotal
  (`DISCOUNT_EXCEEDS_SUBTOTAL`). El `total` se calcula en servidor
  (`SalePriceAfterDiscountResolverService`, precisión 2 con redondeo `HALF_UP`). Nota: la aplicación
  de ofertas al descuento es una feature futura, hoy el descuento es manual.
- Cliente opcional: si llega `clientId` debe pertenecer al comercio del JWT (`404 CLIENT_NOT_FOUND`
  en caso contrario).
- Status: `PaymentMethod` con `CREDIT_ACCOUNT` (fiado) produce venta `PENDING`; los demás métodos
  producen `PAY`.
- Respuesta `201` con `SaleResponse` (subtotal, descuento, total, status, sellPointId, clientId y
  details). La fecha de la venta es `createdAt` (fecha de transacción, no se expone `updatedAt`).

### Arquitectura de `service/sale/`

`SaleCreatorService` es un orquestador puro; cada responsabilidad vive en su servicio:

| Service | Responsabilidad |
|---|---|
| `SaleDetailsMergerService` | Consolidar ítems repetidos |
| `SaleDetailsValidatorService` | Validar activo/stock y armar details + subtotal |
| `SaleProductValidatorService` / `SaleStockValidatorService` | Reglas por ítem |
| `SaleDiscountApplierService` | Validar coherencia del descuento |
| `SalePriceAfterDiscountResolverService` | Matemática del precio final |
| `SaleStatusResolverService` | Status según método de pago |
| `SaleClientResolverService` | Cliente del comercio o `null` |
| `SaleSellPointResolverService` | Punto de venta desde el JWT o `null` |
| `SaleStockUpdaterService` | Descontar stock |
| `SaleDetailCreatorService` | Persistir los details |

## Clientes

Los clientes son **del comercio** (`@ManyToOne Commerce`, unicidad compuesta `commerce_id + dni` en
DB, `uk_clients_commerce_dni`): dos comercios pueden tener el mismo DNI sin conflicto.

| Endpoint | Descripción |
|---|---|
| `GET /api/clients` | Lista del comercio del JWT (con filtros) |
| `GET /api/clients/{dni}` | Cliente por DNI dentro del comercio (`404 CLIENT_NOT_FOUND` si no matchea) |
| `POST /api/clients` | Crea cliente (`201`), valida DNI único por comercio |
| `PUT /api/clients/{id}` | Actualiza cliente (por id + comercio del JWT) |

## Renombres y formato

- **`Commerce.businessName` → `name`**: el atributo y los campos de API de commerce se llaman `name`
  (request: `CommerceRequest`, `CommerceWithOwnerRequest`, `CommerceWithOwnerUpdateRequest`; response:
  `CommerceResponse`, `CurrentCommerceResponse`, `CommerceWithOwnerResponse`). ⚠️ La columna de DB
  sigue siendo `business_name` (mapeada en el `@Column`); el cambio solo afecta la superficie HTTP.
- **Decimales**: todos los campos monetarios (`BigDecimal` de `Product`, `Offer`, `Sale` y
  `SaleDetail`) usan `@Column(precision = 12, scale = 2)` — montos hasta `9.999.999.999,99`.
- Paginación: los listados paginados de categorías, productos, clientes, cajeros y puntos de venta
  ordenan por `updatedAt` desc por defecto; los de ventas usan el `createdAt` del registro.


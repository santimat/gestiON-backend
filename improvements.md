# Revisión de mejoras — gestiON-backend

Revisión de código del backend (Spring Boot 4.1 / Java 25 / MySQL 8) hecha con tres subagentes
`reviewer` en paralelo, cada uno sobre un dominio independiente:

| Revisor | Alcance |
|---|---|
| Arquitectura y convenciones | Capas, naming, disciplina de records/DTO, `BusinessException`/`ErrorCode`, scoping por comercio, `getReference`, mappers, código muerto |
| Seguridad y correctitud | `JwtFilter`/cookie, cobertura de `@PreAuthorize`, IDOR cross-tenant, mass assignment, validación, uploads (Tika/MinIO), math de dinero, secretos |
| Data access y tests | N+1, `@EntityGraph`, query agregada de clientes, paginación, transacciones, índices en MySQL, bucketing temporal, cobertura y flakiness de tests |

Cada hallazgo de severidad alta fue **verificado a mano** contra el código y contra la base MySQL
antes de incluirlo acá. La columna *Verificado* indica el estado de esa comprobación.

Estado del proyecto al momento de la revisión: `.\mvnw.cmd clean compile` → 281 fuentes, BUILD SUCCESS ·
`.\mvnw.cmd test` → 5/5 OK · `clean package` → jar + repackage OK · la app levanta y
`POST /api/auth/login` responde 200. **La base compila; nada de esto es un error de compilación.**

---

## Correcciones a los revisores

Tres afirmaciones de los subagentes no resistieron la verificación y se ajustaron:

| Afirmación original | Realidad verificada |
|---|---|
| "`JwtFilter` se ejecuta dos veces por request" | **Incorrecto.** `OncePerRequestFilter` garantiza que `doFilterInternal` corra una sola vez por request. El doble registro (`@Component` + `addFilterBefore`) es redundancia, no doble ejecución → baja a cosmético |
| "Falta índice en `clients(commerce_id, dni)`" | **Incorrecto.** Ese índice ya existe: es la unique constraint `uk_clients_commerce_dni` sobre `(commerce_id, dni)`. Solo `sales` carece de índice compuesto |
| "`CommerceWithOwnerUpdaterService` es un escape entre tenants" | **Real, pero sobrevalorado.** El endpoint es `@PreAuthorize("hasRole('SUDO')")` y SUDO administra legítimamente todos los comercios → es un hueco de *alcance* (actualiza un user sin validar que sea del commerce del path), no una fuga entre tenants. Baja de Critical a P2 |

---

## P0 — Corregir antes de seguir

### P0-1 · Se puede persistir una venta con total negativo — **RESUELTO**

- **Ubicación:** `service/product/ProductDiscountedPriceCalculatorService.java:16-22` → `service/saleDetails/SaleDetailsValidatorService.java:46-47, 52, 56` → `service/sale/SaleCreatorService.java`
- **Problema:** el precio con descuento de la oferta se calcula como `salePrice.subtract(value)` **sin guard de total negativo**, y ese valor entra como `detail.price` y se acumula en el `subtotal` de la venta.
  El guard "de seguridad" que existe en `SalePriceAfterDiscountResolverService:31-35` **no protege este camino**: es otra clase y `ProductDiscountedPriceCalculatorService` nunca la llama (el comentario del guard asume lo contrario).
  En el otro extremo no hay tope: `OfferRequest.value` solo declara `@Positive` (`dto/request/offer/OfferRequest.java:13-15`) y `OfferCreatorService:41-45` valida el máximo **solo** para `PERCENTAGE`, con un comentario desactualizado — *"en monto fijo no se valida un máximo porque todavía no existe contra qué total compararlo (las ofertas aún no se aplican a la venta)"* — cuando hoy sí se aplican a la venta.
  `OfferAssignerProductService.assignOfferToProducts` asigna la oferta a productos sin capearla contra el `salePrice` de cada uno.
- **Por qué importa:** una oferta `FIXED_AMOUNT` mayor que el precio del producto produce una venta con `subtotal` y `total` negativos que **se persisten**, junto con el descuento de stock. Corrompe la facturación, deja stock negativo y ensucia las estadísticas por comercio.
- **Estado:** verificado por código y por datos. **Latente hoy**: las ofertas fijas del seed son 250.00 y 500.00 contra productos de 1100–5200, y no hay ninguna venta con `total < 0` en la base. Alcanzable por cualquier OWNER.
- **Fix:** (a) capear la oferta contra el `salePrice` del producto al asignarla; (b) mover el guard `signum() < 0` a `ProductDiscountedPriceCalculatorService` (o hacer que este delegue en `SalePriceAfterDiscountResolverService`, que ya tiene el guard); (c) corregir el comentario desactualizado de `OfferCreatorService`.
- **Resolución:** los tres puntos del fix están aplicados.
  - `ProductDiscountedPriceCalculatorService` tiene el guard `signum() < 0` y devuelve
    `409 OFFER_EXCEEDS_PRODUCT_PRICE` (`InvalidDiscountException`).
  - El capeo contra el `salePrice` se aplica en los tres caminos de mutación:
    `OfferAssignerProductService` (al asignar productos), `OfferUpdaterService` (al cambiar el
    valor de una oferta que ya tiene productos) y `ProductUpdaterService` (al bajar el precio de
    un producto que tiene oferta asignada). La lógica vive en
    `OfferProductPriceValidatorService` + `DiscountRangeValidatorService`.
  - El comentario obsoleto de `OfferCreatorService` y el de `SalePriceAfterDiscountResolverService`
    están corregidos: la oferta de monto fijo se capean **al asignar y al actualizar**, no al
    crear, porque al crearse todavía no hay productos contra los que comparar.
  - Cubierto por `ProductDiscountedPriceCalculatorServiceTests` y
    `OfferProductPriceValidatorServiceTests`.

### P0-2 · Los borrados de archivo son no-ops silenciosos — **RESUELTO**

- **Ubicación:** `service/file/FileUploaderService.java:48, 58` vs `service/file/FileDeleterService.java:26`
- **Problema:** el upload **guarda** el objeto como `subDir + "/" + objectName` pero **devuelve** solo `objectName`; el deleter borra `objectName` sin el prefijo. La clave nunca coincide → el borrado no falla (MinIO no da error al borrar una clave inexistente) y no borra nada.
- **Por qué importa:** los "borrar el logo anterior" / "borrar la imagen anterior" en `ProductImageUploaderService:22-24`, `CommercePartialUpdaterService:40-42` y `CommerceUpdaterService:34-36` son no-ops. El bucket crece sin límite y quedan logos/imágenes viejas (con datos de negocio) sin ruta de limpieza.
- **Estado:** verificado. `FileFinderService:27` **sí** antepone el `subDir`, así que las URLs presignadas funcionan — el bug es solo del deleter.
- **Fix:** pasar `subDir + "/" + objectName` al deleter, o persistir la object key completa en la entidad y usar esa.
- **Resolución:** la clave se centralizó en `FileObjectKey.of(subDir, objectName)` y la usan el
  uploader, el finder y el deleter, así que ya no pueden volver a divergir. Cubierto por
  `FileObjectKeyTests`.

### P0-3 · Los errores de validación multipart devuelven 500 en vez de 400 — **RESUELTO**

- **Ubicación:** `exception/handler/GlobalExceptionHandler.java:120` (catch-all) vs `:67` (solo `MethodArgumentNotValidException`)
- **Problema:** 5 endpoints bindean con `@Valid @ModelAttribute` (`ProductPostController:26`, `ProductPutController:27`, `CommercePatchController:27`, `CommerceWithOwnerPostController:24`, `CommerceWithOwnerPutController:21`). Spring lanza **`BindException`** en esos casos, y el handler no tiene rama para esa clase → cae en `@ExceptionHandler(Exception.class)` → `500 INTERNAL_ERROR`.
- **Por qué importa:** rompe el contrato de error documentado (400 `VALIDATION_ERROR` con `fields[]`) que el front consume, y esconde errores del cliente como fallas del servidor.
- **Estado:** verificado (no hay ningún `BindException` en el handler; los 5 endpoints existen).
- **Fix:** agregar `@ExceptionHandler(BindException.class)` que devuelva el mismo payload `fields[]` / `VALIDATION_ERROR` que `handleValidationException`.
- **Resolución:** `GlobalExceptionHandler` tiene la rama `BindException` y devuelve el mismo
  `fields[]` / `VALIDATION_ERROR`.

### P0-4 · La validación de método nativa también devuelve 500 — **RESUELTO**

- **Ubicación:** `exception/handler/GlobalExceptionHandler.java:120` + `controller/sale/TopProductsSoldGetController.java:29`
- **Problema:** desde Spring 6.1 la validación de método de controller es nativa (no hace falta `@Validated`), y el proyecto tiene `@Min(1) int limit` en un `@RequestParam`. Eso dispara `HandlerMethodValidationException`, que el catch-all se traga → 500 en vez de 400.
- **Por qué importa:** igual que P0-3, cualquier `@NotNull`/`@Min` sobre un parámetro de método devuelve 500.
- **Estado:** verificado (la restricción existe, el handler no la cubre).
- **Fix:** agregar `@ExceptionHandler(HandlerMethodValidationException.class)` aplanando `getParameterValidationResults()` al mismo formato `fields[]`.
- **Resolución:** la rama existe y aplana `getParameterValidationResults()` al mismo formato
  `fields[]`. Cubierto por `MethodValidationIntegrationTests`.

---

## P1 — Debería corregirse

### P1-5 · Desactivar un usuario no revoca el acceso — **RESUELTO**

- **Ubicación:** `service/auth/AuthLoginService.java:33-38`, `config/JwtFilter.java:34-42`
- **Problema:** el login solo mira `commerce.isActive()`; `User.active` no se consulta en el login ni se revalida por request. `PATCH /api/cashiers/{id}/toggle-active` mueve un flag que nadie lee.
- **Por qué importa:** un cajero dado de baja sigue autenticándose y conserva acceso completo de lectura/escritura durante la vida del token (24 h).
- **Estado:** verificado. **AGENTS.md ya lo documenta como limitación conocida** (*"el flag `active` de un cajero sigue pudiendo autenticarse, porque ese flag no se valida en el login"*) → es una decisión de producto pendiente, no un descuido.
- **Fix:** rechazar `!loginUser.isActive()` con `ACCOUNT_DISABLED` en el login y revalidar el usuario (o un token version) por request.
- **Resolución:** implementado con la **opción (c)** (una query liviana por request).
  - El login rechaza al usuario desactivado con `403 ACCOUNT_DISABLED`.
  - `JwtFilter` revalida la sesión en cada request autenticado con una proyección mínima
    (`UserActiveStatus`: solo los flags `active` del usuario y del comercio, por primary key), sin
    cargar la entidad ni sus relaciones. Un usuario desactivado → `403 ACCOUNT_DISABLED`; un
    usuario que ya no existe → `401 UNAUTHORIZED`.
  - Cubierto por `DeactivatedUserAccessIntegrationTests` y `UserActiveStatusTests`.

### P1-6 · `size` de paginación sin tope en 9 endpoints — **RESUELTO**

- **Ubicación:** `ClientsGetController:38`, `SalesGetController:40`, `ProductsGetController:38`, `CategoriesGetController:37`, `SellPointsGetController:35`, `SellPointCashiersGetController:37`, `OffersGetController:41`, `CommercesWithOwnerGetController:34`, `ProductsWithOfferGetController:38`
- **Problema:** arman `PageRequest.of(page, size, sort)` a mano, así que **esquivan** el `max-page-size` que aplica `PageableHandlerMethodArgumentResolver`, y no hay `spring.data.web.pageable.max-page-size` configurado. `page < 0` o `size = 0` hacen que `PageRequest.of` lance `IllegalArgumentException` → 500 (no hay handler). Un `sortBy` desconocido → `PropertyReferenceException` → 500.
- **Por qué importa:** `?size=1000000` convierte un endpoint paginado en un dump de tabla. Peor: la query agregada de clientes expande `IN :clientIds` a un placeholder por id, y **MySQL corta en 65.535 placeholders** → error SQL duro en una página grande.
- **Estado:** verificado (9 controllers, sin config, `PropertyReferenceException` no está en el handler).
- **Fix:** clampar `page >= 0` y `1 <= size <= 100` (helper compartido), allow-list de propiedades ordenables, y mapear `IllegalArgumentException` / `PropertyReferenceException` a 400 `INVALID_PARAMETER`.
- **Resolución:** implementado con **rechazo en vez de clampeo silencioso**.
  - `PageableFactory.create(...)` valida `page >= 0`, `1 <= size <= 100`, `sortBy` contra la
    allow-list de `SortableProperties` y `sortDir` ∈ {`asc`, `desc`}. Los 9 controllers lo usan.
  - Un valor fuera de rango se rechaza con `400 INVALID_PARAMETER` (`InvalidParameterException`):
    el cliente tiene que saber que lo que pidió no se puede servir.
  - El handler mapea `IllegalArgumentException` y `PropertyReferenceException` a `400` como red de
    seguridad.
  - Cubierto por `PageableFactoryTests` y `PaginationParametersIntegrationTests`.

### P1-7 · N+1 en productos y ofertas

- **Ubicación:** `repository/JpaProductRepository.java:35` (sin `@EntityGraph`), `repository/JpaOfferRepository.java:87` (sin fetch de `paymentMethods`), `service/product/ProductSearcherService.java`, `service/product/ProductWithOfferSearcherService.java`, `service/offer/OfferPaymentMethodsFinderService.java:37`
- **Problema:** `findAllWithFilters` de productos devuelve entidades con `category` y `offer` en `LAZY` (`model/Product.java:30-36`) y el mapper las lee → **2 queries extra por fila**. Lo mismo con la `@ElementCollection` `paymentMethods` de las ofertas (`model/Offer.java:50`). Los dos servicios de búsqueda de productos **no tienen `@Transactional`**, así que hoy dependen de `open-in-view` (Spring Boot avisa por eso) — desactivar OSIV los rompe con `LazyInitializationException`.
- **Por qué importa:** contradice la regla del propio proyecto ("un finder que alimenta una respuesta que mapea varias relaciones debe usar `@EntityGraph`") y multiplica las queries de los listados más usados.
- **Estado:** verificado (ausencia de `@EntityGraph`, ausencia de `@Transactional`, y los 5 searchers hermanos sí lo tienen).
- **Fix:** `@EntityGraph(attributePaths = {"category", "offer"})` en `JpaProductRepository.findAllWithFilters`, fetch de `paymentMethods` en la query de ofertas, y `@Transactional(readOnly = true)` en los tres servicios.

### P1-8 · Sin bloqueo optimista: dos ventas concurrentes pueden descontar stock de más

- **Ubicación:** `service/sale/SaleStockUpdaterService.java:13-18`, `service/sale/SaleStockValidatorService.java:13`, `model/Product.java:53-55`
- **Problema:** el stock se descuenta con read-modify-write (`product.setCurrentStock(current - qty)`) y **ninguna entidad declara `@Version`** (verificado: cero matches en `model/`). La validación previa de stock es una lectura separada y no atómica. Tampoco hay índice compuesto en `sales` (verificado con `SHOW INDEX`: solo los FK de una columna).
- **Por qué importa:** dos ventas simultáneas de la última unidad pasan ambas la validación y las dos descuentan → stock negativo y overselling.
- **Estado:** verificado. No hay datos que lo disparen hoy, pero la carrera es real.
- **Fix:** `@Version` en `Product` (→ `OptimisticLockException` → 409) o lock `PESSIMISTIC_WRITE` en el paso de descuento con re-chequeo de stock dentro del lock.

### P1-9 · Secretos de producción commiteados — **PARCIAL**

- **Ubicación:** `src/main/resources/application.properties:6` (password de MySQL), `:15` (secret del JWT), `:20-21` (access/secret key de MinIO), `:28` (password del admin SUDO)
- **Problema:** los cuatro están en el repo. El secret del JWT es una **frase legible de ~45 caracteres**, no material aleatorio.
- **Por qué importa:** cualquiera con acceso al repo puede **forjar tokens** para cualquier `userId`/`commerceId`/`role` (incluido `SUDO`) y entrar a MySQL y MinIO directo. Invalida el modelo de confianza del JWT por completo.
- **Estado:** verificado.
- **Fix:** mover a variables de entorno / secret manager (`${JWT_SECRET}`), fallar el arranque si faltan, rotar los cuatro valores y sacarlos del historial de git.
- **Resolución parcial:** `application.properties` ahora lee las credenciales de variables de
  entorno (`MYSQL_*`, `JWT_*`, `MINIO_*`, `ADMIN_*`) y, si no están, cae en los valores de
  desarrollo hardcodeados. Se agregó `.env.example` (commiteado, sin valores) y `.env` (ignorado
  por git) con los datos de desarrollo para que cada quien los complete.
  **Falta** (requiere intervención manual): rotar los cuatro valores reales y sacarlos del
  historial de git. El default hardcodeado sigue siendo el valor de desarrollo, así que la
  rotación no es opcional antes de exponer el servicio.

### P1-10 · Un CASHIER puede aplicar cualquier descuento manual — **RESUELTO**

- **Ubicación original:** `controller/sale/SalePostController.java:25` (`@PreAuthorize` cubre OWNER y CASHIER), `dto/request/sale/SaleRequest.java:24-27`, `service/sale/SaleDiscountApplierService.java:32-36`
- **Problema:** el descuento manual del request no tiene tope, ni motivo obligatorio, ni límite por comercio, ni requiere aprobación del dueño. `validateFixedAmountNotGreaterThanTotal` solo impide que supere el subtotal, así que un CASHIER puede dejar la venta en 0. Además el descuento se **descarta silenciosamente** si el método de pago tiene una oferta activa (`SaleCreatorService:64-69`), con lo que el comportamiento difiere según la configuración.
- **Por qué importa:** regalar stock es decisión del dueño, no del cajero.
- **Decisión de producto:** el descuento manual **se elimina**. Los descuentos salen **solo de las ofertas** (de producto o de método de pago). No hay descuento por rol ni por comercio: la noción de "descuento manual" desaparece del sistema.
- **Fix aplicado:**
  - `SaleRequest` ya no expone `discount` ni `discountType` (el front deja de mandarlos; un body que los mande se ignora sin error, porque Jackson ignora las propiedades desconocidas).
  - `Sale.discount` / `Sale.discount_type` → `payment_method_discount` / `payment_method_discount_type` (columnas renombradas en la base). Guardan el descuento de la **oferta de método de pago** aplicado en el momento de la venta; se persisten, así que el histórico no se altera cuando la oferta vence.
  - `SaleDiscountApplierService` sin ramas de descuento manual: sin oferta el total es el subtotal.
  - `DiscountRangeValidatorService.validateFixedAmountNotGreaterThanTotal` eliminado (quedó muerto).
  - `ErrorCode`: se borran `DISCOUNT_WITHOUT_TYPE`, `TYPE_WITHOUT_DISCOUNT`, `DISCOUNT_EXCEEDS_SUBTOTAL` e `INVALID_DISCOUNT_PREFIX_ZERO` (los cuatro muertos), y se agrega `PAYMENT_METHOD_OFFER_EXCEEDS_SALE_SUBTOTAL` (409) para el guard de total negativo, que antes decía "descuento" y ahora dice "oferta de método de pago".
  - El descarte silencioso desaparece: ya no hay un descuento manual que la oferta pueda pisar.
- **Fuera de alcance:** la auditoría del motivo (eje 3) no aplica al descuento manual porque ya no existe. Queda como feature aparte para el caso de "regalar un producto", que no es un descuento.

---

## P2 — Conviene arreglarlo

| # | Ubicación | Problema | Fix | Estado |
|---|---|---|---|---|
| P2-11 | `controller/sale/SaleWithDetailsGetController.java` | Único endpoint **sin `@PreAuthorize`** (los otros tres sin guard son los de auth, que es lo correcto). Hoy está scopeado por comercio, así que no es explotable, pero un SUDO recibe un 404 confuso y cualquier cambio futuro que afloje el filtro lo vuelve cross-tenant | Agregar `@PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")` | **RESUELTO** |
| P2-12 | `service/user/UserUpdaterService.java:17, 20` | Resuelve el user con `findById` **sin scope de comercio** → `PUT /api/commerces/{commerceId}/user/{userId}` actualiza un user de otro commerce. Además inyecta `PasswordEncoder` y **nunca cambia la contraseña** (dependencia muerta + capacidad faltante) | Nuevo `UserFinderByIdAndCommerceIdService` con `findByIdAndCommerceIdAndRole(id, commerceId, Role.OWNER)`; agregar el setter de password | **RESUELTO** (scoping + `PasswordEncoder` muerto eliminado; el setter de password queda fuera de alcance) |
| P2-13 | `service/commerce/CommerceUpdaterService.java:26-27` | Sin validación de unicidad de cuit/address al actualizar (existen `CommerceValidatorByCuitService`/`ByAddressService`, usados solo en el create) → 409 `DATA_INTEGRITY_VIOLATION` en vez de `COMMERCE_CUIT_ALREADY_EXISTS` / `COMMERCE_ADDRESS_ALREADY_EXISTS` | Métodos `...AndIdNot` en el repository + checks en el updater | **RESUELTO** |
| P2-14 | `service/user/UserUpdaterService.java:24` | Sin chequeo de unicidad de email al actualizar (existe `UserValidatorByEmailService`, sin usar) → mismo desvío de código de error que P2-13 | Llamar al validator con exclusión del propio id | **RESUELTO** |
| P2-15 | `SaleCreatorService:43`, `CategoryCreatorService:25`, `CashierCreatorService:29`, `CashierUpdaterService:25`, `ClientCreatorService:23`, `SellPointCreatorService:26` | `AccessDeniedException` de Spring usada como error de negocio; el handler devuelve `exception.getMessage()` crudo al cliente (`"Commerce ID is null"`, `"Authenticated user does not have a commerce associated"`) | Nuevo `ErrorCode` + subclase de `BusinessException`; el handler debe usar el mensaje por defecto del código | **RESUELTO** (el `AccessDeniedException` de Spring se reemplazó por `CommerceNotAvailableException` + `ErrorCode.COMMERCE_NOT_AVAILABLE`, que se levanta desde un único `CommerceAvailabilityValidatorService` usado por los seis servicios; el handler ya no reenvía el mensaje crudo de Spring, así que el cliente recibe `"The authenticated user has no commerce associated"` en vez de un detalle interno) |
| P2-16 | `controller/category/CategoryGetController.java:29` + `mappers/CategoryMapper.java:21-26` | `toResponse(category)` delega en `toResponse(category, 0)` → **`productCount` siempre 0** en `GET /categories/{id}`, mientras el listado sí da el valor real. Mismo campo, dos significados | Que el finder por id devuelva la `CategoryResponse` con el conteo real, como hace el searcher | **RESUELTO** (nuevo `CategoryDetailsService`: el detalle y el update devuelven el conteo real. El `productCount` de la categoría **anidada** en un producto sigue en `0` a propósito: contarla por producto sería una query por fila en el listado) |
| P2-17 | `service/client/FindPendingSaleWithDetailsByClientIdService.java:29, 43` | `Pageable.unpaged()`: devuelve **todas** las ventas pendientes del cliente con todos sus detalles en una sola respuesta. Además N+1: `findAllBySaleId` se llama **dentro** del `.map()`, y `SaleMapper` lee `sellPoint`/`user`, que el finder no trae | Paginar el endpoint; traer todos los detalles en una query (`d.sale.id IN :saleIds`) y agregar `sellPoint`/`user` al `@EntityGraph` | **RESUELTO** (el endpoint pagina con `PageableFactory`; las ventas de la página llegan con `client`/`sellPoint`/`user` ya cargados por `@EntityGraph`, los detalles salen en una única query batched (`d.sale.id IN :saleIds`) y los totales salen del agregado de resumen de cuenta que ya existía. El conteo de queries no crece con la cantidad de ventas: 4 con 1 venta pendiente y 4 con 5) |
| P2-18 | `service/tika/MimeTypeValidatorService.java:14` y `MimeTypeDetecterService.java:9` | Dos instancias de `Tika` distintas; el allow-list incluye `"image/jpg"`, un MIME que Tika nunca devuelve (da `image/jpeg`) | Un solo `Tika`, mover a `utils/`, sacar `"image/jpg"` | **RESUELTO** (nuevo `service/tika/MimeTypeService.java`: una sola instancia de `Tika`, allow-list `image/png` / `image/jpeg` / `image/webp`. Se eliminaron las dos clases estáticas. De paso el archivo se lee **una** vez en vez de dos: antes se detectaba el tipo para validar y otra vez para detectar) |
| P2-19 | `service/product/ProductFinderByIdsAndCommerceIdService.java:23` | `products.size() != ids.size()` trata un request con ids repetidos (`[3,3]`) como "productos no encontrados" → 404 espurio `PRODUCTS_OFFER_NOT_FOUND` | Comparar contra `Set.copyOf(ids).size()` | **RESUELTO** (se compara contra `ids.stream().distinct().count()`, no contra `Set.copyOf(ids).size()`: el request valida `@NotNull` en la lista pero no en los elementos, así que un id `null` haría explotar `Set.copyOf` con un `NullPointerException` en vez de un 404) |
| P2-20 | `service/file/FileFinderService.java:28, 31-34` | Presigned URL de **5 horas**; errores tragados con `System.out.println` y `null` devuelto (los otros dos servicios de archivo lanzan `FileException` con `ErrorCode`). `ProductMapper:48` cae al nombre interno del objeto en el `imageUrl` público | Expiración ~10 min, `@Slf4j` en vez de `println`, y nunca exponer el nombre crudo del objeto | **RESUELTO** (expiración ya bajada de 5 h a **1 h**; `@Slf4j` en vez de `println`; `ProductMapper` ya no cae al `imageName` crudo, así que el nombre interno del objeto no se expone en el `imageUrl` público. **El error se sigue tragando y devolviendo `null` a propósito**: si MinIO está caído el catálogo tiene que seguir funcionando sin imágenes, así que el front recibe `imageUrl = null` y muestra su placeholder. La expiración queda en 1 h y no en los ~10 min sugeridos, para no romper las imágenes de una pestaña abierta) |

---

## P3 — Limpieza y convenciones

| # | Ubicación | Problema |
|---|---|---|
| P3-21 | `controller/product/ProductToggleAtivePatchController.java:18` | Typo: **`Ative`** → `Active`. Los 4 hermanos usan `ToggleActive`. Renombrar clase y archivo |
| P3-22 | `service/product/ProductUpdateActiveResponse.java` | DTO de **respuesta** living en `service/product`; sus 3 hermanos están en `dto/response/<recurso>/`. Mover a `dto/response/product/` y actualizar los 2 imports |
| P3-23 | `controller/client/GetPendingSaleWithDetailsController.java:18` + `service/client/FindPendingSaleWithDetailsByClientIdService.java:22` | Naming invertido en los dos: debería ser `Resource+Verbo+Controller` y `Entity+Acción+Service`. El servicio además vive en `service/client` pero consulta repos de venta → moverlo a `service/sale` |
| P3-24 | `controller/user/UserPutController.java` | `@RestController` vacío en `/api/users`, sin métodos ni dependencias → código muerto que anuncia un endpoint inexistente. Eliminar |
| P3-25 | `service/product/ProductFinderByIdService.java:12` | Clase **entera sin uso** y es justamente el finder *no scopeado* que el estándar marca como anti-patrón. Eliminar |
| P3-26 | `service/product/ProductCounterByCategoryIds.java:14` | Bean sin sufijo `Service`, en `service/product` pero lo consume `CategorySearcherService` para contar productos por categoría → `service/category/CategoryProductCounterService` |
| P3-27 | `service/sale/SaleItemQuantitiesMapperService.java:14` | Bean llamado `...Mapper...Service` que consulta el repository → colisiona con el significado reservado de "Mapper" (clase estática en `mappers/`) |
| P3-28 | `service/client/ClientResponseAssembler.java:22` | Bean sin sufijo `Service`, inyectado **directamente en 3 controllers** en vez de en un service |
| P3-29 | `service/saleDetails/` | Paquete en camelCase y plural, mientras el hermano es `service/sellpoint`; las clases mezclan `SaleDetails*` (plural) y `SaleDetail*` (singular) para la misma entidad → `service/saledetail` + `SaleDetailValidatorService` / `SaleDetailMergerService` |
| P3-30 | `repository/JpaSaleRepository.java:125-213` | Lógica de negocio (`resolveRange`, `getProfitByPeriod`, `getSalesCountByPeriod`, `getAverageSaleValueByPeriod`, `getProductsSoldByPeriod`, `groupByHour/Day/Week`) implementada como métodos `default` **en la interfaz de Spring Data** → invierte el layering. Los 4 services que los envuelven (`ProfitByPeriodService`, `SalesCountByPeriodService`, `AverageSaleValueByPeriodService`, `ProductsSoldByPeriodService`) son pass-through de una línea |
| P3-31 | `exception/handler/GlobalExceptionHandler.java:37-55` | 4 handlers (`InvalidDateException`, `InvalidProductPriceException`, `OfferOverlapException`, `OfferHasProductsException`) son **byte a byte** equivalentes al handler genérico de `BusinessException` (`:32`), mientras 8 subclases más no tienen rama propia. Borrar los 4 redundantes |
| P3-32 | `service/sale/SaleCreatorService.java:40`, `ClientUpdaterService.java:18`, `SellPointCashiersAssignerService.java:20` vs `SaleSearcherService.java:25` | Se mezclan `jakarta.transaction.Transactional` (8 servicios; no soporta `readOnly` ni las reglas de propagación/rollback de Spring) con `org.springframework.transaction.annotation.Transactional` (el resto) |
| P3-33 | `service/product/ProductPriceValidatorService.java:14` | Lanza `INVALID_REQUEST_BODY` ("body malformado") para un caso de negocio; el null ya lo cubre `@NotNull` en `ProductRequest:18,21`. La rama es inalcanzable por la API |
| P3-34 | `repository/JpaSaleRepository.java:135` | Rama `default` inalcanzable (los 3 constantes de `PeriodGranularity` están cubiertos) y único `IllegalArgumentException` crudo del proyecto → 500 si se alcanzara |
| P3-35 | `controller/category/CategoryGetController.java:29` | Único controller que mapea la entidad JPA (`CategoryMapper.toResponse(category)`, importando `com.gestion.model.Category`); todos los demás reciben un DTO listo |
| P3-36 | varios | Campos inyectados sin `final` (`ProductToggleAtivePatchController:20`, `ProductSearcherService:20`, `CashierSearcherService:16`, `CategorySearcherService:22`) |
| P3-37 | ~20 tipos | Código muerto: `mappers/UserUpdateMapper:16`, `mappers/AuthenticatedUserMapper:21`, `mappers/SaleDetailMapper:11`, `dto/response/commerce/CommerceResponse:3`, `dto/request/file/FileRequest:6`, 7 queries derivadas sin uso en `JpaOfferRepository:21-56`, `JpaUserRepository:27`, `JpaCategoryRepository:14`, y el `ErrorCode.INVALID_DISCOUNT_PREFIX_ZERO` que nunca se lanza (ya eliminado en P1-10) |

---

## Hallazgos reportados que no verifiqué uno por uno

Los tres revisores listaron además ~35 ítems de severidad menor. Se dejan acá como estaba, **sin
verificación individual** — es el punto de partida para una segunda pasada:

- **Perfil por defecto con logging sensible:** `application.properties:11` (`spring.jpa.show-sql=true` imprime cada statement con sus valores) y `:2` (`logging.level.org.springframework.security=DEBUG`). Contraseñas hasheadas, emails, teléfonos, DNI y CUIT en los logs.
- **Postura de cookie y CSRF:** `SecurityConfig:37` deja la cookie con `Secure(false)` y sin `SameSite` explícito, con CSRF globalmente deshabilitado y auth por cookie. El token viaja en claro por HTTP y se puede reusar 24 h.
- **Passwords:** mínimo de 3 caracteres en `UserRequest:18`, `CashierRequest:18` y `CommerceWithOwnerRequest:20`; sin rate limiting ni lockout en `AuthLoginPostController:25-27`; sin audit log de intentos de login.
- **Filtros no sargables:** `JpaClientRepository:25-28`, `JpaProductRepository:29-32`, `JpaSaleRepository:45-47` usan `LOWER(col) LIKE '%' || :searchText || '%'` con el patrón `(:param IS NULL OR ...)` → MySQL escanea la tabla entera del comercio.
- **Indices faltantes:** `sales` solo tiene los FK de una columna (`SHOW INDEX` verificado). Los paths calientes (`sales(commerce_id, client_id, status)`) no pueden usar índice compuesto. `clients` sí lo tiene (`uk_clients_commerce_dni`).
- **Indices declarados en entidades:** cero `@Index` en `model/` — con `ddl-auto=update` y sin Flyway, Hibernate nunca los crea.
- **Bucketing en Java:** `JpaSaleRepository:113-123, 158-168` carga **todos** los `SaleDetail` de la ventana y agrupa en memoria; la última barra semanal del bucket MONTH cubre 2 días contra 7 de las demás.
- **`PeriodRange` recalculado:** `resolveRange` se recalcula en cada sub-service (4 valores de `now` distintos en un mismo request de stats).
- **Redondeo de dinero:** `SalePriceAfterDiscountResolverService:20-21` redondea el porcentaje a 2 decimales **antes** de restar → centavo de diferencia contra `round(subtotal * (1 - d/100))`.
- **Test suite atada a datos sembrados:** `ClientAccountSummaryIntegrationTests:77-80, 101-104, 136-140` usa `getSingleResult()` y `findAll().get(0)` → `NoResultException` / `IndexOutOfBoundsException` sobre una base vacía. Sin Testcontainers ni H2 en el `pom.xml`.
- **Umbral del test de queries:** `ClientAccountSummaryIntegrationTests:134-143` llama `statistics.clear()` y **después** evalúa `clientRepository.findAll()` (2 queries de setup contadas); el `<= 5` actual quedaría en 3 reales → cualquier `@EntityGraph` futuro rompe el test por el motivo equivocado.
- **Cobertura de valores:** el test solo asserta `salesCount` y `totalSpent`; las ramas `PENDING` (`JpaSaleRepository:64, 66`) y los **valores** del endpoint de listado nunca se verifican.
- **`@EntityGraph` / eager loading en general:** la regla del proyecto se cumple en 2 de ~10 finders.
- **Sobre-eager loading** en los mismos finders, que carga relaciones que nadie lee.
- **Rendimiento de la query agregada:** `IN :clientIds` con `Collection<Long>` — el `IN ()` vacío está correctamente protegido por `ClientAccountSummaryService:29-31` (verificado, no es un bug).
- **Producto sin `@Version`** (ya cuartizado en P1-8).
- **N+1 en `FindPendingSaleWithDetailsByClientIdService`** — **resuelto** (P2-17): las ventas de la página
  llegan con sus relaciones cargadas y los detalles salen en una única query batched.
- **Limpieza de mappers/imports sin uso** en varios archivos.
- **Recomendación de layout futuro:** prefijos por comercio en el bucket (`{commerceId}/...`) en lugar del layout plano compartido, y tests de regresión de seguridad (id cross-tenant → 404, CASHIER → 403 en escritura de catálogo, cookie ausente/expirada → 401).

---

## Top 3 acciones

1. **Cerrar el total negativo** (P0-1): capear la oferta `FIXED_AMOUNT` contra el `salePrice` del producto al asignarla, y llevar el guard `signum() < 0` a `ProductDiscountedPriceCalculatorService`, que es el calculador que realmente alimenta el subtotal. Es el único ítem que toca lógica de negocio y conviene cubrirlo con un test antes de tocar código. — **RESUELTO**
2. **Restaurar el contrato de error** (P0-3 + P0-4): agregar `@ExceptionHandler(BindException.class)` y `@ExceptionHandler(HandlerMethodValidationException.class)` al `GlobalExceptionHandler`, devolviendo el mismo `fields[]` con `VALIDATION_ERROR` que ya usa `handleValidationException`. — **RESUELTO**
3. **Arreglar el borrado de archivos** (P0-2): pasar `subDir + "/" + objectName` al deleter, o persistir la object key completa en la entidad y usar esa. — **RESUELTO** (con `FileObjectKey`, que centraliza la clave)

### Wave 1 (hecha)

| Commit | Cierra | Nota |
|---|---|---|
| `docs(offer)` | P0-1 (comentarios obsoletos) | El guard y los capeos ya estaban; faltaba corregir los comentarios que describían el comportamiento viejo |
| `feat(auth)` | P1-5, P2-11 | Opción (c): una query mínima por request |
| `fix(pagination)` | P1-6 | Rechazo con 400 en vez de clampeo silencioso |
| `fix(security)` | P2-12, P2-13, P2-14 | Scoping por comercio + unicidad con exclusión del propio id |
| `chore(config)` | P1-9 (parcial) | Lectura por variables de entorno + `.env` / `.env.example` |

### Pendiente de decidir

- **P1-9**: rotación de los cuatro valores reales y limpieza del historial de git (tarea manual).
- **P1-8**: `@Version` en `Product` o lock pesimista — cambia el esquema y la semántica de las
  transacciones, conviene tratarlo aparte.
- **P1-7**: N+1 de productos y ofertas — optimización, conviene medir queries antes.
- **Regalar un producto**: quedó anotado como feature aparte (no es un descuento). Cuando se
 retome, la auditoría con motivo (eje 3 de P1-10) es el requisito natural.

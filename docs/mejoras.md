# Plan de mejoras — gestiON backend

> Revisión completa del código (rama `amazing-features`, commit `a12202d`, 2026-11-27).
> Documento de análisis y planificación: **no incluye código**. Cada ítem indica el problema, la
> evidencia en el repo, la propuesta y una estimación de impacto/esfuerzo.
>
> Convenciones: **Impacto** Alto / Medio / Bajo · **Esfuerzo** S (horas) / M (1–2 días) / L (más de 2 días).
> Las rutas son relativas a `src/main/java/com/gestion/` salvo que se indique otra cosa.

---

## Resumen ejecutivo

El proyecto tiene una base sólida: capas claras, scoping multi-tenant consistente (`findByIdAndCommerceId` + 404),
manejo de errores centralizado con `ErrorCode`, validación de paginación con allow-list, consultas
agregadas por página para evitar N+1 en listados y revalidación del estado de la cuenta en cada request.

Los riesgos más importantes que encontré son:

1. **Bugs de negocio en cuenta corriente**: las ventas `PARTIAL_PAYED` desaparecen de la deuda del
   cliente y el "total pendiente" ignora los pagos parciales.
2. **Concurrencia en stock y pagos**: el manejo de `OptimisticLockException` nunca se dispara (la
   excepción ocurre en el flush, no en el setter) y los pagos concurrentes pueden sobrepagar una venta.
3. **Pérdida silenciosa de ítems en una venta**: `details` es un `Set` de records, así que dos líneas
   idénticas se colapsan en una antes de llegar al merger.
4. **Secretos por defecto en el repo**: el `jwt.secret` y la contraseña del admin tienen defaults
   versionados; si se despliega sin variables de entorno, cualquiera puede firmar un token `SUDO`.
5. **Cookie de sesión sin `Secure`/`SameSite` y CSRF deshabilitado** con autenticación basada en cookie.
6. **Cobertura de tests prácticamente nula** (solo `contextLoads`).

| Fase | Foco | Ítems | Prioridad |
|---|---|---|---|
| 0 | Bugs críticos de negocio y datos | 10 | Inmediata |
| 1 | Seguridad | 14 | Alta |
| 2 | Modelo de datos e integridad | 10 | Alta |
| 3 | Rendimiento | 10 | Media |
| 4 | Estructura y calidad de código | 14 | Media |
| 5 | Testing, observabilidad y DevOps | 10 | Media (algunos ítems habilitan las fases anteriores) |
| 6 | Evolución funcional | 8 | Baja / según roadmap |

> **Recomendación de orden**: empezar la Fase 5.1–5.3 (tests de integración mínimos) en paralelo con la
> Fase 0, para que cada bug corregido quede cubierto por un test que lo reproduzca.

---

## Fase 0 — Bugs críticos (correctitud de negocio y datos)

> **Estado (2026-11-27): implementada.** Decisiones tomadas en la revisión:
> - 0.1: se resolvió con JPQL y no con `Specification`. La consulta es una agregación de forma fija
>   (`GROUP BY` + `SUM(CASE ...)` + proyección a record); `Specification` aporta para filtros
>   dinámicos sobre entidades, no para proyecciones agregadas. Si el listado de pendientes suma
>   filtros opcionales (fecha, punto de venta), ahí sí conviene migrarlo a `Specification`.
> - 0.2: decremento atómico (`UPDATE ... WHERE current_stock >= :q`).
> - 0.9: `version` obligatoria en `ProductUpdateRequest` (409 `CONCURRENT_MODIFICATION` si no coincide).
> - 0.10: `JwtFilter` arma el principal con un snapshot de la base en cada request; el token solo
>   aporta el `userId`.

### 0.1 Deuda del cliente ignora ventas parcialmente pagadas — Impacto Alto · Esfuerzo M
- **Evidencia**: `repository/JpaSaleRepository.java:51` (`findPendingByClientIdAndCommerceId` filtra solo
  `status = PENDING`) y `:61-63` (`findAccountSummariesByClientIds` cuenta y suma solo `PENDING`, y suma
  `s.total` completo).
- **Problema**: una venta a cuenta corriente con un pago parcial pasa a `PARTIAL_PAYED` y deja de figurar
  en `/api/clients/{id}/pending-sales-with-details` y en `totalPending` del cliente. Además, para las
  `PENDING` que sí cuenta, suma el total completo sin restar pagos. La deuda informada es incorrecta en
  ambos sentidos.
- **Propuesta**: definir "pendiente" como `status IN (PENDING, PARTIAL_PAYED)` en ambas queries y calcular
  `totalPending` como `SUM(total) - SUM(pagos)` (subquery o join agregado sobre `transactions` tipo
  `PAYMENT`). Unificar con `SalePendingCalculatorService`, que ya hace el cálculo correcto por venta.

### 0.2 El control de concurrencia de stock nunca se activa — Impacto Alto · Esfuerzo M
- **Evidencia**: `service/sale/SaleStockUpdaterService.java:20-21` envuelve `setCurrentStock(...)` en un
  `try/catch (OptimisticLockException)`.
- **Problema**: un setter de JPA nunca lanza `OptimisticLockException`; la excepción aparece al hacer
  flush/commit (como `ObjectOptimisticLockingFailureException` de Spring), fuera de ese `try`. Resultado:
  dos ventas simultáneas del mismo producto → una termina en **500 `INTERNAL_ERROR`** en vez de
  `409 CONCURRENT_STOCK_UPDATE`. Además, la validación de stock (`SaleStockValidatorService`) y el
  decremento son "check-then-act" en memoria.
- **Propuesta** (elegir una):
  - **Decremento atómico en SQL**: `UPDATE products SET current_stock = current_stock - :q WHERE id = :id
    AND commerce_id = :c AND current_stock >= :q`, verificando filas afectadas = 1 (más simple y robusto).
  - **Bloqueo pesimista** (`@Lock(PESSIMISTIC_WRITE)`) al cargar los productos de la venta, ordenando
    por id para evitar deadlocks.
  - Mantener `@Version` y mapear `ObjectOptimisticLockingFailureException` en `GlobalExceptionHandler`
    a `CONCURRENT_STOCK_UPDATE` (mínimo indispensable si no se cambia el enfoque).

### 0.3 Pagos concurrentes pueden sobrepagar una venta — Impacto Alto · Esfuerzo S
- **Evidencia**: `service/sale/SalePaymentService.java` lee el pendiente, valida y luego inserta la
  transacción, sin bloqueo sobre la venta. `Sale` no tiene `@Version`.
- **Problema**: dos pagos simultáneos sobre la misma venta pasan ambos la validación `amount <= pending`.
- **Propuesta**: cargar la venta con `PESSIMISTIC_WRITE` en el flujo de pago (o agregar `@Version` a
  `Sale` y mapear el conflicto a 409). Agregar un índice por `transactions.sale_id` (ver 3.x).

### 0.4 Líneas de venta idénticas se pierden — Impacto Alto · Esfuerzo S
- **Evidencia**: `dto/request/sale/SaleRequest.java:19` declara `Set<SaleDetailRequest> details`.
- **Problema**: `SaleDetailRequest` es un record (igualdad por valor). Si el front envía dos líneas
  `{productId: 1, quantity: 2}`, Jackson las colapsa en una sola y `SaleDetailMergerService` suma solo 2
  en vez de 4: se cobra y descuenta menos stock del vendido, sin error.
- **Propuesta**: cambiar a `List<SaleDetailRequest>` (el merger ya agrupa por producto). Agregar
  `@Size(max = N)` razonable.

### 0.5 `updatedAt` nunca se actualiza en `Commerce` y `SellPoint` — Impacto Medio · Esfuerzo S
- **Evidencia**: `model/Commerce.java:46` y `model/SellPoint.java:45` tienen `updatable = false` en
  `updated_at`.
- **Problema**: el campo queda congelado en la fecha de creación, pero los listados ordenan por
  `updatedAt` por defecto (`CommercesWithOwnerGetController`, `SellPointsGetController`).
- **Propuesta**: quitar `updatable = false` de `updated_at` en ambas entidades.

### 0.6 CUIT validado hasta 20 caracteres pero la columna admite 12 — Impacto Medio · Esfuerzo S
- **Evidencia**: `model/Commerce.java:30` (`length = 12`) vs `@Length(min = 8, max = 20)` en
  `CommerceRequest`, `CommerceWithOwnerRequest` y `CommerceWithOwnerUpdateRequest`.
- **Problema**: un CUIT con guiones (`20-12345678-9`, 13 caracteres) pasa la validación y falla en la base
  (error de truncado → 500 o `DATA_INTEGRITY_VIOLATION`).
- **Propuesta**: normalizar el CUIT (solo dígitos), validarlo con `@Pattern` de 11 dígitos (opcional:
  dígito verificador) y alinear la longitud de la columna.

### 0.7 Ordenamiento roto en el listado de comercios (SUDO) — Impacto Medio · Esfuerzo S
- **Evidencia**: `service/commerce/CommerceWithOwnerSearcherService.java:25` pagina sobre `User`
  (`findAllByRoleWithCommerce`) pero el controller valida `sortBy` contra `SortableProperties.COMMERCE`.
- **Problema**: `sortBy=cuit` o `address` no existen en `User` → `PropertyReferenceException` → 400;
  `sortBy=name` ordena por el nombre del **owner**, no del comercio; `createdAt`/`updatedAt` son del usuario.
  Además, un comercio sin owner no aparece nunca.
- **Propuesta**: paginar sobre `Commerce` (con join al owner) o prefijar las propiedades (`commerce.cuit`)
  con un allow-list propio. Renombrar el parámetro `sortOrder` → `sortDir` para alinearlo con el resto.

### 0.8 No se puede crear/actualizar un producto con stock 0 — Impacto Medio · Esfuerzo S
- **Evidencia**: `dto/request/product/ProductRequest.java:33,36` (y `ProductUpdateRequest`) usan
  `@Positive` en `currentStock` y `minStock`.
- **Problema**: impide registrar un producto agotado o corregir el stock a 0 desde la edición, y
  `minStock = 0` (sin alerta) tampoco es posible. Contradice el filtro `WITHOUT_STOCK`.
- **Propuesta**: `@PositiveOrZero` en ambos campos.

### 0.9 La edición de producto pisa el stock sin control — Impacto Medio · Esfuerzo M
- **Evidencia**: `service/product/ProductUpdaterService.java` hace `setCurrentStock(request.currentStock())`.
- **Problema**: si entre que el owner abrió el formulario y lo guardó hubo ventas, el stock vendido se
  "devuelve" silenciosamente. Hoy el `@Version` solo protege contra la carrera en el mismo instante y
  termina en 500 (ver 0.2).
- **Propuesta**: separar el ajuste de stock en un endpoint/caso de uso propio ("ajuste de inventario"
  con delta y motivo), o exigir el `version` en el request de update para detectar edición sobre datos
  viejos (409). Ver también 6.2 (movimientos de stock).

### 0.10 Sell point del cajero tomado del JWT (dato desactualizable) — Impacto Medio · Esfuerzo S
- **Evidencia**: `service/sale/SaleSellPointResolverService.java` usa el `sellPointId` del token con
  `getReference`, y `SellPointUpdaterService` / `SellPointCashiersAssignerService` reasignan cajeros.
- **Problema**: si el owner mueve a un cajero de punto de venta, o desactiva el punto de venta, el cajero
  sigue registrando ventas en el punto anterior hasta 24 h (vida del token). No se valida que el punto de
  venta esté activo.
- **Propuesta**: resolver el sell point desde la base en la creación de la venta (es una sola query por
  venta) validando pertenencia al comercio y `active = true`; o incluir el `sellPointId` en la
  revalidación del `JwtFilter`. El comentario "el id viene del JWT" del AGENTS.md sigue siendo válido
  para `userId`/`commerceId`, no para datos que el owner puede cambiar.

---

## Fase 1 — Seguridad

### 1.1 Secretos y credenciales por defecto versionados — Impacto Alto · Esfuerzo S
- **Evidencia**: `src/main/resources/application.properties:17` (`jwt.secret` con default fijo) y `:30`
  (`admin.password=admin123`); también MinIO `admin/admin123` en properties y `docker-compose.yml`.
- **Problema**: si el despliegue omite una variable de entorno, la app arranca con el secret público del
  repo → cualquiera puede firmar un JWT con `role=SUDO`. Lo mismo con el admin.
- **Propuesta**: sin default para `JWT_SECRET` ni `ADMIN_PASSWORD` fuera de un perfil `dev`; validar al
  arranque (`@Validated` en `JwtProperties`/`AdminProperties` con largo mínimo del secret ≥ 32 bytes) y
  fallar rápido. Rotar el secret actual. Mover defaults de desarrollo a `application-dev.properties`.

### 1.2 Cookie de sesión: `Secure=false`, sin `SameSite`, duración duplicada — Impacto Alto · Esfuerzo S
- **Evidencia**: `controller/auth/AuthLoginPostController.java:33-35`, `AuthLogoutController.java:20-21`.
- **Problema**: el token viaja en HTTP plano si no hay TLS; sin `SameSite` explícito depende del default
  del navegador; `Max-Age=86400` está hardcodeado y puede divergir de `jwt.expiration`.
- **Propuesta**: construir la cookie con `ResponseCookie` (soporta `SameSite`), con `Secure` y `SameSite`
  configurables por propiedad (`true`/`Strict` o `Lax` en prod), y `Max-Age` derivado de
  `jwt.expiration`. Centralizar creación/borrado en un único componente.

### 1.3 CSRF deshabilitado con autenticación por cookie — Impacto Alto · Esfuerzo M
- **Evidencia**: `config/SecurityConfig.java` (`csrf(AbstractHttpConfigurer::disable)`).
- **Problema**: al usar cookie (no header), el navegador la adjunta automáticamente. Hoy la protección
  depende solo de CORS + `SameSite` por defecto del navegador; requests "simples" (form POST,
  `multipart/form-data`, que este backend acepta con `@ModelAttribute`) no hacen preflight.
- **Propuesta**: (a) `SameSite=Strict/Lax` (1.2) como primera barrera, y (b) habilitar CSRF con
  `CookieCsrfTokenRepository` (patrón double-submit, el SPA reenvía `X-XSRF-TOKEN`), o como mínimo exigir
  un header custom en métodos no seguros. Coordinar con el front.

### 1.4 Sin protección contra fuerza bruta en el login — Impacto Alto · Esfuerzo M
- **Evidencia**: `controller/auth/AuthLoginPostController.java`, `service/auth/AuthLoginService.java`.
- **Propuesta**: rate limiting por IP y por email (p. ej. Bucket4j o un filtro simple con caché), y
  bloqueo temporal tras N intentos fallidos. Responder `429` con un `ErrorCode` nuevo.

### 1.5 Enumeración de usuarios por timing en el login — Impacto Bajo · Esfuerzo S
- **Evidencia**: `service/auth/AuthLoginService.java`: si el email no existe se responde sin ejecutar
  BCrypt; si existe se ejecuta `matches` (~100 ms).
- **Propuesta**: ejecutar un `matches` contra un hash dummy cuando el usuario no existe, para igualar
  tiempos. (El mensaje ya es genérico; esto completa la intención.)

### 1.6 Política de contraseñas muy débil — Impacto Medio · Esfuerzo S
- **Evidencia**: `@Size(min = 3, ...)` en `CashierRequest`, `UserRequest`, `UserLoginRequest`,
  `CommerceWithOwnerRequest` (este último sin máximo).
- **Propuesta**: mínimo 8 caracteres, máximo 72 bytes (límite real de BCrypt; hoy
  `CommerceWithOwnerRequest` no tiene máximo y BCrypt trunca/falla), y no aplicar la regla de largo al
  **login** (solo `@NotBlank`), para no revelar la política ni rechazar contraseñas viejas.

### 1.7 Logout sin revocación y tokens que sobreviven a cambios de contraseña — Impacto Medio · Esfuerzo M
- **Evidencia**: README (sección Autenticación) y `AuthLogoutController`; `CashierUpdaterService` cambia
  la contraseña sin invalidar sesiones.
- **Propuesta**: agregar a `User` un `tokenVersion` (o `credentialsChangedAt`) que viaje como claim y se
  compare en la misma query de revalidación del `JwtFilter` (costo extra nulo). Incrementarlo en logout
  "de todos los dispositivos", cambio de contraseña, cambio de rol o de sell point. Reducir la vida del
  token (p. ej. 8–12 h) o introducir refresh token.

### 1.8 Nombre de archivo del usuario en la clave del objeto MinIO — Impacto Medio · Esfuerzo S
- **Evidencia**: `service/file/FileUploaderService.java:36`
  (`UUID.randomUUID() + "-" + file.getOriginalFilename()`).
- **Problema**: el nombre original puede contener `/`, `..`, caracteres de control o ser muy largo
  (la columna `logoName`/`imageName` es `VARCHAR(255)`); también filtra nombres de archivos del cliente.
- **Propuesta**: usar solo `UUID` + extensión derivada del MIME detectado por Tika
  (`image/png` → `.png`).

### 1.9 Logs con información sensible y verbosidad de desarrollo — Impacto Medio · Esfuerzo S
- **Evidencia**: `application.properties:2` (`logging.level.org.springframework.security=DEBUG`), `:13`
  (`spring.jpa.show-sql=true`); mensajes como `"User with email X not found"` o `"Client with DNI X"`
  se loguean en `GlobalExceptionHandler.buildResponse`.
- **Propuesta**: mover DEBUG y `show-sql` a un perfil `dev`; en prod usar `INFO`. Evitar PII (email, DNI)
  en mensajes que se loguean o enmascararla.

### 1.10 CORS hardcodeado — Impacto Medio · Esfuerzo S
- **Evidencia**: `config/CorsConfig.java` (`http://localhost:5173`, `allowedHeaders("*")`).
- **Propuesta**: orígenes por propiedad/variable de entorno; restringir `allowedHeaders` a los que usa el
  front (`Content-Type`, `X-XSRF-TOKEN`). `exposedHeaders("Set-Cookie")` es innecesario (los navegadores
  nunca exponen `Set-Cookie` a JS): quitarlo.

### 1.11 `JwtFilter` registrado dos veces y ejecutado en el login — Impacto Bajo · Esfuerzo S
- **Evidencia**: `config/JwtFilter.java` es `@Component` (Spring Boot lo registra también como filtro del
  servlet container) y además se agrega a la cadena de seguridad.
- **Problema**: hoy `OncePerRequestFilter` evita la doble ejecución, pero es frágil. Además corre en
  `POST /api/auth/login`: una cookie de un usuario eliminado devuelve 401 **en el propio login**,
  impidiendo volver a loguearse hasta borrar la cookie.
- **Propuesta**: desactivar el auto-registro (`FilterRegistrationBean` con `enabled=false`) y
  `shouldNotFilter` para el endpoint de login. Parsear el JWT una sola vez (hoy `isTokenValid` +
  `getClaimsFromToken` lo verifican dos veces) y capturar claims faltantes/malformados como 401 en vez
  de dejar escapar un NPE.

### 1.12 Validaciones faltantes en parámetros de endpoints — Impacto Medio · Esfuerzo S
- **Evidencia**:
  - `controller/sale/SalePaymentPostController.java:28`: `@RequestBody` sin `@Valid` (el DTO tiene
    `@NotNull`/`@DecimalMin` que nunca se evalúan; el service compensa, pero la convención se rompe).
  - `controller/sale/TopProductsSoldGetController.java:29`: `@Min(1)` sin `@Max` → `limit=1000000`.
  - `controller/cashier/CashiersGetController.java:28`: recibe `Pageable` crudo, sin pasar por
    `PageableFactory` (sin allow-list de `sortBy`, tamaño máximo por defecto de Spring).
  - `OfferProductIdsRequest.productIds` y `SellPointRequest.cashierIds` sin límite de tamaño.
  - `SaleRequest.details` sin `@Size(max)`.
- **Propuesta**: aplicar `@Valid`, `@Max`, `@Size(max)` y migrar `CashiersGetController` a
  `PageableFactory` + `SortableProperties.USER`.

### 1.13 Revelación de información en `IllegalArgumentException` → 400 — Impacto Bajo · Esfuerzo S
- **Evidencia**: `exception/handler/GlobalExceptionHandler.java:140` mapea **toda**
  `IllegalArgumentException` a `400 INVALID_PARAMETER`.
- **Problema**: un bug interno (p. ej. `PeriodRangeService` lanza `IllegalArgumentException`) se reporta
  como error del cliente y se loguea como warning, ocultando fallos reales.
- **Propuesta**: limitar el handler a `PropertyReferenceException` y a excepciones propias; dejar que las
  `IllegalArgumentException` genéricas caigan en el 500 con log de error.

### 1.14 Encabezados de seguridad y HTTPS — Impacto Bajo · Esfuerzo S
- **Propuesta**: revisar los headers por defecto de Spring Security (HSTS solo aplica sobre HTTPS),
  agregar `Content-Security-Policy` mínima para respuestas de error, y documentar que el despliegue debe
  ir detrás de TLS (reverse proxy) con `server.forward-headers-strategy` configurado.

---

## Fase 2 — Modelo de datos e integridad

### 2.1 Migraciones versionadas en lugar de `ddl-auto=update` — Impacto Alto · Esfuerzo M
- **Evidencia**: `application.properties` (`spring.jpa.hibernate.ddl-auto=update`). El commit `13fe5d1`
  ya tuvo que esquivar una limitación de `update` con enums.
- **Problema**: `update` no elimina columnas, no cambia tipos/longitudes, no crea constraints sobre datos
  existentes y no es reproducible entre entornos. Varios ítems de este plan (0.5, 0.6, 2.2–2.5) requieren
  cambios de esquema que `update` no aplicará.
- **Propuesta**: introducir **Flyway**: generar una baseline `V1` desde el esquema actual, pasar a
  `ddl-auto=validate` y versionar cada cambio posterior.

### 2.2 Unicidad garantizada solo en la aplicación — Impacto Medio · Esfuerzo S
- **Evidencia**: `CategoryValidatorByNameService`, `SellPointValidatorByNameService` validan con `exists...`
  pero `categories` y `sell_points` no tienen unique `(commerce_id, name)`.
- **Problema**: race condition check-then-insert → duplicados. (`clients` sí tiene `uk_clients_commerce_dni`.)
- **Propuesta**: unique constraints `(commerce_id, name)` (case-insensitive vía collation `_ci`, que es el
  default de MySQL) y mapear la violación al `ErrorCode` específico.

### 2.3 Claves foráneas obligatorias declaradas como opcionales — Impacto Medio · Esfuerzo S
- **Evidencia**: `commerce_id` sin `nullable = false` en `Product`, `Offer`, `Sale`; `Sale.user`,
  `SaleDetail.sale/product`, `Transaction.sale`, `Sale.status`, `Product.createdAt` también son nullables.
- **Propuesta**: marcar `nullable = false` donde el negocio lo exige (todas las anteriores). Es la base
  del aislamiento multi-tenant: una fila sin `commerce_id` es invisible e inconsistente.

### 2.4 Dinero y multiplicadores en `Double` — Impacto Medio · Esfuerzo M
- **Evidencia**: `Commerce.profitMultiplier`, `Product.profitMultiplier` y los DTOs asociados son
  `Double`; `JpaSaleRepository.averageSaleValueByPeriod` devuelve `Double`.
- **Propuesta**: `BigDecimal` con `precision/scale` explícitos (p. ej. `(6,4)` para multiplicadores),
  y `BigDecimal` en el promedio. Agregar límites razonables (`@DecimalMax`) al multiplicador.

### 2.5 Snapshot incompleto en `SaleDetail` — Impacto Medio · Esfuerzo S
- **Evidencia**: `model/SaleDetail.java` guarda `price`, descuento y tipo, pero no el **costo** ni el
  nombre del producto al momento de la venta.
- **Problema**: la "ganancia" real no se puede calcular históricamente (el costo cambia) y un producto
  renombrado altera ventas pasadas. Hoy `ProfitByPeriodService` en realidad reporta **ingresos** (suma de
  `total`), no ganancia.
- **Propuesta**: agregar `unitCost` (y opcionalmente `productName`) al detalle; renombrar el campo de
  la respuesta a `revenue` o calcular ganancia real como `Σ(price − unitCost) × qty`.

### 2.6 Índices para las consultas reales — Impacto Medio · Esfuerzo S
- **Evidencia**: no hay `@Index` en ninguna entidad. Consultas frecuentes: ventas por
  `(commerce_id, status, created_at)` (stats, listados), `transactions(sale_id, transaction_type)`,
  `products(commerce_id, active, current_stock)`, `offers(commerce_id, offer_type, start_date, end_date)`,
  `users(commerce_id, role)`.
- **Propuesta**: crear índices compuestos vía migración (2.1). MySQL crea índices simples por FK, pero no
  los compuestos que necesitan los filtros + rangos de fechas.

### 2.7 Enum `PAYED` / `PARTIAL_PAYED` — Impacto Bajo · Esfuerzo M
- **Problema**: el inglés correcto es `PAID` / `PARTIALLY_PAID` (también `SALE_ALREADY_PAYED`). Es un
  contrato público con el front.
- **Propuesta**: renombrar con migración de datos coordinada con el front; si no es viable, documentarlo
  como deuda conocida.

### 2.8 Relación oferta–producto 1:N limitante — Impacto Bajo · Esfuerzo L
- **Evidencia**: `Product.offer` (`@ManyToOne`): un producto tiene a lo sumo una oferta. Para programar
  una oferta futura hay que reemplazar la vigente.
- **Propuesta** (evaluar con negocio): tabla `offer_products` N:M y resolver la oferta activa por fecha.
  Mantener la regla de no-solapamiento por producto.

### 2.9 Reglas de solapamiento de ofertas de producto poco intuitivas — Impacto Bajo · Esfuerzo S
- **Evidencia**: `service/offer/OfferOverlapValidatorService.java`: dos ofertas de producto solapadas solo
  se rechazan si tienen **el mismo valor y tipo**.
- **Propuesta**: definir con negocio la regla real (probablemente el solapamiento relevante es por
  producto asignado, y se valida al asignar en `OfferAssignerProductService`) y documentarla.

### 2.10 Auditoría mínima — Impacto Medio · Esfuerzo M
- **Problema**: `Transaction` no registra qué usuario cobró el pago; no hay registro de quién cambió
  precios, stock o activó/desactivó entidades.
- **Propuesta**: agregar `user_id` a `transactions`; considerar Spring Data Auditing (`@CreatedBy`,
  `@LastModifiedBy`) con un `AuditorAware` que lea el `UserPrincipal`.

---

## Fase 3 — Rendimiento

### 3.1 N+1 sobre la oferta al validar productos de una venta — Impacto Medio · Esfuerzo S
- **Evidencia**: `JpaProductRepository.findByIdInAndCommerceId` (línea 21) no trae `offer`;
  `ProductActiveOfferFinderService` accede a `product.getOffer().getOfferType()` por cada producto.
- **Propuesta**: `@EntityGraph(attributePaths = "offer")` en ese método (o una variante específica para
  ventas). Si se adopta el bloqueo pesimista de 0.2, combinarlo en la misma query.

### 3.2 Estadísticas agrupadas en memoria — Impacto Medio · Esfuerzo M
- **Evidencia**: `JpaSaleRepository.saleDetailsInPeriod` (línea 118) carga **todas** las entidades
  `SaleDetail` + `Sale` del período (hasta 30 días) y los `*ProductGrouper` agrupan en Java.
- **Propuesta**: agregar en SQL (`GROUP BY` por hora/día/semana con `CONVERT_TZ` a la zona del negocio)
  y devolver proyecciones `(bucket, cantidad)`. Reduce memoria y tráfico de forma lineal con el volumen.

### 3.3 Inserción de detalles una por una (y sin batching posible) — Impacto Bajo · Esfuerzo S
- **Evidencia**: `service/saledetail/SaleDetailCreatorService.java` hace `save` en loop; todas las
  entidades usan `GenerationType.IDENTITY`, que desactiva el batch insert de Hibernate.
- **Propuesta**: `saveAll` como mínimo. Si el volumen lo justifica, evaluar `SEQUENCE`/tabla de
  secuencias con `hibernate.jdbc.batch_size`. Para el tamaño típico de un ticket el impacto es bajo.

### 3.4 Query de revalidación en cada request — Impacto Bajo · Esfuerzo S
- **Evidencia**: `config/JwtFilter.java` + `UserActiveStatusFinderService` (con `@Transactional()` de
  escritura).
- **Propuesta**: marcarlo `readOnly = true`. Si la carga crece, caché local (Caffeine) con TTL corto
  (5–15 s) e invalidación explícita al hacer toggle-active; documentar el trade-off con el corte inmediato.

### 3.5 Búsquedas `LIKE '%texto%'` sobre múltiples columnas — Impacto Medio (a escala) · Esfuerzo M
- **Evidencia**: `findAllWithFilters` de productos, clientes, ventas y cajeros (`LOWER(...) LIKE
  LOWER(CONCAT('%', :x, '%'))`, incluso `CAST(s.id AS STRING) LIKE`).
- **Problema**: no usa índices; `LOWER()` es redundante con collations `_ci` de MySQL e impide índices.
- **Propuesta**: quitar `LOWER` (confiando en la collation), usar prefijo (`texto%`) donde tenga sentido
  (DNI, id de venta como igualdad exacta), y evaluar `FULLTEXT` para nombre/descripción de producto.

### 3.6 `count` derivado de queries con `JOIN FETCH` — Impacto Bajo · Esfuerzo S
- **Evidencia**: `JpaProductRepository.findAllWithFilters`, `JpaSaleRepository.findAllWithFilters`,
  `JpaUserRepository.findAllCashiersWithFilters` paginan con `JOIN FETCH` sin `countQuery`.
- **Propuesta**: declarar `countQuery` explícita sin fetch (más eficiente y evita errores de
  derivación de Spring Data ante cambios futuros).

### 3.7 Open Session In View activo por defecto — Impacto Medio · Esfuerzo M
- **Evidencia**: `spring.jpa.open-in-view` no está configurado (default `true`, Spring loguea un warning).
- **Problema**: mantiene la conexión durante toda la request (incluida la serialización) y oculta lazy
  loads accidentales (p. ej. `SaleWithDetailsFinderService` no tiene `@Transactional`).
- **Propuesta**: `spring.jpa.open-in-view=false` **después** de tener tests de integración (Fase 5),
  corrigiendo los servicios que dependan de lazy loading fuera de transacción.

### 3.8 Pool de conexiones y logging SQL — Impacto Bajo · Esfuerzo S
- **Propuesta**: configurar HikariCP (`maximum-pool-size`, `connection-timeout`), quitar `show-sql` en
  prod (1.9) y, si hace falta medir, usar `datasource-proxy` o logs de Hibernate con estadísticas en dev.

### 3.9 URLs pre-firmadas regeneradas en cada listado — Impacto Bajo · Esfuerzo S
- **Evidencia**: `FileFinderService.getObjectUrl` se llama por cada producto en cada página.
- **Problema**: el costo de CPU es bajo, pero cada request devuelve una URL distinta → el navegador no
  cachea las imágenes y las vuelve a descargar.
- **Propuesta**: si las imágenes de productos no son sensibles, bucket/prefijo público de solo lectura con
  URL estable; si deben ser privadas, cachear la URL firmada por objeto durante parte de su expiración.

### 3.10 Upload de archivos dentro de transacciones de base — Impacto Medio · Esfuerzo M
- **Evidencia**: `ProductCreatorService`, `ProductImageUploaderService`, `CommercePartialUpdaterService`,
  `CommerceUpdaterService`, `CommerceWithOwnerCreatorService`.
- **Problema**: (a) la transacción y la conexión quedan abiertas mientras se sube a MinIO; (b) si la
  transacción hace rollback, el archivo queda huérfano; (c) se **borra la imagen anterior antes** de
  subir la nueva: si el upload falla, la entidad queda apuntando a un archivo inexistente.
- **Propuesta**: subir primero, persistir la referencia, y borrar la imagen anterior **después del commit**
  (`TransactionSynchronization.afterCommit` o `@TransactionalEventListener(AFTER_COMMIT)`); en rollback,
  borrar el archivo recién subido. Centralizar en un único servicio de "reemplazo de imagen".

---

## Fase 4 — Estructura y calidad de código

### 4.1 Lógica duplicada — Impacto Medio · Esfuerzo S
- `ProductPriceWithOfferResolver` y `SaleDetailPriceCalculator.calculatePrice` son idénticos → uno solo.
- `ProductFinderByIdAndCommerceIdService.findProductsByIdsAndCommerceId` y
  `ProductFinderByIdsAndCommerceIdService` resuelven lo mismo con distinto error.
- `writeError` duplicado en `JwtFilter` y `SecurityConfig` → un `SecurityErrorWriter` común.
- Lógica de "registrar pago y recalcular estado" duplicada en `SalePaymentService` y
  `SaleTransactionService` → un único caso de uso.
- Tres servicios que actualizan comercio (`CommerceUpdaterService`, `CommercePartialUpdaterService`,
  `CommerceProfitMultiplierUpdaterService`) con manejo de logo repetido.

### 4.2 Nombres que no siguen la convención del proyecto — Impacto Bajo · Esfuerzo S
- Sin sufijo `Service`: `ProductImageUpdater`, `ProductUpdateValidator`, `ProductPriceWithOfferResolver`,
  `OfferProductPriceValidator` (convive con `OfferProductPriceValidatorService`), `SaleDetailPriceCalculator`,
  `HourlyProductGrouper`/`DailyProductGrouper`/`WeeklyProductGrouper`.
- `AuthLogoutController` / `AuthCheckController` no siguen `Resource + Verbo + Controller`
  (`AuthLogoutPostController`, `AuthMeGetController`).
- `SalesStatsByProfitGetterService` devuelve estadísticas por período, no "por ganancia".
- `CommerceAvailabilityValidatorService` solo verifica `commerceId != null`; no valida disponibilidad
  (activo). Renombrar o hacer que valide realmente.

### 4.3 Configuración hardcodeada repartida — Impacto Medio · Esfuerzo S
- Zona horaria `America/Argentina/Buenos_Aires` repetida en 4 clases (`PeriodRangeService`, los 3
  groupers) → una propiedad `app.business-zone` (o, a futuro, por comercio).
- Límite de 10 MB duplicado en `FileUploaderService` y `application.properties` → leerlo de
  `spring.servlet.multipart.max-file-size`.
- Nombre de cookie `"token"` y su duración en varios lugares → `JwtProperties`/`CookieProperties`.
- `EXPIRY_HOURS` de URLs firmadas → propiedad de MinIO.

### 4.4 Granularidad de servicios — Impacto Medio · Esfuerzo M
- **Observación**: la convención "una clase por caso de uso" está bien aplicada, pero hay ~130 clases de
  servicio y varias son funciones puras de una línea sin dependencias (`ClientSearchTextNormalizerService`,
  `SaleProductValidatorService`, `SaleStatusResolverService`, `SalePaymentMethodValidatorService`,
  `OfferStatusResolverService`, `SaleRevenueOutflowCalculatorService`).
- **Propuesta** (sin romper la convención para casos de uso): mover reglas puras del dominio a métodos de
  la entidad o a *value objects* (p. ej. `Offer.statusAt(now)`, `Sale.isSettled()`,
  `Money/Discount.apply(...)`), y dejar como `@Service` solo lo que orquesta o accede a infraestructura.
  Facilita mucho el testing unitario.

### 4.5 Uso inconsistente de `@Transactional` — Impacto Medio · Esfuerzo S
- Lecturas sin `readOnly`: `UserActiveStatusFinderService`, `OfferFinderByPaymentMethodAndCommerceIdService`
  (`@Transactional()`), stats de ventas, `SaleWithDetailsFinderService`, `CommerceCurrentFinderService`,
  `CommerceWithOwnerSearcherService`, `CashierSearcherService`.
- `ClientUpdaterService` no llama a `save` (funciona por dirty checking) mientras el resto sí: elegir un
  criterio y aplicarlo de forma uniforme.
- `ProductCreatorService` llama a `save` dos veces (antes y después de la imagen).

### 4.6 Nombres totalmente calificados y código muerto — Impacto Bajo · Esfuerzo S
- `com.gestion.enums.PaymentMethod`, `java.time.Instant`, `java.time.temporal.ChronoUnit` inline en
  `SaleTransactionService`, `JpaSaleRepository`, servicios de ofertas.
- `TransactionCreatorService` con variable no usada y `return` comentado; `TransactionRequest` solo
  referenciado desde `TransactionMapper` (no hay endpoint que lo reciba); `JpaCategoryRepository.findAllByCommerceId`
  sin uso. Hacer una pasada de "find usages" antes de borrar.
- Comentario en inglés/español mezclado ("arrives como").

### 4.7 Tiempo "ahora" no inyectable — Impacto Medio · Esfuerzo S
- **Evidencia**: `Instant.now()` disperso en servicios de ofertas, ventas y stats;
  `SalesStatsByProfitGetterService` resuelve el rango 4 veces con `now` distintos.
- **Propuesta**: bean `Clock` inyectado; resolver el `PeriodRange` una sola vez por request. Habilita
  tests deterministas de ofertas y períodos.

### 4.8 Precisión de descuentos inconsistente — Impacto Bajo · Esfuerzo S
- **Evidencia**: `ProductDiscountedPriceCalculatorService` divide con escala 4 y redondea a 2;
  `SalePriceAfterDiscountResolverService` divide directamente con escala 2.
- **Propuesta**: un único componente de cálculo monetario con una política de redondeo documentada.

### 4.9 Contrato de respuestas — Impacto Bajo · Esfuerzo S
- `AuthLogoutController` devuelve `ResponseEntity<String>` (texto plano) → `204 No Content`.
- `OfferProductsPutController` devuelve `200` vacío → `204`.
- `SalePaymentPostController` devuelve `200` al crear un pago → `201`.
- `Page<T>` serializado directamente (Spring Data avisa que su JSON no es estable) → usar
  `PagedModel` / `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` o un DTO de página propio.

### 4.10 Endpoint `/api/auth/me` con consulta redundante — Impacto Bajo · Esfuerzo S
- **Evidencia**: `AuthCheckController` + `AuthCheckerStatusService` cargan el `User` completo por email
  para chequear el comercio, algo que el `JwtFilter` ya validó en la misma request.
- **Propuesta**: eliminar el chequeo duplicado. Ídem en `AuthLoginService` (`isCommerceUserActive` vuelve
  a buscar al usuario que ya tiene cargado).

### 4.11 Mapeo de entidades con Lombok — Impacto Bajo · Esfuerzo S
- `@AllArgsConstructor` en entidades JPA invita a construir entidades con orden posicional frágil
  (`OfferMapper.toEntity` usa `new Offer(null, commerce, ...)`). Preferir builders o setters explícitos.
- Sin `equals/hashCode` definidos: correcto para entidades, pero `SellPoint.users` es `Set<User>` y
  `.distinct()` en `OfferAssignerProductService` depende de identidad; dejarlo documentado.

### 4.12 Documentación de API — Impacto Medio · Esfuerzo S
- **Propuesta**: `springdoc-openapi` (compatible con la versión de Boot) para exponer el contrato
  (protegido o solo en dev). El README hoy cumple ese rol a mano y tiende a desactualizarse (el AGENTS.md
  ya marca una afirmación vieja del README).

### 4.13 Repositorio — Impacto Bajo · Esfuerzo S
- `.archify/` y `skills-lock.json` versionados: evaluar si corresponde al repo.
- `.gitignore` ignora `*.txt` globalmente: puede ocultar archivos legítimos (p. ej. fixtures de test).
- `pom.xml` con metadatos vacíos (`<licenses>`, `<developers>`, `<scm>`) y `name/description`
  genéricos (`proyecto-final`).

### 4.14 Dependencias — Impacto Medio · Esfuerzo S
- `okhttp` fijado manualmente a `4.12.0` y `tika-core` `2.9.2`: verificar compatibilidad con
  `minio 9.x` y si hay versiones con parches de seguridad; preferir que el BOM gestione versiones.
- Activar Dependabot/Renovate (o `versions-maven-plugin`) y un escaneo de vulnerabilidades
  (OWASP Dependency-Check).

---

## Fase 5 — Testing, observabilidad y DevOps

### 5.1 Infraestructura de tests de integración — Impacto Alto · Esfuerzo M
- **Evidencia**: único test `ProyectoFinalApplicationTests.contextLoads`, que además requiere un MySQL
  local levantado.
- **Propuesta**: **Testcontainers** (MySQL + MinIO) con `@ServiceConnection`; perfil `test`. Permite que
  los tests corran en cualquier máquina y en CI.

### 5.2 Tests de los flujos de dinero — Impacto Alto · Esfuerzo M
- Prioridad: creación de venta (descuentos por producto y por medio de pago, regalo, cuenta corriente con
  pago parcial, stock insuficiente, líneas duplicadas — ver 0.4), registro de pagos (sobrepago, venta
  pagada, concurrencia — ver 0.3), cuenta corriente del cliente (ver 0.1) y estadísticas por período.
- Las reglas puras (4.4) se cubren con tests unitarios sin Spring.

### 5.3 Tests de seguridad y aislamiento multi-tenant — Impacto Alto · Esfuerzo M
- `MockMvc` + `spring-security-test`: para cada endpoint, matriz de rol permitido/denegado y un test
  "comercio A no ve/edita recursos de comercio B → 404". Tests del `JwtFilter` (usuario desactivado → 403,
  inexistente → 401, token expirado/firmado con otra clave → 401).

### 5.4 CI — Impacto Medio · Esfuerzo S
- **Propuesta**: GitHub Actions con `./mvnw verify` (compile + tests con Testcontainers), caché de Maven y
  reporte de cobertura (JaCoCo). Opcional: Spotless/Checkstyle para formato consistente.

### 5.5 Perfiles de configuración — Impacto Medio · Esfuerzo S
- `application.properties` (común), `application-dev.properties` (defaults de dev, DEBUG, show-sql),
  `application-prod.properties` (sin defaults de secretos, `ddl-auto=validate`, cookie `Secure`).
  Hoy no hay perfiles (AGENTS.md lo confirma).

### 5.6 Health checks y métricas — Impacto Medio · Esfuerzo S
- **Propuesta**: `spring-boot-starter-actuator` exponiendo solo `health` (con indicadores de DB y MinIO)
  y, si se monitorea, `metrics`/`prometheus` protegidos por rol o por red.

### 5.7 Logging estructurado y trazabilidad — Impacto Medio · Esfuerzo S
- **Propuesta**: correlation id por request (MDC, header `X-Request-Id`), incluir `userId`/`commerceId`
  en el MDC tras autenticar, y formato JSON en prod. Registrar eventos de seguridad (login fallido,
  cuenta deshabilitada) en un logger dedicado.

### 5.8 Contenerización completa — Impacto Medio · Esfuerzo S
- **Evidencia**: `docker-compose.yml` solo levanta MinIO; MySQL debe instalarse a mano.
- **Propuesta**: agregar MySQL 8 (con volumen y healthcheck) al compose, leer credenciales de `.env`
  (hoy MinIO tiene `admin/admin123` hardcodeado en el compose) y un `Dockerfile` multi-stage de la app
  (o `spring-boot:build-image`). Revisar el tag `pgsty/minio` (imagen de terceros, el comentario dice
  "verifica el tag").

### 5.9 Inicialización del admin — Impacto Bajo · Esfuerzo S
- **Evidencia**: `config/AdminInitializer.java` crea el SUDO si no existe el email configurado.
- **Problema**: cambiar `ADMIN_EMAIL` crea un segundo SUDO sin desactivar el anterior; con defaults crea
  `admin@admin.com/admin123`.
- **Propuesta**: no crear el admin sin credenciales explícitas (1.1); opcionalmente forzar cambio de
  contraseña en el primer login; considerar mover el seed a una migración.

### 5.10 Documentación operativa — Impacto Bajo · Esfuerzo S
- Mantener README/AGENTS.md sincronizados con cada fase (variables nuevas, perfiles, cómo correr tests con
  Testcontainers, política de cookies/CSRF para el front).

---

## Fase 6 — Evolución funcional (según roadmap)

| # | Mejora | Motivo |
|---|---|---|
| 6.1 | **Anulación/devolución de ventas** (estado `CANCELLED`, reposición de stock, transacción de reverso) | Hoy no hay forma de corregir una venta errónea. |
| 6.2 | **Movimientos de stock** (tabla `stock_movements`: venta, ajuste, ingreso, devolución) | Trazabilidad del inventario; resuelve 0.9. |
| 6.3 | **Cambio de contraseña propio** y reset por SUDO/OWNER | No existe endpoint para que un owner cambie su contraseña. |
| 6.4 | **Cierre de caja por punto de venta** (apertura, cierre, arqueo) | Natural en un POS con `sell_points` y cajeros. |
| 6.5 | **Zona horaria y moneda por comercio** | Reemplaza la zona hardcodeada (4.3). |
| 6.6 | **Exportación de reportes** (CSV/PDF de ventas y deuda de clientes) | Uso habitual del owner. |
| 6.7 | **Ganancia real por período** | Requiere el snapshot de costo (2.5). |
| 6.8 | **Notificaciones de stock bajo** | Ya existe la query `findLowStockProductsByCommerceId`. |

---

## Hoja de ruta sugerida

1. **Sprint 1** — 5.1 (Testcontainers) · 0.1 a 0.6 · 1.1 · 1.2 · 1.12.
2. **Sprint 2** — 0.7 a 0.10 · 1.3 · 1.4 · 1.6 · 1.8 · 1.9 · 5.2 · 5.3 · 5.5.
3. **Sprint 3** — 2.1 (Flyway, prerequisito del resto de la Fase 2) · 2.2 a 2.6 · 3.1 · 3.10 · 5.4.
4. **Sprint 4** — 3.2 · 3.5 · 3.6 · 3.7 · Fase 4 (refactors, ya con red de tests) · 5.6 a 5.8.
5. **Backlog** — 1.7 · 2.7 a 2.10 · resto de la Fase 3 · Fase 6.

## Fortalezas a conservar

- Scoping multi-tenant uniforme con 404 (no 403) para recursos de otro comercio.
- `ErrorCode` + `BusinessException` + `GlobalExceptionHandler`: contrato de errores estable para el front.
- `PageableFactory` + `SortableProperties`: rechazo explícito de parámetros inválidos.
- Agregaciones por página (`countItemsBySaleIds`, `sumPaymentsBySaleIds`, `findAccountSummariesByClientIds`)
  que evitan N+1 en listados.
- Revalidación de `active` en cada request: corte inmediato al desactivar usuario/comercio.
- Detección de MIME por contenido (Tika) con allow-list en los uploads.
- Comentarios que explican el *porqué* de decisiones no obvias (p. ej. `FileObjectKey`).

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
| `CLIENT_NOT_FOUND` | 404 | Cliente inexistente por DNI |
| `CATEGORY_NOT_FOUND` | 404 | Categoría inexistente |
| `PRODUCT_NOT_FOUND` | 404 | Producto inexistente |
| `COMMERCE_NOT_FOUND` | 404 | Comercio inexistente |
| `SALE_NOT_FOUND` | 404 | Venta inexistente |
| `ENDPOINT_NOT_FOUND` | 404 | Ruta inexistente |
| `EMAIL_ALREADY_EXISTS` | 409 | Email de usuario ya registrado |
| `CLIENT_DNI_ALREADY_EXISTS` | 409 | DNI de cliente ya registrado |
| `COMMERCE_ADDRESS_ALREADY_EXISTS` | 409 | Dirección de comercio ya registrada |
| `COMMERCE_CUIT_ALREADY_EXISTS` | 409 | CUIT de comercio ya registrado |
| `DATA_INTEGRITY_VIOLATION` | 409 | Violación de restricción de la base de datos |
| `VALIDATION_ERROR` | 400 | Fallo de validación del body (incluye `fields[]`) |
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


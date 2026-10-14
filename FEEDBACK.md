# Cambios

## Modelos
- En `Order.java` falta **@Table(name="orders")** importante que sea orders en plurar. (**order** es una plabra reservada de MySQL)
- OrderDetail.java falta **@Table(name="order_details")**

## DTOs
- `OrderDetailRequest.java` eliminar unitPrice, el precio sale de Product (salePrice). Pequeño error de tipeo en "Quantity ir required"
- `OrderDetailResponse.java` eliminar Product, dejá solo ProductId y productName (despues si se quiere una vista detallada se consultan)
- `OrderRequest.java` añadir @Length(max = 255) a description

## Mappers
- `OrderDetailMapper.toEntity()` tiene que setear order, y el unitPrice sale de Product.getSalePrice()
- `OrderDetailMapper.toDetailResponse()` que mapee el nuevo detailResponse

## OrderCreatorService
- Descontar el stock del producto,chequear que detail.quantity no sea mayor a product.currentStock y despues hacer un product.setCurrentStock(product.currentStock - detail.quantity). Si detail.quantity > product.currentStock, lanza una excepción **InsufficientStockException** (la vas a tener que crear)
- `totalAccumulated.add(subTotal);` vas a tener que hacer **totalAccumulated = totalAccumulated.add(subTotal); porque los BigDecimal no son mutables.

### Bueno
Creo que con esas cosas ya quedaría por ahi esa parte.
Despues bueno faltaría el controller para obtener todas las ordenes que recuerdo que lo estabas haciendo el otro día.

:p

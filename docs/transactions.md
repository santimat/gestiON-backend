# Transacciones - Rediseño del Flujo Financiero

## Cambios Realizados

### Archivos Nuevos

| Archivo | Acción | Qué agrega |
|---------|--------|------------|
| `src/main/java/com/gestion/enums/TransactionType.java` | Nuevo enum | `VENTA` / `PAGO` |
| `src/main/java/com/gestion/exception/SaleAlreadyPayedException.java` | Nueva excepción | Error 409: venta ya pagada |
| `src/main/java/com/gestion/exception/InvalidPaymentAmountException.java` | Nueva excepción | Error 400: monto inválido |
| `src/main/java/com/gestion/dto/request/transaction/TransactionRequest.java` | Nuevo DTO | Request con `amount`, `type`, `saleId` |
| `src/main/java/com/gestion/dto/response/transaction/TransactionResponse.java` | Nuevo DTO | Response con `id`, `amount`, `type`, `saleId`, `createdAt` |
| `src/main/java/com/gestion/mappers/TransactionMapper.java` | Nuevo mapper | Conversión entidad ↔ DTO |
| `src/main/java/com/gestion/service/sale/SalePaymentService.java` | Nuevo servicio | Registrar pagos, actualizar status |
| `src/main/java/com/gestion/dto/request/sale/SalePaymentRequest.java` | Nuevo DTO | Request para endpoint de pagos |
| `src/main/java/com/gestion/controller/sale/SalePaymentPostController.java` | Nuevo controlador | `POST /api/sales/{saleId}/payments` |

### Archivos Modificados

| Archivo | Acción | Qué cambia |
|---------|--------|------------|
| `src/main/java/com/gestion/enums/SaleStatus.java` | Modificado | + `PARTIAL_PAYED` |
| `src/main/java/com/gestion/model/Transaction.java` | Reescrito | `amount` + `type` + `sale`; elimina `revenue`/`outflow` |
| `src/main/java/com/gestion/repository/JpaTransactionRepository.java` | Reescrito | Queries: `findBySaleIdAndType`, `sumPaymentsBySaleId` |
| `src/main/java/com/gestion/enums/ErrorCode.java` | Modificado | + `SALE_ALREADY_PAYED`, `INVALID_PAYMENT_AMOUNT` |
| `src/main/java/com/gestion/service/transaction/TransactionCreatorService.java` | Reescrito | Solo `createSaleTransaction(Sale)` → crea VENTA |
| `src/main/java/com/gestion/service/sale/SaleRevenueOutflowCalculatorService.java` | Reescrito | Devuelve `amount` + `shouldCreateTransaction` |
| `src/main/java/com/gestion/service/sale/SaleTransactionService.java` | Reescrito | Crea VENTA + PAGO inicial si CREDIT_ACCOUNT con partiallyPaidAmount |
| `src/main/java/com/gestion/service/sale/SaleCreatorService.java` | Modificado | Llama `transactionService.createIfNeeded(request, savedSale, commerceId)` |

---

## Flujo Completo

### 1. Crear Venta (Contado: CASH, CARD, TRANSFER)
```
POST /api/sales
→ SaleCreatorService
  → Crea Sale (status = PENDING/PAYED según resolver)
  → SaleTransactionService.createIfNeeded()
    → TransactionCreatorService.createSaleTransaction(sale)
      → INSERTA Transaction(type=VENTA, amount=total, sale_id)
```

### 2. Crear Venta (Cuenta Corriente - CREDIT_ACCOUNT sin pago inicial)
```
POST /api/sales { paymentMethod: CREDIT_ACCOUNT, partiallyPaidAmount: null }
→ SaleCreatorService
  → Crea Sale (status = PENDING)
  → SaleTransactionService.createIfNeeded()
    → Crea Transaction(type=VENTA, amount=total, sale_id)
    → No crea PAGO porque partiallyPaidAmount es null/0
```

### 3. Crear Venta (Cuenta Corriente - CREDIT_ACCOUNT con pago parcial)
```
POST /api/sales { paymentMethod: CREDIT_ACCOUNT, partiallyPaidAmount: 5000 }
→ SaleCreatorService
  → Crea Sale (status = PARTIAL_PAYED)
  → SaleTransactionService.createIfNeeded()
    → Crea Transaction(type=VENTA, amount=10000, sale_id)
    → Crea Transaction(type=PAGO, amount=5000, sale_id)
```

### 4. Registrar Pago a Cuenta Corriente
```
POST /api/sales/{saleId}/payments { amount: 3000 }
→ SalePaymentService.registerPayment(saleId, commerceId, amount)
  → Busca Sale con scope commerceId
  → Valida: no PAYED/GIFTED, amount > 0, amount <= pendiente
  → pendiente = total - SUM(PAGOS existentes)
  → INSERTA Transaction(type=PAGO, amount, sale_id)
  → Actualiza Sale.status:
      - si pendiente - amount == 0 → PAYED
      - si pendiente - amount > 0 → PARTIAL_PAYED
```

### 5. Consultar Pendiente de una Venta
```
pendiente = sale.getTotal() - jpaTransactionRepository.sumPaymentsBySaleId(saleId)
```

---

## Reglas de Negocio

| Escenario | Transaction VENTA | Transaction PAGO | Sale.status |
|-----------|-------------------|------------------|-------------|
| Contado (CASH/CARD/TRANSFER) | Sí (total) | No | PENDING → PAYED |
| CREDIT_ACCOUNT sin pago | Sí (total) | No | PENDING |
| CREDIT_ACCOUNT pago parcial | Sí (total) | Sí (paid) | PARTIAL_PAYED |
| CREDIT_ACCOUNT pago total | Sí (total) | Sí (total) | PAYED |
| GIFTED | No | No | GIFTED |

---

## Validaciones

- `partiallyPaidAmount` ≥ 0 (Bean Validation `@DecimalMin("0")`)
- `partiallyPaidAmount` ≤ total (SalePartialPaymentValidatorService)
- Pago: amount > 0 y ≤ pendiente (SalePaymentService)
- No se puede pagar venta PAYED/GIFTED (SaleAlreadyPayedException)

---

## Endpoints

| Método | Path | Descripción |
|--------|------|-------------|
| POST | `/api/sales` | Crear venta (genera Transaction VENTA) |
| POST | `/api/sales/{saleId}/payments` | Registrar pago (genera Transaction PAGO) |
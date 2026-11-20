package com.gestion.service.sale;

import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;
import com.gestion.model.Sale;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SaleRevenueOutflowCalculatorService {

    public record Result(BigDecimal amount, boolean shouldCreateTransaction) {}

    public Result calculate(Sale sale) {
        // Solo crear transacción VENTA para ventas no GIFTED
        if (sale.getStatus() == SaleStatus.GIFTED) {
            return new Result(BigDecimal.ZERO, false);
        }
        // Para todas las demás ventas (incluyendo CREDIT_ACCOUNT), crear VENTA con el total
        return new Result(sale.getTotal(), true);
    }
}
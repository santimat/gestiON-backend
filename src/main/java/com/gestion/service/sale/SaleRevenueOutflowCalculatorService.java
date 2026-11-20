package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;
import com.gestion.model.Sale;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SaleRevenueOutflowCalculatorService {

    public record Result(BigDecimal revenue, BigDecimal outflow) {}

    public Result calculate(SaleRequest request, Sale sale) {
        BigDecimal revenue;
        BigDecimal outflow;

        if (sale.getPaymentMethod() == PaymentMethod.CREDIT_ACCOUNT) {
            BigDecimal paid = request.partiallyPaidAmount() != null ? request.partiallyPaidAmount() : BigDecimal.ZERO;
            revenue = paid;
            outflow = sale.getTotal().subtract(paid);
        } else if (sale.getStatus() != SaleStatus.GIFTED) {
            revenue = sale.getTotal();
            outflow = BigDecimal.ZERO;
        } else {
            revenue = BigDecimal.ZERO;
            outflow = BigDecimal.ZERO;
        }

        return new Result(revenue, outflow);
    }
}
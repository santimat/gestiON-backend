package com.gestion.service.saledetail;

import com.gestion.model.SaleDetail;

import java.math.BigDecimal;
import java.util.List;

public record SaleDetailValidationResult(
        List<SaleDetail> details, BigDecimal subtotal) {
}
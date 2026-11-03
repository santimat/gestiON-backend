package com.gestion.service.saleDetails;

import com.gestion.model.SaleDetail;

import java.math.BigDecimal;
import java.util.List;

public record SaleDetailsValidationResult(
        List<SaleDetail> details, BigDecimal subtotal) {
}


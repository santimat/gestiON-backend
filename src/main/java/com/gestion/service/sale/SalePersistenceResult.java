package com.gestion.service.sale;

import com.gestion.model.Sale;

public record SalePersistenceResult(
        Sale savedSale,
        int itemCount
) {
}
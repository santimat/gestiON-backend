package com.gestion.dto.project.sale;

import com.gestion.model.Sale;

public record SalePersistenceResult(
        Sale savedSale,
        int itemCount
) {
}
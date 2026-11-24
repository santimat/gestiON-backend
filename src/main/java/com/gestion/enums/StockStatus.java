package com.gestion.enums;

public enum StockStatus {
    LOW_STOCK,      // currentStock <= minStock y > 0
    WITHOUT_STOCK   // currentStock = 0
}
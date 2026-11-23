package com.gestion.utils;

import java.util.Set;

public final class SortableProperties {

    public static final Set<String> CLIENT = Set.of(
            "id", "name", "lastName", "dni", "createdAt", "updatedAt");

    public static final Set<String> SALE = Set.of(
            "id", "total", "subtotal", "status", "paymentMethod", "createdAt", "updatedAt");

    public static final Set<String> PRODUCT = Set.of(
            "id", "name", "costPrice", "salePrice", "currentStock", "minStock", "active",
            "createdAt", "updatedAt");

    public static final Set<String> CATEGORY = Set.of(
            "id", "name", "active", "createdAt", "updatedAt");

    public static final Set<String> SELL_POINT = Set.of(
            "id", "name", "active", "createdAt", "updatedAt");

    public static final Set<String> OFFER = Set.of(
            "id", "value", "discountType", "offerType", "startDate", "endDate");

    public static final Set<String> COMMERCE = Set.of(
            "id", "name", "address", "cuit", "active", "createdAt", "updatedAt");

    public static final Set<String> USER = Set.of(
            "id", "name", "email", "role", "active", "createdAt", "updatedAt");

    private SortableProperties() {
    }
}

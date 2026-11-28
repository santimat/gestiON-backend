package com.gestion.utils;

public final class CuitNormalizer {

    public static final String PATTERN = "^\\d{11}$";
    public static final String MESSAGE = "CUIT must have 11 digits (hyphens are optional)";

    private CuitNormalizer() {
    }

    public static String normalize(String cuit) {
        if (cuit == null) {
            return null;
        }
        return cuit.replaceAll("[\\s-]", "");
    }
}

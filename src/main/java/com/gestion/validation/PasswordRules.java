package com.gestion.validation;

import java.nio.charset.StandardCharsets;

public final class PasswordRules {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_BCRYPT_BYTES = 72;

    private PasswordRules() {
    }

    // BCrypt solo considera los primeros 72 bytes, y Spring Security rechaza contraseñas mas largas
    public static boolean fitsBcrypt(String password) {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length <= MAX_BCRYPT_BYTES;
    }

    // codePointCount: un emoji cuenta como un caracter, no como dos chars UTF-16
    public static boolean meetsPolicy(String password) {
        return password != null
                && password.codePointCount(0, password.length()) >= MIN_LENGTH
                && fitsBcrypt(password);
    }
}

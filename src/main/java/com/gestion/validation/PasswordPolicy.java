package com.gestion.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Politica de contraseñas para altas y cambios (reglas en PasswordRules): minimo 8 caracteres y maximo 72 bytes en UTF-8,
 * que es lo que BCrypt considera (un @Size(max = 72) contaria caracteres, y una contraseña con
 * acentos o emojis podria pasar la validacion y romper en el encoder).
 */
@Documented
@Constraint(validatedBy = PasswordPolicyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordPolicy {

    String message() default "Password must have at least 8 characters and at most 72 bytes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package com.gestion.exception;

public class InactiveResourceException extends RuntimeException {
    public InactiveResourceException(String message) {
        super(message);
    }
}

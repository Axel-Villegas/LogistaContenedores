package com.logistica.exception;

public class LogisticaException extends RuntimeException {
    public LogisticaException(String message) {
        super(message);
    }

    public LogisticaException(String message, Throwable cause) {
        super(message, cause);
    }
}
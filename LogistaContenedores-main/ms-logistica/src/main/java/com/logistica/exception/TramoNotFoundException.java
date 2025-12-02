package com.logistica.exception;

public class TramoNotFoundException extends NotFoundException {
    public TramoNotFoundException(Long id) {
        super("Tramo no encontrado: " + id);
    }
}
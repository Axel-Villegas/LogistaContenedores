package com.logistica.exception;

public class RutaNotFoundException extends NotFoundException {
    public RutaNotFoundException(Long id) {
        super("Ruta no encontrada: " + id);
    }
}
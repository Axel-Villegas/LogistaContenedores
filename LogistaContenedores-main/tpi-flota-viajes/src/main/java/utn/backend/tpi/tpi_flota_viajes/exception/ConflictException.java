package utn.backend.tpi.tpi_flota_viajes.exception;

/**
 * Se lanza cuando hay un conflicto en la operación
 * Ejemplos:
 * - Intento de crear un recurso duplicado (dominio ya existe)
 * - Intento de cambiar estado a uno inválido
 * - Intento de asignar un tramo a un camión no disponible
 * HTTP Status: 409 Conflict
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String mensaje) {
        super(mensaje);
    }

    public ConflictException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

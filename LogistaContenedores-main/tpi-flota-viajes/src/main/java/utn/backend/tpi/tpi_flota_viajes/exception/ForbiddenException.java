package utn.backend.tpi.tpi_flota_viajes.exception;

/**
 * Se lanza cuando el usuario no tiene permiso para ejecutar la acción
 * HTTP Status: 403 Forbidden
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String mensaje) {
        super(mensaje);
    }

    public ForbiddenException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

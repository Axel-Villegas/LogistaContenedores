package utn.backend.tpi.tpi_flota_viajes.exception;

/**
 * Se lanza cuando un recurso no es encontrado
 * HTTP Status: 404 Not Found
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String mensaje) {
        super(mensaje);
    }

    public NotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

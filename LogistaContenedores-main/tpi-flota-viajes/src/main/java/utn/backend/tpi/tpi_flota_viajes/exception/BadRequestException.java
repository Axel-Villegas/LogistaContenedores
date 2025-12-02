package utn.backend.tpi.tpi_flota_viajes.exception;

/**
 * Se lanza cuando la solicitud tiene datos inválidos
 * Ejemplos:
 * - Capacidad negativa
 * - Coordenadas fuera de rango
 * - Kilómetros recorridos en 0
 * HTTP Status: 400 Bad Request
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String mensaje) {
        super(mensaje);
    }

    public BadRequestException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

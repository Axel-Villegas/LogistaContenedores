package utn.backend.tpi.tpi_flota_viajes.exception;

import org.springframework.dao.DataIntegrityViolationException;
import utn.backend.tpi.tpi_flota_viajes.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manejador global de excepciones
 * Se encarga de procesar todas las excepciones lanzadas en los Controllers
 * y devolver respuestas consistentes en formato JSON
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
        /**
         * Maneja NotFoundException (404)
         */
        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFoundException(
                        NotFoundException ex,
                        WebRequest request) {

                log.warn("NotFoundException: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje(ex.getMessage())
                                .codigo("NOT_FOUND")
                                .status(HttpStatus.NOT_FOUND.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        /**
         * Maneja ConflictException (409)
         */
        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponse> handleConflictException(
                        ConflictException ex,
                        WebRequest request) {

                log.warn("ConflictException: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje(ex.getMessage())
                                .codigo("CONFLICT")
                                .status(HttpStatus.CONFLICT.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        /**
         * Maneja BadRequestException (400)
         */
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequestException(
                        BadRequestException ex,
                        WebRequest request) {

                log.warn("BadRequestException: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje(ex.getMessage())
                                .codigo("BAD_REQUEST")
                                .status(HttpStatus.BAD_REQUEST.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Maneja ForbiddenException (403)
         */
        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ErrorResponse> handleForbiddenException(
                        ForbiddenException ex,
                        WebRequest request) {

                log.warn("ForbiddenException: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje(ex.getMessage())
                                .codigo("FORBIDDEN")
                                .status(HttpStatus.FORBIDDEN.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        /**
         * Maneja errores de validación (400)
         * Se ejecuta cuando @Valid falla en los DTOs
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {

                log.warn("Error de validación en request");

                List<String> detalles = new ArrayList<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> detalles.add(error.getField() + ": " + error.getDefaultMessage()));

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje("Error de validación en los datos enviados")
                                .codigo("VALIDATION_ERROR")
                                .status(HttpStatus.BAD_REQUEST.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .detalles(detalles)
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Maneja cualquier otra excepción no prevista (500)
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex,
                        WebRequest request) {

                log.error("Error no controlado: {}", ex.getMessage(), ex);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje("Error interno del servidor")
                                .codigo("INTERNAL_SERVER_ERROR")
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Maneja excepciones que se pueden escapar al crear dos pk iguales al mismo
        // tiempo
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex,
                        WebRequest request) {

                log.error("Violación de constraint de integridad: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje("Error: Los datos violan restricciones de integridad (probablemente duplicado)")
                                .codigo("DATA_INTEGRITY_VIOLATION")
                                .status(HttpStatus.CONFLICT.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler({ ResourceAccessException.class, RestClientException.class })
        public ResponseEntity<ErrorResponse> handleExternalServiceException(
                        Exception ex,
                        WebRequest request) {

                log.error("Error de comunicación con microservicio externo: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje("El servicio de logística no está disponible o respondió con error.")
                                .codigo("EXTERNAL_SERVICE_ERROR")
                                .status(HttpStatus.BAD_GATEWAY.value()) // 502 es mejor que 500 aquí
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
        }

        @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
                        org.springframework.http.converter.HttpMessageNotReadableException ex,
                        WebRequest request) {

                log.warn("Error de lectura del mensaje HTTP: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .mensaje("Cuerpo de la solicitud inválido o mal formado")
                                .codigo("INVALID_REQUEST_BODY")
                                .status(HttpStatus.BAD_REQUEST.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
}

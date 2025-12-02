package utn.backend.tpi.tpi_flota_viajes.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObtenerDisponiblesRequest {
    /**
     * Peso total que se necesita transportar (en kg)
     * El sistema buscará camiones con capacidad >= a este valor
     * Debe ser positivo (> 0)
     */
    @NotNull(message = "Capacidad de peso requerida es obligatoria")
    @Positive(message = "Capacidad debe ser mayor a 0")
    private Double capacidadPesoRequerida;

    /**
     * Volumen total que se necesita transportar (en m³)
     * El sistema buscará camiones con capacidad >= a este valor
     * Debe ser positivo (> 0)
     */
    @NotNull(message = "Capacidad de volumen requerida es obligatoria")
    @Positive(message = "Volumen debe ser mayor a 0")
    private Double capacidadVolumenRequerida;
}

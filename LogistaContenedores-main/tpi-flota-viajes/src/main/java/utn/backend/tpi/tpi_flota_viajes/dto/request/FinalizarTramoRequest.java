package utn.backend.tpi.tpi_flota_viajes.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizarTramoRequest {

    /**
     * Kilómetros recorridos durante el tramo
     * Se acumularán en el registro del camión
     */
    @NotNull(message = "Kilómetros recorridos es obligatorio")
    @Positive(message = "Kilómetros recorridos debe ser mayor a 0")
    private Double kmRecorridos;
}

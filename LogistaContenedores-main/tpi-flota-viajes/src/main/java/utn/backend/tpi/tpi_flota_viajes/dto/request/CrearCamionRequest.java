package utn.backend.tpi.tpi_flota_viajes.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearCamionRequest {
    @NotBlank(message = "Dominio es obligatorio")
    @Size(min = 6, max = 10, message = "Dominio debe tener entre 6 y 10 caracteres")
    private String dominio;  // Ej: "AY 123 BC"

    @NotNull(message = "Capacidad de peso es obligatoria")
    @Positive(message = "Capacidad de peso debe ser mayor a 0")
    private Double capacidadPeso;  // kg

    @NotNull(message = "Capacidad de volumen es obligatoria")
    @Positive(message = "Capacidad de volumen debe ser mayor a 0")
    private Double capacidadVolumen;  // m³

    @NotNull(message = "ID del transportista es obligatorio")
    private Long idTransportista;

    @NotNull @Positive
    private Double costoPorKm;

    @NotNull @Positive
    private Double consumoCombustiblePromedio;
}

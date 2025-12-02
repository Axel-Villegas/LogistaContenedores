package utn.backend.tpi.tpi_flota_viajes.dto.response;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CamionDisponibleItemResponse {
    private Long id;
    private String dominio;
    private Double capacidadPeso;
    private Double capacidadVolumen;
    private Double consumoCombustiblePromedio;
    private Double costoPorKm;
    private String transportistaNombre;
}

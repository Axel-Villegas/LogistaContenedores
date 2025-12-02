package utn.backend.tpi.tpi_flota_viajes.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CamionResponse {
    private Long id;
    private String dominio;
    private Double capacidadPeso;
    private Double capacidadVolumen;
    private Double consumoCombustiblePromedio;
    private Double costoPorKm;
    private String estado;  // DISPONIBLE, EN_USO, MANTENIMIENTO
    private Long transportistaId;
    private String transportistaNombre;
    private Long tramoEnCursoId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

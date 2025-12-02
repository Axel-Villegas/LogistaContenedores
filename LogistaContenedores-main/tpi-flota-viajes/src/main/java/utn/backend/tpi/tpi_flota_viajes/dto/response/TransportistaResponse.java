package utn.backend.tpi.tpi_flota_viajes.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportistaResponse {
    private Long id;
    private String nombre;
    private String licencia;
    private String contacto;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

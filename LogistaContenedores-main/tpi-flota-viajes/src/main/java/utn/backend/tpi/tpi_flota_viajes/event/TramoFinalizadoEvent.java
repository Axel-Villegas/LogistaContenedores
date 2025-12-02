package utn.backend.tpi.tpi_flota_viajes.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoFinalizadoEvent {
    private Long idTramo;
    private Long camionId;
    private String dominioCamion;
    private Double kmRecorridos;
    private Double latitud;
    private Double longitud;
    private Double costoPorKmCamion;  // Para cálculo de costo real
    private Double consumoCombustibleCamion;  // Para cálculo de costo real
    private LocalDateTime fechaHoraFin;
    private LocalDateTime timestamp;
}

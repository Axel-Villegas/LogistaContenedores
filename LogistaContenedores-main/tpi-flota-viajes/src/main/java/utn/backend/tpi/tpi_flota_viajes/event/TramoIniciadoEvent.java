package utn.backend.tpi.tpi_flota_viajes.event;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoIniciadoEvent {
    private Long idTramo;
    private Long camionId;
    private String dominioCamion;
    private Double latitud;
    private Double longitud;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime timestamp;
}

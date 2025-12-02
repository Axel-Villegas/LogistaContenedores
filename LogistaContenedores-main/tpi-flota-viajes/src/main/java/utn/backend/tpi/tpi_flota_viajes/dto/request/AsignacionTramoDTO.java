package utn.backend.tpi.tpi_flota_viajes.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AsignacionTramoDTO {
    private Long tramoId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String origen;
    private String destino;
    private Double kmEstimados;
}

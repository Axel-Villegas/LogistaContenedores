package utn.backend.tpi.tpi_flota_viajes.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReasignacionRequest {
    private Long tramoAnterior;
    private Long tramoNuevo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    public ReasignacionRequest() {}

    public ReasignacionRequest(Long tramoAnterior, Long tramoNuevo, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.tramoAnterior = tramoAnterior;
        this.tramoNuevo = tramoNuevo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }
}

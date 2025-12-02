package utn.backend.tpi.tpi_flota_viajes.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TramoPendienteResponse {
    private Long idTramo;
    private String dominioCamion;
    private String estadoTramo;
    private LocalDateTime fechaAsignacion;
    private String tipo;
    private Double kmEstimados;
    private boolean puedeIniciar; // Si no está vencido
}

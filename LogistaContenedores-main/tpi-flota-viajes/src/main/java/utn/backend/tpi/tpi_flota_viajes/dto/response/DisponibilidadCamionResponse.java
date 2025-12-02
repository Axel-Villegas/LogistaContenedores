package utn.backend.tpi.tpi_flota_viajes.dto.response;

import lombok.Builder;
import lombok.Data;
import utn.backend.tpi.tpi_flota_viajes.model.EstadoCamion;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DisponibilidadCamionResponse {
    private String dominio;
    private EstadoCamion estado;
    private LocalDateTime proximaDisponibilidad;
    private List<Long> tramosAsignados;
    private Long tramoEnEjecucion;
    private int cantidadTramosAsignados;
    private double capacidadPeso;
    private double capacidadVolumen;
    private String nombreTransportista;
}

package utn.backend.tpi.tpi_flota_viajes.clients.dto;

import lombok.Data;
import java.time.LocalDateTime;

// DTO espejo de la respuesta de ms-logistica
@Data
public class TramoDTO {
    private Long id;
    private String estado;
    private String dominioCamionRef;
    private String tipo;
    private Double kmEstimados;
    private Double kmRecorridos;
    private Double costoEstimado;
    private Double costoReal;
    private Double tiempoEstimado;
    private Double tiempoReal;
    // Fechas planificadas
    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;

    // Fechas reales de ejecución
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
}

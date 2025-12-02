package com.logistica.dto.response;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
public class TramoResponse {
    private Long id;
    private String estado;
    private String tipo;
    private String origen;
    private String destino;

    // Referencia al camión (Importante: mismo nombre que espera ms-flota)
    private String dominioCamionRef;

    // Métricas Operativas
    private Double kmEstimados;
    private Double kmRecorridos;
    private Double tiempoEstimado;
    private Double tiempoReal;

    // Métricas Económicas
    private Double costoEstimado;
    private Double costoReal;

    // Fechas Planificadas
    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;

    // Fechas Reales (Ejecución)
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
}
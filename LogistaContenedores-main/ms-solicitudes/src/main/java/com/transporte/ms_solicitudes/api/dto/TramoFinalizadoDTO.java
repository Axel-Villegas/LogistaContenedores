package com.transporte.ms_solicitudes.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoFinalizadoDTO {

    private Long idTramo;
    private double kmRecorridos;
    private double costoReal;
    private double tiempoReal;
    private LocalDateTime fechaHora;
    private String estado;
    private String nroSolicitudRef;

    private String ubicacionFin; // Nombre del depósito o "Destino Final"
    private boolean esDestinoFinal;
}
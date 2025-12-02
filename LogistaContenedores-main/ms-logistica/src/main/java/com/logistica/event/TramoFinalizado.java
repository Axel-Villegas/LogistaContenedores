package com.logistica.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TramoFinalizado {
    private Long idTramo;
    private String nroSolicitudRef;
    private double kmRecorridos;
    private double costoReal;
    private double tiempoReal;
    private LocalDateTime fechaHora;
    private String estado; // "FINALIZADO"
}
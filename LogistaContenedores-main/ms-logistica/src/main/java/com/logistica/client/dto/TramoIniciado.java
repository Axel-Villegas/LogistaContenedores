package com.logistica.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TramoIniciado {
    private Long idTramo;
    private String nroSolicitudRef;
    private LocalDateTime fechaHora;
    private String estado; // "INICIADO"
}
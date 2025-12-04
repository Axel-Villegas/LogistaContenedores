package com.transporte.ms_solicitudes.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoIniciadoDTO {

    private Long idTramo;
    private String estado;
    private LocalDateTime fechaHora;
    private String nroSolicitudRef;
}
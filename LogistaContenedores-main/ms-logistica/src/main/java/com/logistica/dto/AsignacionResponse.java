package com.logistica.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignacionResponse {
    private Long tramoId;
    private String camionDominio;
    private String estado;
    private String mensaje;

    public AsignacionResponse(Long tramoId, String camionDominio, String estado) {
        this.tramoId = tramoId;
        this.camionDominio = camionDominio;
        this.estado = estado;
        this.mensaje = String.format("Camión %s asignado exitosamente al tramo %d", camionDominio, tramoId);
    }
}

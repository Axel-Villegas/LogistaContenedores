package com.transporte.ms_solicitudes.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponseDTO {
    private String nroSolicitud;
    private String estado;
    private String idCliente;
    private ContenedorResumenDTO contenedor;
    private String fechaCreacion; // Útil para el cliente

    // Los campos calculados (costo, tiempo) van en null ahora
    // porque se calculan en la etapa de planificación (Operador)

    @Data
    @AllArgsConstructor
    public static class ContenedorResumenDTO {
        private String id;
        private Double peso;
        private Double volumen;
    }
}
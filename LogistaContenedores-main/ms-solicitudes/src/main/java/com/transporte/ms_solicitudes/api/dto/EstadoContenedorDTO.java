package com.transporte.ms_solicitudes.api.dto;

import com.transporte.ms_solicitudes.model.EstadoContenedor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoContenedorDTO {
    private String idContenedor;
    private EstadoContenedor estado;
    private String ubicacionActual; // "Puerto Bs As", "Depósito Central", "En viaje", etc.
}
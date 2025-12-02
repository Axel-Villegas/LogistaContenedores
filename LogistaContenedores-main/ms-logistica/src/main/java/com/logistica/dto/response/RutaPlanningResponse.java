package com.logistica.dto.response;

import com.logistica.model.Ruta;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RutaPlanningResponse {
    private Ruta ruta;
    private double costoEstimadoTotal;
    private double tiempoEstimadoTotal; // en segundos
}
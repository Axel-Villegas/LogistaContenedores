package com.logistica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RutaPlanningResponse {
    private RutaResponse ruta;
    private double costoEstimadoTotal;
    private double tiempoEstimadoTotal; // en segundos
}
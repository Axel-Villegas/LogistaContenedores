package com.logistica.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RutaTentativaResponse {
    private String descripcion; // Ej: "Ruta Más Rápida", "Ruta Económica"
    private double distanciaTotalKm;
    private double tiempoEstimadoTotalSegundos;
    private double costoEstimadoTotal;
    private List<TramoResponse> tramosSugeridos; // Reutilizamos tu TramoResponse
}
package com.logistica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalculoResponse {
    private double distanciaKM;
    private double tiempoEstimado; // segundos
    private double costoEstimado;
}
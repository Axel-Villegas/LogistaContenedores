package com.logistica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DistanciaResponse {
    private double distanciaKm;
    private double tiempoSegundos;
}
package com.logistica.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RutaResponse {
    private Long id;
    private String nroSolicitudRef;
    private int cantidadTramos;
    private double distanciaTotal;

    private List<TramoResponse> tramos;
}
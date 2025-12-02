package com.logistica.client.dto;

import lombok.Data;

@Data
public class CamionInfo {
    private Long id;
    private String dominio;
    private Double capacidadPeso;
    private Double capacidadVolumen;
    private Double consumoCombustiblePromedio;
    private Double costoPorKm;
}
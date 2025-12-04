package com.logistica.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepositoResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private Double costoEstadiaDiario;
}
package com.logistica.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
public class AsignarTramosConsecutivosRequest {

    @NotBlank(message = "El dominio del camión es obligatorio")
    private String camionDominio;

    @NotEmpty(message = "Debe especificar al menos un tramo")
    private List<Long> tramoIds;

    @NotEmpty(message = "Debe especificar fechas de inicio")
    private List<LocalDateTime> fechasInicio;

    @NotEmpty(message = "Debe especificar fechas de fin")
    private List<LocalDateTime> fechasFin;
}

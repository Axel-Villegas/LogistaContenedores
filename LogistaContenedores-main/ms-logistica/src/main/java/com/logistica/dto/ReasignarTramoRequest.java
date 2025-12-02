package com.logistica.dto;

import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ReasignarTramoRequest {

    @NotBlank(message = "El nuevo dominio del camión es obligatorio")
    private String nuevoCamionDominio;

    @NotNull(message = "La nueva fecha de inicio es obligatoria")
    private LocalDateTime nuevaFechaInicio;

    @NotNull(message = "La nueva fecha de fin es obligatoria")
    private LocalDateTime nuevaFechaFin;
}

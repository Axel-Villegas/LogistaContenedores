package com.logistica.dto;

import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class AsignarCamionRequest {

    @NotBlank(message = "El dominio del camión es obligatorio")
    private String camionDominio;

    @NotNull(message = "La fecha de inicio estimada es obligatoria")
    private LocalDateTime fechaHoraInicioEstimada;

    @NotNull(message = "La fecha de fin estimada es obligatoria")
    private LocalDateTime fechaHoraFinEstimada;
}

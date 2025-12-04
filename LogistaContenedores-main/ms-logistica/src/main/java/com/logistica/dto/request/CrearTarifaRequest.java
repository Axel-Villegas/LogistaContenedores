package com.logistica.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CrearTarifaRequest {

    @NotNull(message = "El valor por KM base es obligatorio")
    @Positive(message = "El valor por KM debe ser positivo")
    private Double valorKMBase;

    @NotNull(message = "El costo del litro de combustible es obligatorio")
    @Positive(message = "El costo del combustible debe ser positivo")
    private Double costoLitroCombustible;

    @NotNull(message = "El porcentaje de recargo es obligatorio")
    @PositiveOrZero(message = "El porcentaje no puede ser negativo")
    private Double porcentajeRecargo; // El nuevo campo dinámico

    @NotNull(message = "La fecha de vigencia es obligatoria")
    private LocalDate fechaVigencia;
}
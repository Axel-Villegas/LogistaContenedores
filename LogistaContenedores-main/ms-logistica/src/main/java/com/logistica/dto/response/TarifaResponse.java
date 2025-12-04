package com.logistica.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class TarifaResponse {
    private Long id;
    private Double valorKMBase;
    private Double costoLitroCombustible;
    private Double porcentajeRecargo;
    private LocalDate fechaVigencia;
}
package com.logistica.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double valorKMBase;
    private double costoLitroCombustible;
    private LocalDate fechaVigencia;
    /**
     * Porcentaje adicional para cubrir mantenimiento, seguros y chofer.
     * Ejemplo: 30.0 para un 30%
     */
    private Double porcentajeRecargo;
}
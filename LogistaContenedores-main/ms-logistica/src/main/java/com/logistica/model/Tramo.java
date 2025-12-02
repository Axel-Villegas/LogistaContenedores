package com.logistica.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tramo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ruta_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Ruta ruta;

    @ManyToOne
    @JoinColumn(name = "deposito_origen_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Deposito depositoOrigen;

    @ManyToOne
    @JoinColumn(name = "deposito_destino_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Deposito depositoDestino;

    @ManyToOne
    @JoinColumn(name = "tarifa_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Tarifa tarifa;

    private String tipo; // Origen-Deposito, Deposito-Deposito, Deposito-Destino, Origen-Destino

    @Enumerated(EnumType.STRING)
    private EstadoTramo estado; // ESTIMADO, ASIGNADO, INICIADO, FINALIZADO

    private double kmEstimados;
    private double kmRecorridos;

    private double costoEstimado;
    private double costoReal;

    private double tiempoEstimado; // en segundos
    private double tiempoReal; // en segundos

    // Fechas planificadas por el operador
    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;

    // Fechas reales de ejecución
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;

    // Referencia a MS Flota
    private String dominioCamionRef;
}
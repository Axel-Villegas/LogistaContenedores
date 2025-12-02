package com.logistica.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nroSolicitudRef; // Referencia a MS Solicitudes
    private int cantidadTramos;
    private double distanciaTotal; // en kilómetros

    @OneToMany(mappedBy = "ruta", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Tramo> tramos;
}
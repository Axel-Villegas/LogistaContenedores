package com.logistica.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Deposito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;
    private double costoEstadiaDiario;
}
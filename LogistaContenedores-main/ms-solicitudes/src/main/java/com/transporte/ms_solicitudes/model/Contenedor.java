package com.transporte.ms_solicitudes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contenedores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contenedor {
    @Id
    private String idContenedor; // Identificación única
    private Double peso;
    private Double volumen;
    private boolean refrigerado;
}

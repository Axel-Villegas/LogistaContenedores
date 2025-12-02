package com.transporte.ms_solicitudes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    private String idCliente; // Puede ser el mismo ID que en Keycloak o un CUIT/DNI
    private String nombre;
    private String email;
}

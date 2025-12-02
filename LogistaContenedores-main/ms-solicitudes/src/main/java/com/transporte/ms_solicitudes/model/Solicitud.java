package com.transporte.ms_solicitudes.model;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "solicitudes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {
    @Id
    private String nroSolicitud;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;
    private Double costoEstimado;
    private Integer tiempoEstimado;
    private Double costoFinal;
    private Integer tiempoReal;
    private Double origenLatitud;
    private Double origenLongitud;
    private Double destinoLatitud;
    private Double destinoLongitud;
    private String idCliente;
    private String idContenedor;
    private String idRutaRef;
}

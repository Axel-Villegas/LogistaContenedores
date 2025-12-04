package com.transporte.ms_solicitudes.model;

public enum EstadoContenedor {
    EN_ORIGEN,      // Esperando ser retirado del cliente
    EN_TRANSITO,    // Arriba de un camión
    EN_DEPOSITO,    // Esperando en un depósito intermedio
    ENTREGADO       // Llegó al destino final
}

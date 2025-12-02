package com.logistica.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class PlanificarRutaRequest {
    private String nroSolicitud;
    private List<Long> idDepositos; // IDs de depósitos intermedios (puede ser null o vacío)
    private Double latOrigen;
    private Double lonOrigen;
    private Double latDestino;
    private Double lonDestino;
    private Long idTarifa;
}
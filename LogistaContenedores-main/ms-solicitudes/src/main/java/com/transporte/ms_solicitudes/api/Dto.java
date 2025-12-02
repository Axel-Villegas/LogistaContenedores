package com.transporte.ms_solicitudes.api;

record SolicitudRequestDTO(
    String idCliente,
    String idContenedor,
    Localizacion origen,
    Localizacion destino
) {
    record Localizacion(String dir, Double lat, Double lon) {}
}


record SolicitudResponseDTO(
    String nroSolicitud,
    String estado
) {}

record TrackingDTO(
    String estado,
    String ubicacionActual,
    String proximoTramo,
    String ETA
) {}



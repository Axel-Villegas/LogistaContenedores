package com.transporte.ms_solicitudes.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRequestDTO {

    @Valid
    @NotNull(message = "La información del contenedor es obligatoria")
    private ContenedorDTO contenedor;

    @Valid
    @NotNull(message = "El origen es obligatorio")
    private UbicacionDTO origen;

    @Valid
    @NotNull(message = "El destino es obligatorio")
    private UbicacionDTO destino;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContenedorDTO {
        @NotNull
        private String idContenedor; // Identificador físico del contenedor
        @NotNull
        private Double peso;
        @NotNull
        private Double volumen;
        private boolean refrigerado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UbicacionDTO {
        private String direccion; // Dirección textual
        @NotNull
        private Double latitud;
        @NotNull
        private Double longitud;
    }
}
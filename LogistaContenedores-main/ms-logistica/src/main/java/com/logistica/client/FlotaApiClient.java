package com.logistica.client;

import com.logistica.client.dto.CamionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class FlotaApiClient {
    private final RestClient restClient;

    public FlotaApiClient(@Value("${app.flota.base-url:http://ms-flota:8083}") String flotaBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(flotaBaseUrl)
                // --- CAMBIO CLAVE: Agregamos este interceptor ---
                .requestInterceptor((request, body, execution) -> {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.getCredentials() instanceof Jwt) {
                        Jwt jwt = (Jwt) authentication.getCredentials();
                        request.getHeaders().setBearerAuth(jwt.getTokenValue());
                    }
                    return execution.execute(request, body);
                })
                // -----------------------------------------------
                .build();
    }

    /**
     * Obtiene información de un camión por su dominio
     */
    public CamionInfo obtenerCamionPorDominio(String dominio) {
        try {
            log.info("Obteniendo información del camión con dominio: {}", dominio);
            
            CamionInfo camion = restClient.get()
                    .uri("/api/flota/camiones/dominio/{dominio}", dominio)
                    .retrieve()
                    .body(CamionInfo.class);
            
            if (camion == null) {
                throw new RuntimeException("Camión no encontrado: " + dominio);
            }
            
            log.info("Camión obtenido: dominio={}, costoPorKm={}, consumo={}", 
                    camion.getDominio(), camion.getCostoPorKm(), camion.getConsumoCombustiblePromedio());
            
            return camion;
        } catch (Exception e) {
            log.error("Error al obtener camión {}: {}", dominio, e.getMessage(), e);
            throw new RuntimeException("Error al obtener información del camión: " + e.getMessage(), e);
        }
    }

    /**
     * Consultar disponibilidad de camiones por fecha y capacidad
     */
    public java.util.List<java.util.Map<String, Object>> consultarDisponibilidad(
            java.time.LocalDateTime fecha, Double pesoMinimo, Double volumenMinimo) {

        try {
            return restClient.get()
                .uri("/api/flota/camiones/disponibles?fecha={fecha}&pesoMinimo={peso}&volumenMinimo={volumen}",
                     fecha, pesoMinimo, volumenMinimo)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<java.util.List<java.util.Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("Error consultando disponibilidad: {}", e.getMessage());
            throw new RuntimeException("Error al consultar disponibilidad de camiones", e);
        }
    }

    /**
     * Notificar asignación múltiple de tramos a ms-flota
     */
    public void notificarAsignacionMultiple(String dominio, java.util.List<Long> tramoIds) {
        try {
            java.util.Map<String, Object> request = java.util.Map.of(
                "tramoIds", tramoIds
            );

            restClient.post()
                .uri("/api/flota/camiones/{dominio}/asignar-multiples-simple", dominio)
                .body(request)
                .retrieve()
                .toBodilessEntity();

            log.info("Notificación de asignación múltiple enviada para camión {}", dominio);

        } catch (Exception e) {
            log.error("Error en notificación de asignación múltiple: {}", e.getMessage());
            throw new RuntimeException("Error al notificar asignación múltiple", e);
        }
    }

    /**
     * Notificar reasignación de tramo
     */
    public void notificarReasignacion(String dominioAnterior, String dominioNuevo, Long tramoId) {
        try {
            java.util.Map<String, Object> request = java.util.Map.of(
                "tramoAnterior", tramoId,
                "tramoNuevo", tramoId  // Mismo tramo, diferente camión
            );

            if (dominioAnterior != null && !dominioAnterior.equals(dominioNuevo)) {
                // Liberar del camión anterior
                restClient.post()
                    .uri("/api/flota/camiones/{dominio}/reasignar", dominioAnterior)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            }

        } catch (Exception e) {
            log.error("Error en reasignación: {}", e.getMessage());
            throw new RuntimeException("Error al reasignar camión", e);
        }
    }
}

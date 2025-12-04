package com.logistica.client;

import com.logistica.client.dto.TramoFinalizado;
import com.logistica.client.dto.TramoIniciado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;
import java.time.LocalDateTime;

@Component
@Slf4j
public class SolicitudesApiClient {
    private final RestClient restClient;

    public SolicitudesApiClient(@Value("${app.solicitudes.base-url}") String solicitudesBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(solicitudesBaseUrl)
                // Interceptor para propagar el Token JWT
                .requestInterceptor((request, body, execution) -> {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.getCredentials() instanceof Jwt) {
                        Jwt jwt = (Jwt) authentication.getCredentials();
                        request.getHeaders().setBearerAuth(jwt.getTokenValue());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    public void notificarTramoIniciado(String nroSolicitud, Long idTramo) {
        try {
            // 1. Crear el OBJETO (no un Map)
            TramoIniciado evento = new TramoIniciado(
                    idTramo,
                    nroSolicitud,
                    LocalDateTime.now(),
                    "EN_TRANSITO" // O el estado que corresponda
            );

            // 2. Enviarlo
            restClient.put()
                    .uri("/api/v1/solicitudes/{nro}/estado", nroSolicitud)
                    .body(evento) // <--- Pasamos el objeto, Jackson lo convierte a JSON
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notificado inicio tramo {} para solicitud {}", idTramo, nroSolicitud);
        } catch (Exception e) {
            log.error("Error notificando a ms-solicitudes: {}", e.getMessage());
        }
    }

    public void notificarTramoFinalizado(String nroSolicitud, Long idTramo, double costo,double km, double tiempo, String ubicacion, boolean finalizado) {
        try {
            // Si es destino final, sugerimos ENTREGADA, si no, sigue EN_TRANSITO
            // Aunque ms-solicitudes tiene la última palabra, enviamos el dato coherente.
            String estadoSugerido = finalizado ? "ENTREGADA" : "EN_TRANSITO";

            TramoFinalizado evento = new TramoFinalizado(
                    idTramo,
                    nroSolicitud,
                    km,     // <--- AHORA USA EL VALOR REAL, NO 0.0
                    costo,
                    tiempo,
                    LocalDateTime.now(),
                    estadoSugerido,
                    ubicacion,
                    finalizado
            );

            // 2. Enviarlo
            restClient.put()
                    .uri("/api/v1/solicitudes/{nro}/actualizar-metricas", nroSolicitud)
                    .body(evento) // <--- Pasamos el objeto
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notificado fin tramo {} para solicitud {}", idTramo, nroSolicitud);
        } catch (Exception e) {
            log.error("Error notificando a ms-solicitudes: {}", e.getMessage());
        }
    }

    public void notificarRutaCompletada(String nroSolicitud) {
        try {
            Map<String, String> body = Map.of("estado", "ENTREGADA");

            restClient.put()
                    .uri("/api/v1/solicitudes/{nro}/estado", nroSolicitud)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notificada ruta completada para: {}", nroSolicitud);
        } catch (Exception e) {
            log.error("Error notificando completitud a ms-solicitudes: {}", e.getMessage());
        }
    }
}
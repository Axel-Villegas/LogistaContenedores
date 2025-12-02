package utn.backend.tpi.tpi_flota_viajes.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import jakarta.ws.rs.BadRequestException;
import utn.backend.tpi.tpi_flota_viajes.clients.dto.TramoDTO;

@Component
public class LogisticaApiClient {
    private final RestClient restClient;

    public LogisticaApiClient(@Value("${api.logistica.url}") String logisticaBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(logisticaBaseUrl)
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

    public TramoDTO iniciarTramo(Long idTramo) {
        return restClient.put()
                .uri("/api/v1/tramos/{id}/iniciar", idTramo)
                .retrieve()
                // Agregar manejo de errores específico si es necesario
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new BadRequestException("Error en solicitud a Logística: " + response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Error en servidor de Logística: " + response.getStatusText());
                })
                .body(TramoDTO.class);
    }

    /**
     * Notifica a ms-logistica que un tramo está finalizando
     * Envía los kilómetros recorridos para registro
     */
    public TramoDTO finalizarTramo(Long idTramo, double kmRecorridos) {
        return restClient.put()
                .uri("/api/v1/tramos/{id}/finalizar?kmRecorridos={kmRecorridos}", idTramo, kmRecorridos)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new BadRequestException("Error en solicitud a Logística: " + response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Error en servidor de Logística: " + response.getStatusText());
                })
                .body(TramoDTO.class);
    }

    public void asignarCamion(Long idTramo, String dominio) {
        restClient.post()
                .uri("/api/v1/tramos/{id}/asignar?dominio={dominio}", idTramo, dominio)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new BadRequestException("Error en solicitud a Logística: " + response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Error en servidor de Logística: " + response.getStatusText());
                })
                .toBodilessEntity();
    }

    /**
     * Obtiene los detalles de un tramo específico
     * Necesario para conocer qué camión está asignado
     */
    public TramoDTO obtenerTramo(Long idTramo) {
        return restClient.get()
                .uri("/api/v1/tramos/{id}/detalle", idTramo)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new BadRequestException("Tramo no encontrado: " + response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Error en servidor de Logística: " + response.getStatusText());
                })
                .body(TramoDTO.class);
    }

    /**
     * Libera un camión por timeout desde ms-logistica
     */
    public void liberarCamionPorTimeout(String dominio, Long tramoId) {
        restClient.post()
                .uri("/api/flota/camiones/{dominio}/timeout?tramoId={tramoId}", dominio, tramoId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new BadRequestException("Error liberando camión por timeout: " + response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("Error en servidor de Flota: " + response.getStatusText());
                })
                .toBodilessEntity();
    }
}
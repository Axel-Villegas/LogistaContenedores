package com.logistica.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.logistica.client.OsrmDistanceResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OsrmClient2 {
    private final RestClient restClient;

    @SuppressWarnings("null")
    public OsrmClient2(@org.springframework.beans.factory.annotation.Value("${app.osrm.base-url}") String osrmUrl) {
        // OSRM local o remoto
        this.restClient = RestClient.builder().baseUrl(osrmUrl).build();
    }

    /**
     * Calcula distancia y tiempo entre dos puntos
     */
    public OsrmDistanceResponse calcularDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        try {
            log.info("Calculando distancia: ({}, {}) -> ({}, {})", lat1, lon1, lat2, lon2);

            String uri = String.format("/route/v1/driving/%.6f,%.6f;%.6f,%.6f",
                    lon1, lat1, lon2, lat2);

            OsrmDistanceResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(OsrmDistanceResponse.class);

            log.info("Distancia calculada: {} km, {} segundos",
                    response.getDistanceKm(), response.getDurationSeconds());

            return response;
        } catch (Exception e) {
            log.error("Error al llamar OSRM", e);
            throw new RuntimeException("Error calculando distancia con OSRM: " + e.getMessage(), e);
        }
    }
}

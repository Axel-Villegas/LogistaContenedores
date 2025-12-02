package com.logistica.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OsrmClient {
    private final RestClient restClient;

    public OsrmClient(@Value("${app.osrm.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Calcula la distancia y duración entre dos puntos.
     * Formato: "longitud,latitud"
     */

    public OsrmResponse.Route getRoute(String fromLongLat, String toLongLat) {
        // "http://osrm:5000/route/v1/driving/-64.18,-31.41;-60.69,-32.94?overview=false"
        String uri = String.format("/route/v1/driving/%s;%s?overview=false", fromLongLat, toLongLat);

        OsrmResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(OsrmResponse.class);

        if (response != null && response.getRoutes() != null && !response.getRoutes().isEmpty()) {
            return response.getRoutes().get(0);
        }
        throw new RuntimeException("No se pudo calcular la ruta OSRM");
    }
}

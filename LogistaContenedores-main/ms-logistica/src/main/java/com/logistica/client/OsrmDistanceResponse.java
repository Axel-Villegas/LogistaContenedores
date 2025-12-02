package com.logistica.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmDistanceResponse {
    @JsonProperty("routes")
    private List<Route> routes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        @JsonProperty("distance")
        private double distance; // en metros

        @JsonProperty("duration")
        private double duration; // en segundos
    }

    public double getDistanceKm() {
        if (routes == null || routes.isEmpty()) {
            return 0.0;
        }
        return routes.get(0).distance / 1000.0;
    }

    public double getDurationSeconds() {
        if (routes == null || routes.isEmpty()) {
            return 0.0;
        }
        return routes.get(0).duration;
    }

}
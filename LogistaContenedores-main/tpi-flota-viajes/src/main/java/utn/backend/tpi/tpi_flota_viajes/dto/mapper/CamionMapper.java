package utn.backend.tpi.tpi_flota_viajes.dto.mapper;

import utn.backend.tpi.tpi_flota_viajes.dto.response.CamionDisponibleItemResponse;
import utn.backend.tpi.tpi_flota_viajes.model.Camion;
import utn.backend.tpi.tpi_flota_viajes.dto.response.CamionResponse;

public class CamionMapper {
    public static CamionResponse toResponse(Camion entity) {
        return CamionResponse.builder()
                .id(entity.getId())
                .dominio(entity.getDominio())
                .capacidadPeso(entity.getCapacidadPeso())
                .capacidadVolumen(entity.getCapacidadVolumen())
                .consumoCombustiblePromedio(entity.getConsumoCombustiblePromedio())
                .costoPorKm(entity.getCostoPorKm())
                .estado(entity.getEstado().toString())
                .transportistaId(getTransportistaId(entity))  // ← Helper method
                .transportistaNombre(getTransportistaNombre(entity))  // ← Helper method
                .tramoEnCursoId(entity.getTramoEnEjecucion())
                .build();
    }

    public static CamionDisponibleItemResponse toDisponibleItemResponse(Camion entity) {
        return CamionDisponibleItemResponse.builder()
                .id(entity.getId())
                .dominio(entity.getDominio())
                .capacidadPeso(entity.getCapacidadPeso())
                .capacidadVolumen(entity.getCapacidadVolumen())
                .consumoCombustiblePromedio(entity.getConsumoCombustiblePromedio())
                .costoPorKm(entity.getCostoPorKm())
                .transportistaNombre(getTransportistaNombre(entity))  // ← Helper method
                .build();
    }

    // Helper methods
    private static Long getTransportistaId(Camion camion) {
        return camion.getTransportista() != null ? camion.getTransportista().getId() : null;
    }

    private static String getTransportistaNombre(Camion camion) {
        return camion.getTransportista() != null ? camion.getTransportista().getNombre() : null;
    }

}

package utn.backend.tpi.tpi_flota_viajes.dto.mapper;

import org.springframework.stereotype.Component;
import utn.backend.tpi.tpi_flota_viajes.clients.dto.TramoDTO;
import utn.backend.tpi.tpi_flota_viajes.dto.response.TramoResponse;

@Component
public class TramoMapper {

    /**
     * Convierte el DTO interno de ms-logistica en el DTO público para el transportista.
     */
    public TramoResponse toResponse(TramoDTO tramoDTO, String dominioCamion) {
        if (tramoDTO == null) {
            return null;
        }

        return TramoResponse.builder()
                .idTramo(tramoDTO.getId())
                .estado(tramoDTO.getEstado())
                .fechaHoraInicio(tramoDTO.getFechaHoraInicioReal())  // Usar fechas reales
                .fechaHoraFin(tramoDTO.getFechaHoraFinReal())        // Usar fechas reales
                .kmRecorridos(tramoDTO.getKmRecorridos())
                .dominioCamion(dominioCamion)
                .build();
    }
}
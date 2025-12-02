package utn.backend.tpi.tpi_flota_viajes.dto.mapper;

import utn.backend.tpi.tpi_flota_viajes.dto.response.TransportistaResponse;
import utn.backend.tpi.tpi_flota_viajes.model.Transportista;

//Para mapear entidades a DTOs
public class TransportistaMapper {
    public static TransportistaResponse toResponse(Transportista entity) {
        return TransportistaResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .licencia(entity.getLicencia())
                .contacto(entity.getContacto())
                .activo(entity.getActivo())
                .build();
    }
}

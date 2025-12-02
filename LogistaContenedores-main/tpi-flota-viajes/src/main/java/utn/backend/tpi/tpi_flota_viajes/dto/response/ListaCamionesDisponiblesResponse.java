package utn.backend.tpi.tpi_flota_viajes.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListaCamionesDisponiblesResponse {
    private List<CamionDisponibleItemResponse> camiones;
    private Long total;
}

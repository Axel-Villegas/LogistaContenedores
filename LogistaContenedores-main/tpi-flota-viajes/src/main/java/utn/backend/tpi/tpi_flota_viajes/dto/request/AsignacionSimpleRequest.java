package utn.backend.tpi.tpi_flota_viajes.dto.request;

import lombok.Data;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionSimpleRequest {
    private List<Long> tramoIds;
}
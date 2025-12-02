package utn.backend.tpi.tpi_flota_viajes.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String mensaje;
    private String codigo;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private List<String> detalles;  // Para errores de validación
}

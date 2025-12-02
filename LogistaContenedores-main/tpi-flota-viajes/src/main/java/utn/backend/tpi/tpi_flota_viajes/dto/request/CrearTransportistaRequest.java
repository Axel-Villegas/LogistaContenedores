package utn.backend.tpi.tpi_flota_viajes.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import utn.backend.tpi.tpi_flota_viajes.validation.ValidContacto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearTransportistaRequest {
    @NotBlank(message = "Nombre es obligatorio")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "Licencia es obligatoria")
    @Size(min = 8, max = 20, message = "Licencia debe tener entre 8 y 20 caracteres")
    private String licencia;

    @NotBlank(message = "Contacto es obligatorio")
    @ValidContacto  // ← Usar la anotación custom
    private String contacto;
}

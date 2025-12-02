package utn.backend.tpi.tpi_flota_viajes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transportista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;
    private String licencia;
    
    @Column(unique = true, nullable = false)
    private String contacto; // Email o teléfono

    private Boolean activo; // Campo requerido por tu CamionService

    @OneToMany(mappedBy = "transportista", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Camion> camiones;
}
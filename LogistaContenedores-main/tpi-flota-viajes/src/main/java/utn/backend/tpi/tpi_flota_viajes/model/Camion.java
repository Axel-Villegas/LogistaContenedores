package utn.backend.tpi.tpi_flota_viajes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String dominio;

    private double capacidadPeso;
    private double capacidadVolumen;
    private double consumoCombustiblePromedio;
    private double costoPorKm;
    private double kmRecorridos;

    @Enumerated(EnumType.STRING)
    private EstadoCamion estado;

    // Gestión de múltiples tramos
    private Long tramoEnEjecucion;        // Tramo que está ejecutando AHORA (si EN_VIAJE)

    @ElementCollection
    @OrderColumn(name = "orden_tramo")
    @CollectionTable(name = "camion_tramos_programados",
                    joinColumns = @JoinColumn(name = "camion_id"))
    @Column(name = "tramo_id")
    private List<Long> tramosProgramados = new java.util.ArrayList<>();

    private LocalDateTime proximaDisponibilidad; // Cuándo termina el último tramo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id")
    private Transportista transportista;

    // Métodos helper para gestión de disponibilidad
    public boolean estaDisponibleEn(java.time.LocalDateTime fecha) {
        if (estado == EstadoCamion.MANTENIMIENTO) {
            return false;
        }
        return proximaDisponibilidad == null || proximaDisponibilidad.isBefore(fecha);
    }

    public boolean tieneTramosAsignados() {
        return !tramosProgramados.isEmpty() || tramoEnEjecucion != null;
    }

    public int cantidadTramosAsignados() {
        return tramosProgramados.size();
    }

    public Long getProximoTramoAEjecutar() {
        return tramosProgramados.isEmpty() ? null : tramosProgramados.get(0);
    }
}
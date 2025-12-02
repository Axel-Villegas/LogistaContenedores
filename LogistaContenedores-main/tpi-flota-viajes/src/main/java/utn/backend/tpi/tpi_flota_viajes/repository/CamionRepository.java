package utn.backend.tpi.tpi_flota_viajes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utn.backend.tpi.tpi_flota_viajes.model.Camion;
import utn.backend.tpi.tpi_flota_viajes.model.EstadoCamion;
import utn.backend.tpi.tpi_flota_viajes.model.Transportista;

import java.util.Optional;

import java.util.List;

public interface CamionRepository extends JpaRepository<Camion, Long> {

    //Metodo modificado para que devuelva un Optional (estaba hecho con una lista algo sin sentido)
    Optional<Camion> findByDominio(String dominio);

    @Query("SELECT c FROM Camion c WHERE c.transportista.id = :transportistaId")
    List<Camion> findByTransportistaId(@Param("transportistaId") Long transportistaId);

    long countByEstado(EstadoCamion estado);

    @Query("SELECT COUNT(c) FROM Camion c WHERE c.transportista.id = :transportistaId")
    long countByTransportistaId(@Param("transportistaId") Long transportistaId);

    @Query("SELECT c FROM Camion c WHERE c.estado = :estado " +
                    "AND c.capacidadPeso >= :peso " +
                    "AND c.capacidadVolumen >= :volumen")
    List<Camion> findDisponiblesConCapacidad(
                    @Param("estado") EstadoCamion estado,
                    @Param("peso") Double peso,
                    @Param("volumen") Double volumen);

    boolean existsByTransportistaAndEstado(Transportista transportista, EstadoCamion estado);

    List<Camion> findByTransportistaIdAndEstado(Long transportistaId, EstadoCamion estado);
}
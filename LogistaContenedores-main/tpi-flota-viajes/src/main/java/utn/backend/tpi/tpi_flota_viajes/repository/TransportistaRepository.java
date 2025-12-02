package utn.backend.tpi.tpi_flota_viajes.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utn.backend.tpi.tpi_flota_viajes.model.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {

    // Buscar transportista por licencia (debe ser único)
    java.util.List<Transportista> findByLicencia(String licencia);

    // Buscar solo activos
    @Query("SELECT t FROM Transportista t WHERE t.activo = true")
    List<Transportista> findAllActivos();

    @Query("SELECT t FROM Transportista t LEFT JOIN FETCH t.camiones WHERE t.id = :id AND t.activo = true")
    Optional<Transportista> findByIdWithCamionesActivo(@Param("id") Long id);
}

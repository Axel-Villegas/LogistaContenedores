package com.logistica.repository;

import com.logistica.model.Tramo;
import com.logistica.model.EstadoTramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {

    List<Tramo> findByRutaId(Long rutaId);

    List<Tramo> findByEstado(EstadoTramo estado);

    List<Tramo> findByRutaIdAndEstado(Long rutaId, EstadoTramo estado);

    @Query("SELECT t FROM Tramo t WHERE t.ruta.nroSolicitudRef = :nroSolicitud")
    List<Tramo> findByNroSolicitud(@Param("nroSolicitud") String nroSolicitud);

    List<Tramo> findByEstadoNot(EstadoTramo estado);


    @Query("SELECT t FROM Tramo t WHERE t.dominioCamionRef = :dominio " +
           "AND t.estado IN ('ASIGNADO', 'EN_CURSO')")
    List<Tramo> findByDominioCamionRefAndEstadoIn(@Param("dominio") String dominio);
}
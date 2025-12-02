package com.logistica.repository;

import com.logistica.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    Ruta findByNroSolicitudRef(String nroSolicitudRef);
}
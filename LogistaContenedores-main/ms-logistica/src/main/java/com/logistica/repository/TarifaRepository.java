package com.logistica.repository;

import com.logistica.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;


@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    List<Tarifa> findByFechaVigencia(LocalDate fechaVigencia);
}
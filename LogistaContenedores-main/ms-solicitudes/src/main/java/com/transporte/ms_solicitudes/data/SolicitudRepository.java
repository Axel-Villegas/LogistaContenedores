package com.transporte.ms_solicitudes.data;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transporte.ms_solicitudes.model.EstadoSolicitud;
import com.transporte.ms_solicitudes.model.Solicitud;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, String>{
    List<Solicitud> findByEstado(EstadoSolicitud estado);
}

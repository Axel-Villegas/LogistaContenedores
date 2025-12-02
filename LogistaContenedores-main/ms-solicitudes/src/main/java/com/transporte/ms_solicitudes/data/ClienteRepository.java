package com.transporte.ms_solicitudes.data;

import com.transporte.ms_solicitudes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
}

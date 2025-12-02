package com.transporte.ms_solicitudes.data;

import com.transporte.ms_solicitudes.model.EstadoSolicitud;
import com.transporte.ms_solicitudes.model.Solicitud;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SolicitudesStore {

    private final Map<String, Solicitud> data = new ConcurrentHashMap<>();

    // Nueva solicitud
    public Solicitud crearSolicitud(String idCliente, String idContenedor, Double destinoLat, Double destinoLon) {
        String nro = UUID.randomUUID().toString().substring(0, 8);
        Solicitud s = Solicitud.builder()
                .nroSolicitud(nro)
                .estado(EstadoSolicitud.BORRADOR)
                .idCliente(idCliente)
                .idContenedor(idContenedor)
                .destinoLatitud(destinoLat)
                .destinoLongitud(destinoLon)
                .build();
        data.put(nro, s);
        return s;
    }

    public Optional<Solicitud> findByNro(String nro) {
        return Optional.ofNullable(data.get(nro));
    }

    public List<Solicitud> findPendientes() {
        return data.values().stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.BORRADOR)
                .collect(Collectors.toList());
    }

    public Optional<Solicitud> aceptarSolicitud(String nro) {
        return findByNro(nro).map(s -> {
            s.setEstado(EstadoSolicitud.ACEPTADA);
            data.put(nro, s);
            return s;
        });
    }
}

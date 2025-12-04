package com.transporte.ms_solicitudes.service;

import com.transporte.ms_solicitudes.api.dto.EstadoContenedorDTO;
import com.transporte.ms_solicitudes.api.dto.SolicitudRequestDTO;
import com.transporte.ms_solicitudes.api.dto.TramoFinalizadoDTO;
import com.transporte.ms_solicitudes.data.ClienteRepository;
import com.transporte.ms_solicitudes.data.ContenedorRepository;
import com.transporte.ms_solicitudes.data.SolicitudRepository;
import com.transporte.ms_solicitudes.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolicitudesService {

    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;

    @Transactional
    public Solicitud crearSolicitud(SolicitudRequestDTO req, String idClienteAutenticado) {

        // 1. REGISTRAR CLIENTE (Si no existe previamente) [Requerimiento 1.2]
        Cliente cliente = clienteRepository.findById(idClienteAutenticado)
                .orElseGet(() -> clienteRepository.save(Cliente.builder()
                        .idCliente(idClienteAutenticado)
                        .nombre("Cliente " + idClienteAutenticado)
                        .email("pendiente@email.com")
                        .build()));

        // 2. CREAR/ACTUALIZAR CONTENEDOR
        //ojo aca porque instancia vacia si no existe???
        Contenedor contenedor = contenedorRepository.findById(req.getContenedor().getIdContenedor())
                .orElse(new Contenedor()); // Si no existe, instancia vacía

        // Actualizamos datos siempre (por si cambiaron)
        contenedor.setIdContenedor(req.getContenedor().getIdContenedor());
        contenedor.setPeso(req.getContenedor().getPeso());
        contenedor.setVolumen(req.getContenedor().getVolumen());
        contenedor.setRefrigerado(req.getContenedor().isRefrigerado());

        // ESTADO INICIAL: En el origen
        contenedor.setEstado(EstadoContenedor.EN_ORIGEN);
        contenedor.setUbicacionActual(req.getOrigen().getDireccion()); // "Fabrica Ford, Pacheco"

        contenedorRepository.save(contenedor);

        // 3. REGISTRAR SOLICITUD con estado BORRADOR [Requerimiento 1.3]
        String nro = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Solicitud solicitud = Solicitud.builder()
                .nroSolicitud(nro)
                .estado(EstadoSolicitud.BORRADOR)
                .idCliente(cliente.getIdCliente())
                .idContenedor(contenedor.getIdContenedor())

                // Datos de Origen
                .origenDireccion(req.getOrigen().getDireccion())
                .origenLatitud(req.getOrigen().getLatitud())
                .origenLongitud(req.getOrigen().getLongitud())

                // Datos de Destino
                .destinoDireccion(req.getDestino().getDireccion())
                .destinoLatitud(req.getDestino().getLatitud())
                .destinoLongitud(req.getDestino().getLongitud())

                // Los campos de costos y tiempos se inicializan en null/0
                // hasta que el Operador planifique la ruta.
                .build();

        return solicitudRepository.save(solicitud);
    }

    public Optional<Solicitud> findByNro(String nro) {
        return solicitudRepository.findById(nro);
    }

    public List<Solicitud> findPendientes() {
        return solicitudRepository.findByEstado(EstadoSolicitud.BORRADOR);
    }

    public List<Solicitud> findAll() {
        return solicitudRepository.findAll();
    }

    @Transactional
    public Optional<Solicitud> aceptarSolicitud(String nro) {
        return solicitudRepository.findById(nro).map(s -> {
            s.setEstado(EstadoSolicitud.ACEPTADA); // Cambio de estado a ACEPTADA (antes PROGRAMADA)
            return solicitudRepository.save(s);
        });
    }

    //Agregados para estado de solicitud y tracking
    @Transactional
    public void actualizarEstado(String nro, String nuevoEstadoStr, String mensaje) {
        Solicitud solicitud = solicitudRepository.findById(nro)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + nro));

        try {
            // Convertir String a Enum (Manejar posibles nombres distintos)
            EstadoSolicitud nuevoEstado = EstadoSolicitud.valueOf(nuevoEstadoStr);

            // Solo actualizamos si el estado avanza (opcional, regla de negocio)
            solicitud.setEstado(nuevoEstado);

            solicitudRepository.save(solicitud);
        } catch (IllegalArgumentException e) {
            // Si el estado que manda logística no coincide con el Enum de Solicitudes
            throw new RuntimeException("Estado inválido recibido: " + nuevoEstadoStr);
        }

        // ACTUALIZAR TRACKING CONTENEDOR
        Contenedor c = contenedorRepository.findById(solicitud.getIdContenedor()).orElse(null);
        if (c != null) {
            c.setEstado(EstadoContenedor.EN_TRANSITO);
            c.setUbicacionActual("En tránsito (Tramo iniciado)");
            contenedorRepository.save(c);
        }

    }

    @Transactional
    public void actualizarMetricas(String nro, TramoFinalizadoDTO info) {
        Solicitud solicitud = solicitudRepository.findById(nro)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + nro));

        Double costoExtra = info.getCostoReal();
        Double tiempoExtra = info.getTiempoReal();

        // Acumular costos y tiempos (manejar nulls por si es el primer tramo)
        // Manejo de nulos seguros
        double costoActual = solicitud.getCostoFinal() == null ? 0.0 : solicitud.getCostoFinal();
        double tiempoActual = solicitud.getTiempoReal() == null ? 0 : solicitud.getTiempoReal();
        solicitud.setCostoFinal(costoActual + (costoExtra != null ? costoExtra : 0.0));
        solicitud.setTiempoReal((int) (tiempoActual + (tiempoExtra != null ? tiempoExtra : 0.0)));

        // Si Logística dice que es el final, marcamos la solicitud como ENTREGADA aquí también
        if (info.isEsDestinoFinal()) {
            solicitud.setEstado(EstadoSolicitud.ENTREGADA);
        } else {
            // Si no, nos aseguramos que esté en tránsito
            solicitud.setEstado(EstadoSolicitud.EN_TRANSITO);
        }

        solicitudRepository.save(solicitud);

        // ACTUALIZAR TRACKING CONTENEDOR
        Contenedor c = contenedorRepository.findById(solicitud.getIdContenedor()).orElse(null);
        if (c != null) {
            if (info.isEsDestinoFinal()) {
                c.setEstado(EstadoContenedor.ENTREGADO);
            } else {
                c.setEstado(EstadoContenedor.EN_DEPOSITO);
            }
            // Guardamos dónde quedó (Ej: "Depósito Central" o "Domicilio Cliente")
            c.setUbicacionActual(info.getUbicacionFin());
            contenedorRepository.save(c);
        }
    }

    // Método para el endpoint de consulta
    public EstadoContenedorDTO obtenerEstadoContenedor(String idContenedor) {
        Contenedor c = contenedorRepository.findById(idContenedor)
                .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado"));

        return EstadoContenedorDTO.builder()
                .idContenedor(c.getIdContenedor())
                .estado(c.getEstado())
                .ubicacionActual(c.getUbicacionActual())
                .build();
    }
}

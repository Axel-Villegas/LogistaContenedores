package com.transporte.ms_solicitudes.api;

import com.transporte.ms_solicitudes.model.Solicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor

public class SolicitudesController {
    private final com.transporte.ms_solicitudes.service.SolicitudesService service;

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crear(@RequestBody SolicitudRequestDTO req) {
        if (req == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud no puede ser null");
        }
        if (req.destino() == null || req.origen() == null) { // <--- Validar origen
            throw new IllegalArgumentException("Origen y Destino no pueden ser null");
        }

        Solicitud nueva = service.crearSolicitud(
                req.idCliente(),
                req.idContenedor(),
                req.origen().lat(), // <--- Pasar latitud origen
                req.origen().lon(), // <--- Pasar longitud origen
                req.destino().lat(),
                req.destino().lon());

        SolicitudResponseDTO dto = new SolicitudResponseDTO(
                nueva.getNroSolicitud(),
                nueva.getEstado().name());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{nro}")
    public ResponseEntity<Solicitud> porNro(@PathVariable String nro) {

        return service.findByNro(nro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Solicitud> listarTodas() {
        return service.findAll();
    }

    @GetMapping("/pendientes")
    public List<Solicitud> pendientes() {

        return service.findPendientes();
    }

    @PutMapping("/{nro}/aceptar")
    public ResponseEntity<Solicitud> aceptar(@PathVariable String nro) {

        return service.aceptarSolicitud(nro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

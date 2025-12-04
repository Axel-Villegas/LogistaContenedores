package com.transporte.ms_solicitudes.api;

import com.transporte.ms_solicitudes.api.dto.*;
import com.transporte.ms_solicitudes.model.Solicitud;
import com.transporte.ms_solicitudes.service.SolicitudesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;




import java.util.List;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor

public class SolicitudesController {
    private final SolicitudesService service;

    /**
     * Crear solicitud (CLIENTE)
     * Requerimiento [cite: 176]
     */
    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> crear(
            @Valid @RequestBody SolicitudRequestDTO req,
            @AuthenticationPrincipal Jwt jwt) { // Obtenemos el token JWT

        // Extraemos el ID del usuario del token (simulado o real)
        // Si jwt es null (pruebas sin seguridad), usamos un default
        String idClienteAutenticado = (jwt != null) ? jwt.getClaimAsString("sub") : "cliente-anonimo";

        Solicitud nueva = service.crearSolicitud(req, idClienteAutenticado);

        // Respuesta simple al cliente confirmando creación
        SolicitudResponseDTO dto = SolicitudResponseDTO.builder()
                .nroSolicitud(nueva.getNroSolicitud())
                .estado(nueva.getEstado().name())
                .idCliente(nueva.getIdCliente())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
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

    //Endpoint para actualizar el tracking de la solicitud
    // Endpoint llamado por ms-logistica al iniciar tramo o finalizar ruta
    @PutMapping("/{nro}/estado")
    public ResponseEntity<Void> actualizarEstado(
            @PathVariable String nro,
            @RequestBody TramoIniciadoDTO request) { // Usamos el DTO de clase

        // Usamos request.getEstado() que vendrá como "EN_TRANSITO" o "ENTREGADA"
        // dependiendo de quién llame en logística
        String mensaje = (request.getIdTramo() != null && request.getIdTramo() != 0)
                ? "Tramo iniciado: " + request.getIdTramo()
                : "Actualización de estado general";

        service.actualizarEstado(nro, request.getEstado(), mensaje);
        return ResponseEntity.ok().build();
    }

    // Endpoint llamado por ms-logistica al finalizar un tramo (suma costos)
    @PutMapping("/{nro}/actualizar-metricas")
    public ResponseEntity<Void> actualizarMetricas(
            @PathVariable String nro,
            @RequestBody TramoFinalizadoDTO request) { // Usamos el DTO de clase

        service.actualizarMetricas(nro, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Consultar estado de un contenedor específico
     * GET /api/v1/solicitudes/contenedores/{id}
     */
    @GetMapping("/contenedores/{id}")
    public ResponseEntity<EstadoContenedorDTO> verEstadoContenedor(@PathVariable String id) {
        return ResponseEntity.ok(service.obtenerEstadoContenedor(id));
    }

}

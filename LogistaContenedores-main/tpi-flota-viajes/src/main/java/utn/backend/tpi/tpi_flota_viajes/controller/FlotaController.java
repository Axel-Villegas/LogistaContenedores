package utn.backend.tpi.tpi_flota_viajes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import utn.backend.tpi.tpi_flota_viajes.dto.request.*;
import utn.backend.tpi.tpi_flota_viajes.dto.response.CamionResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.response.TramoResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.response.TramoPendienteResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.response.TransportistaResponse;
import utn.backend.tpi.tpi_flota_viajes.service.CamionService;
import utn.backend.tpi.tpi_flota_viajes.service.TramoExecutionService;
import utn.backend.tpi.tpi_flota_viajes.service.TransportistaService;
import utn.backend.tpi.tpi_flota_viajes.dto.response.DisponibilidadCamionResponse;
import utn.backend.tpi.tpi_flota_viajes.model.Camion;

import java.time.LocalDateTime;
import java.util.List;
import utn.backend.tpi.tpi_flota_viajes.dto.request.AsignacionTramoDTO;
import utn.backend.tpi.tpi_flota_viajes.dto.request.ReasignacionRequest;


@RestController
@RequestMapping("/api/flota") // Ruta base para todo este MS
@RequiredArgsConstructor
public class FlotaController {

    private final CamionService camionService;
    private final TransportistaService transportistaService;
    private final TramoExecutionService tramoExecutionService;

    // --- Endpoints de FLOTA (OPERADOR) ---

    @PostMapping("/camiones")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CamionResponse> addCamion(
            @Valid @RequestBody CrearCamionRequest crearCamionRequest) {
        CamionResponse response = camionService.addCamion(crearCamionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/camiones/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CamionResponse> getCamionById(@PathVariable Long id) {
        return ResponseEntity.ok(camionService.obtenerPorId(id));
    }

    @GetMapping("/camiones/dominio/{dominio}")
    public ResponseEntity<CamionResponse> getCamionByDominio(@PathVariable String dominio) {
        return ResponseEntity.ok(camionService.obtenerPorDominio(dominio));
    }

    @GetMapping("/camiones")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<CamionResponse>> getAllCamiones() {
        return ResponseEntity.ok(camionService.listarTodos());
    }

    @PostMapping("/transportistas")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TransportistaResponse> addTransportista(
            @Valid @RequestBody CrearTransportistaRequest crearTransportistaRequest) {
        TransportistaResponse response = transportistaService.crear(crearTransportistaRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/transportistas/{id}/camiones")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<CamionResponse>> getCamionesPorTransportista(@PathVariable Long id) {
        return ResponseEntity.ok(camionService.obtenerPorTransportista(id));
    }

    @PostMapping("/tramos/{idTramo}/asignar")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> asignarTramo(
            @PathVariable Long idTramo, @RequestParam String dominio) {
        tramoExecutionService.asignarTramo(idTramo, dominio);
        return ResponseEntity.ok().build();
    }

    // --- Endpoints de VIAJES (TRANSPORTISTA) ---

    @PostMapping("/tramos/{idTramo}/iniciar")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<TramoResponse> iniciarTramo(@PathVariable Long idTramo) {
        TramoResponse tramoActualizado = tramoExecutionService.iniciarTramo(idTramo);
        return ResponseEntity.ok(tramoActualizado);
    }

    @PostMapping("/tramos/{idTramo}/finalizar")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<TramoResponse> finalizarTramo(
            @PathVariable Long idTramo,
            @Valid @RequestBody FinalizarTramoRequest request) {
        TramoResponse tramoActualizado = tramoExecutionService.finalizarTramo(idTramo, request.getKmRecorridos());
        return ResponseEntity.ok(tramoActualizado);
    }

    @GetMapping("/transportistas/{transportistaId}/tramos-pendientes")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<List<TramoPendienteResponse>> getTramosAsignados(@PathVariable Long transportistaId) {
        List<TramoPendienteResponse> tramos = tramoExecutionService.obtenerTramosAsignadosDelTransportista(transportistaId);
        return ResponseEntity.ok(tramos);
    }

    // --- Nuevos endpoints para gestión avanzada ---

    /**
     * Consultar disponibilidad de camiones por fecha y capacidad
     */
    @GetMapping("/camiones/disponibles")
    public ResponseEntity<List<DisponibilidadCamionResponse>> consultarDisponibilidad(
            @RequestParam LocalDateTime fecha,
            @RequestParam(required = false) Double pesoMinimo,
            @RequestParam(required = false) Double volumenMinimo) {

        List<Camion> camiones = camionService.buscarCamionesDisponiblesEn(
            fecha, pesoMinimo, volumenMinimo);

        List<DisponibilidadCamionResponse> response = camiones.stream()
            .map(camionService::mapearDisponibilidad)
            .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Asignar múltiples tramos consecutivos a un camión
     */
    @PostMapping("/camiones/{dominio}/asignar-multiples")
    public ResponseEntity<Void> asignarTramosConsecutivos(
            @PathVariable String dominio,
            @RequestBody List<AsignacionTramoDTO> tramos) {

        camionService.asignarTramosConsecutivos(dominio, tramos);
        return ResponseEntity.ok().build();
    }

    /**
     * Reasignar camión (cambiar de un tramo a otro)
     */
    @PostMapping("/camiones/{dominio}/reasignar")
    public ResponseEntity<Void> reasignarCamion(
            @PathVariable String dominio,
            @RequestBody ReasignacionRequest request) {

        camionService.reasignarCamion(dominio,
            request.getTramoAnterior(),
            request.getTramoNuevo(),
            request.getFechaInicio(),
            request.getFechaFin());

        return ResponseEntity.ok().build();
    }

    /**
     * Asignación múltiple simplificada (llamada desde ms-logistica)
     */
    @PostMapping("/camiones/{dominio}/asignar-multiples-simple")
    public ResponseEntity<Void> asignarTramosSimple(
            @PathVariable String dominio,
            @RequestBody AsignacionSimpleRequest request) {

        if (request.getTramoIds() != null) {
            for (Long tramoId : request.getTramoIds()) {
                camionService.asignarCamionATramo(dominio, tramoId);
            }
        }

        return ResponseEntity.ok().build();
    }
}
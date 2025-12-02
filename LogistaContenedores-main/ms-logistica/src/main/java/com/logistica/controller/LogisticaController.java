package com.logistica.controller;

import com.logistica.dto.request.PlanificarRutaRequest;
import com.logistica.dto.response.CalculoResponse;
import com.logistica.dto.response.DistanciaResponse;
import com.logistica.dto.AsignarCamionRequest;
import com.logistica.dto.AsignarTramosConsecutivosRequest;
import com.logistica.dto.ReasignarTramoRequest;
import com.logistica.dto.AsignacionResponse;
import com.logistica.dto.response.TramoResponse;
import com.logistica.exception.TramoNotFoundException;
import com.logistica.model.*;
import com.logistica.service.RutaService;
import com.logistica.service.DepositoService;
import com.logistica.service.TarifaService;
import com.logistica.service.TramoService;
import com.logistica.dto.response.RutaPlanningResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.logistica.dto.mapper.TramoMapper;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class LogisticaController {

    private final RutaService rutaService;
    private final DepositoService depositoService;
    private final TarifaService tarifaService;
    private final TramoService tramoService;
    private final TramoMapper tramoMapper;

    /**
     * Planifica una ruta calculando tramos, distancias y costos estimados
     * POST /api/v1/rutas/planificar
     */
    @PostMapping("/rutas/planificar")
    public ResponseEntity<RutaPlanningResponse> planificarRuta(@RequestBody PlanificarRutaRequest request) {
        log.info("Planificando ruta para solicitud: {}", request.getNroSolicitud());

        try {
            List<Deposito> depositos = request.getIdDepositos() != null && !request.getIdDepositos().isEmpty()
                    ? request.getIdDepositos().stream()
                            .map(depositoService::obtenerDeposito)
                            .toList()
                    : List.of();

            Tarifa tarifa = tarifaService.obtenerTarifa(request.getIdTarifa());

            RutaPlanningResponse response = rutaService.planificarRuta(
                    request.getNroSolicitud(),
                    depositos,
                    request.getLatOrigen(),
                    request.getLonOrigen(),
                    request.getLatDestino(),
                    request.getLonDestino(),
                    tarifa);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error planificando ruta: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/rutas/calcular")
    public ResponseEntity<CalculoResponse> calcularRuta(
            @RequestParam Double latOrigen,
            @RequestParam Double lonOrigen,
            @RequestParam Double latDestino,
            @RequestParam Double lonDestino,
            @RequestParam(required = false) Long idDepositos,
            @RequestParam Long idTarifa) {

        log.info("Calculando ruta con depósito: {}", idDepositos);

        try {
            Tarifa tarifa = tarifaService.obtenerTarifa(idTarifa);

            List<Deposito> depositos = idDepositos != null
                    ? List.of(depositoService.obtenerDeposito(idDepositos))
                    : List.of();

            DistanciaResponse distanciaResponse = rutaService.calcularDistancia(
                    latOrigen, lonOrigen, latDestino, lonDestino, depositos);

            double costoEstimado = distanciaResponse.getDistanciaKm() * tarifa.getValorKMBase();

            CalculoResponse response = new CalculoResponse(
                    distanciaResponse.getDistanciaKm(),
                    distanciaResponse.getTiempoSegundos(),
                    costoEstimado);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculando ruta: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Crea un nuevo depósito
     * POST /api/v1/depositos
     */
    @PostMapping("/depositos")
    public ResponseEntity<Deposito> crearDeposito(@RequestBody Deposito deposito) {
        log.info("Creando depósito: {}", deposito.getNombre());
        Deposito depositoCreado = depositoService.crearDeposito(deposito);
        return ResponseEntity.status(HttpStatus.CREATED).body(depositoCreado);
    }

    /**
     * Crea una nueva tarifa
     * POST /api/v1/tarifas
     */
    @PostMapping("/tarifas")
    public ResponseEntity<Tarifa> crearTarifa(@RequestBody Tarifa tarifa) {
        log.info("Creando tarifa vigente desde: {}", tarifa.getFechaVigencia());
        Tarifa tarifaCreada = tarifaService.crearTarifa(tarifa);
        return ResponseEntity.status(HttpStatus.CREATED).body(tarifaCreada);
    }

    /**
     * INTERNO: Recibe notificación síncrona de MS Flota - Tramo iniciado
     * PUT /api/v1/tramos/{id}/iniciar
     */
    @PutMapping("/tramos/{id}/iniciar")
    public ResponseEntity<TramoResponse> iniciarTramo(@PathVariable Long id) {
        log.info("Tramo {} iniciado (notificación de MS Flota)", id);
        Tramo tramo = tramoService.marcarTramoIniciado(id);
        return ResponseEntity.ok(tramoMapper.toResponse(tramo));
    }

    /**
     * INTERNO: Recibe notificación síncrona de MS Flota - Tramo finalizado
     * PUT /api/v1/tramos/{id}/finalizar
     */
    @PutMapping("/tramos/{id}/finalizar")
    public ResponseEntity<TramoResponse> finalizarTramo(@PathVariable Long id, @RequestParam double kmRecorridos) {
        log.info("Tramo {} finalizado con {} km", id, kmRecorridos);
        Tramo tramo = tramoService.marcarTramoFinalizado(id, kmRecorridos);
        return ResponseEntity.ok(tramoMapper.toResponse(tramo));
    }

    /**
     * Asigna un camión a un tramo con planificación (llamado por Operador)
     * POST /api/v1/tramos/{id}/asignar-camion
     */
    @PostMapping("/tramos/{id}/asignar-camion")
    public ResponseEntity<AsignacionResponse> asignarCamionConPlanificacion(
            @PathVariable Long id,
            @RequestBody AsignarCamionRequest request) {
        log.info("Asignando camión {} al tramo {} con planificación: {} - {}",
                request.getCamionDominio(), id,
                request.getFechaHoraInicioEstimada(), request.getFechaHoraFinEstimada());

        tramoService.asignarCamionConPlanificacion(id, request.getCamionDominio(),
                request.getFechaHoraInicioEstimada(), request.getFechaHoraFinEstimada());

        // Devolver respuesta simple con información básica
        AsignacionResponse response = new AsignacionResponse(id, request.getCamionDominio(), "ASIGNADO");
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un tramo por su ID
     * GET /api/v1/tramos/{id}
     */
    @GetMapping("/tramos/{id}")
    public ResponseEntity<TramoResponse> getTramoById(@PathVariable Long id) {
        return tramoService.obtenerTramo(id)
                .map(tramo -> ResponseEntity.ok(tramoMapper.toResponse(tramo)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * INTERNO: Obtiene detalles de un tramo específico
     * GET /api/v1/tramos/{id}/detalle
     */
    @GetMapping("/tramos/{id}/detalle")
    public ResponseEntity<TramoResponse> obtenerTramoDetalle(@PathVariable Long id) {
        Tramo tramo = tramoService.obtenerTramo(id)
                .orElseThrow(() -> new TramoNotFoundException(id));
        return ResponseEntity.ok(tramoMapper.toResponse(tramo));
    }

    /**
     * Consulta contenedores pendientes de entrega y su ubicación
     * GET /api/v1/contenedores/pendientes
     */
    @GetMapping("/contenedores/pendientes")
    public ResponseEntity<List<TramoResponse>> obtenerContenedoresPendientes() {
        List<TramoResponse> response = tramoService.obtenerTramosPendientes().stream()
                .map(tramoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    //Estos estan devolviendo entidades directamente
    @GetMapping("/depositos")
    public ResponseEntity<List<Deposito>> listarDepositos() {
        return ResponseEntity.ok(depositoService.listarDepositos());
    }

    @GetMapping("/tarifas")
    public ResponseEntity<List<Tarifa>> listarTarifas() {
        return ResponseEntity.ok(tarifaService.listarTarifas());
    }

    @GetMapping("/tramos")
    public ResponseEntity<List<TramoResponse>> listarTramos() {
        List<TramoResponse> response = tramoService.listarTramos().stream()
                .map(tramoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    /**
     * Obtener tramos de una ruta específica
     * GET /api/v1/rutas/{rutaId}/tramos
     */
    @GetMapping("/rutas/{rutaId}/tramos")
    public ResponseEntity<List<TramoResponse>> obtenerTramosPorRuta(@PathVariable Long rutaId) {
        List<TramoResponse> response = tramoService.obtenerTramosPorRuta(rutaId).stream()
                .map(tramoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    // --- Endpoints de Asignacion ---

    /**
     * Asignar múltiples tramos consecutivos a un camión
     * POST /api/v1/tramos/asignar-consecutivos
     */
    @PostMapping("/tramos/asignar-consecutivos")
    public ResponseEntity<Void> asignarTramosConsecutivos(
            @RequestBody AsignarTramosConsecutivosRequest request) {

        tramoService.asignarTramosConsecutivos(
                request.getCamionDominio(),
                request.getTramoIds(),
                request.getFechasInicio(),
                request.getFechasFin()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Reasignar tramo a otro camión
     * POST /api/v1/tramos/{id}/reasignar
     */
    @PostMapping("/tramos/{id}/reasignar")
    public ResponseEntity<TramoResponse> reasignarTramo(
            @PathVariable Long id,
            @RequestBody ReasignarTramoRequest request) {

        // 1. Ejecutar lógica (devuelve Entidad)
        Tramo tramoActualizado = tramoService.reasignarTramo(id,
                request.getNuevoCamionDominio(),
                request.getNuevaFechaInicio(),
                request.getNuevaFechaFin());

        // 2. Convertir a DTO usando el Mapper
        return ResponseEntity.ok(tramoMapper.toResponse(tramoActualizado));
    }
}
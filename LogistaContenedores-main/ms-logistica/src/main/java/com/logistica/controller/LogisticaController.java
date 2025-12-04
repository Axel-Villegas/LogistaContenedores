package com.logistica.controller;

import com.logistica.dto.request.PlanificarRutaRequest;
import com.logistica.dto.response.*;
import com.logistica.dto.AsignarCamionRequest;
import com.logistica.dto.AsignarTramosConsecutivosRequest;
import com.logistica.dto.ReasignarTramoRequest;
import com.logistica.dto.AsignacionResponse;
import com.logistica.exception.TramoNotFoundException;
import com.logistica.model.*;
import com.logistica.service.RutaService;
import com.logistica.service.DepositoService;
import com.logistica.service.TarifaService;
import com.logistica.service.TramoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.logistica.dto.mapper.TramoMapper;
import com.logistica.dto.request.CrearTarifaRequest;
import com.logistica.dto.mapper.TarifaMapper;
import jakarta.validation.Valid;
import com.logistica.dto.request.CrearDepositoRequest;
import com.logistica.dto.mapper.DepositoMapper;
import com.logistica.dto.mapper.RutaMapper;

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
    private final TarifaMapper tarifaMapper;
    private final DepositoMapper depositoMapper;
    private final RutaMapper rutaMapper;

    //ENDPOINTS DE RUTAS

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
     * Obtener una ruta por ID
     * GET /api/v1/rutas/{id}
     */
    @GetMapping("/rutas/{id}")
    public ResponseEntity<RutaResponse> obtenerRuta(@PathVariable Long id) {
        Ruta ruta = rutaService.obtenerRuta(id);
        return ResponseEntity.ok(rutaMapper.toResponse(ruta));
    }

    /**
     * Obtener ruta por número de solicitud
     * GET /api/v1/rutas/solicitud/{nroSolicitud}
     */
    @GetMapping("/rutas/solicitud/{nroSolicitud}")
    public ResponseEntity<RutaResponse> obtenerRutaPorSolicitud(@PathVariable String nroSolicitud) {
        Ruta ruta = rutaService.obtenerRutaPorSolicitud(nroSolicitud);
        if (ruta == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rutaMapper.toResponse(ruta));
    }

    // REQUERIMIENTO 3: Consultar rutas tentativas
    @PostMapping("/rutas/tentativas")
    public ResponseEntity<List<RutaTentativaResponse>> consultarRutasTentativas(@RequestBody PlanificarRutaRequest request) {
        log.info("Consultando rutas tentativas para solicitud: {}", request.getNroSolicitud());

        // Recuperar entidades necesarias (igual que en planificar)
        List<Deposito> depositos = request.getIdDepositos() != null
                ? request.getIdDepositos().stream().map(depositoService::obtenerDeposito).toList()
                : List.of();

        Tarifa tarifa = tarifaService.obtenerTarifa(request.getIdTarifa());

        List<RutaTentativaResponse> alternativas = rutaService.obtenerRutasTentativas(
                depositos,
                request.getLatOrigen(), request.getLonOrigen(),
                request.getLatDestino(), request.getLonDestino(),
                tarifa
        );

        return ResponseEntity.ok(alternativas);
    }


    //ENDPOINTS DE TARIFAS

    /**
     * Crea una nueva tarifa
     * POST /api/v1/tarifas
     */
    @PostMapping("/tarifas")
    public ResponseEntity<TarifaResponse> crearTarifa(@Valid @RequestBody CrearTarifaRequest request) {
        log.info("Creando tarifa vigente desde: {}", request.getFechaVigencia());

        // 1. Convertir Request -> Entidad
        Tarifa tarifaEntity = tarifaMapper.toEntity(request);

        // 2. Llamar al servicio
        Tarifa tarifaCreada = tarifaService.crearTarifa(tarifaEntity);

        // 3. Convertir Entidad -> Response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tarifaMapper.toResponse(tarifaCreada));
    }

    /**
     * Listar tarifas
     * GET /api/v1/tarifas
     */
    @GetMapping("/tarifas")
    public ResponseEntity<List<TarifaResponse>> listarTarifas() {
        List<TarifaResponse> response = tarifaService.listarTarifas().stream()
                .map(tarifaMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza una tarifa existente
     * PUT /api/v1/tarifas/{id}
     */
    @PutMapping("/tarifas/{id}")
    public ResponseEntity<TarifaResponse> actualizarTarifa(@PathVariable Long id, @Valid @RequestBody CrearTarifaRequest request) {
        Tarifa tarifaEntity = tarifaMapper.toEntity(request);
        Tarifa actualizada = tarifaService.actualizarTarifa(id, tarifaEntity);
        return ResponseEntity.ok(tarifaMapper.toResponse(actualizada));
    }

    //ENDPOINTS DE DEPOSITOS

    /**
     * Crea un nuevo depósito
     * POST /api/v1/depositos
     */
    @PostMapping("/depositos")
    public ResponseEntity<DepositoResponse> crearDeposito(@Valid @RequestBody CrearDepositoRequest request) {
        log.info("Creando depósito: {}", request.getNombre());

        // 1. DTO -> Entidad
        Deposito depositoEntity = depositoMapper.toEntity(request);

        // 2. Lógica de negocio
        Deposito depositoCreado = depositoService.crearDeposito(depositoEntity);

        // 3. Entidad -> DTO Response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(depositoMapper.toResponse(depositoCreado));
    }

    /**
     * Listar depósitos
     * GET /api/v1/depositos
     */
    @GetMapping("/depositos")
    public ResponseEntity<List<DepositoResponse>> listarDepositos() {
        List<DepositoResponse> response = depositoService.listarDepositos().stream()
                .map(depositoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza un depósito existente
     * PUT /api/v1/depositos/{id}
     */
    @PutMapping("/depositos/{id}")
    public ResponseEntity<DepositoResponse> actualizarDeposito(
            @PathVariable Long id,
            @Valid @RequestBody CrearDepositoRequest request) { // Reutilizamos el Request DTO

        log.info("Actualizando depósito ID {}: {}", id, request.getNombre());

        // 1. DTO -> Entidad (Temporal con los datos nuevos)
        Deposito nuevosDatos = depositoMapper.toEntity(request);

        // 2. Llamar al servicio para que actualice
        Deposito depositoActualizado = depositoService.actualizarDeposito(id, nuevosDatos);

        // 3. Entidad -> DTO Response
        return ResponseEntity.ok(depositoMapper.toResponse(depositoActualizado));
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

    /**
     * Listar todos los tramos
     * GET /api/v1/tramos
     */
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
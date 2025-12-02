package utn.backend.tpi.tpi_flota_viajes.service;

import org.springframework.dao.DataIntegrityViolationException;
import utn.backend.tpi.tpi_flota_viajes.dto.request.CrearCamionRequest;
import utn.backend.tpi.tpi_flota_viajes.dto.response.CamionResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.mapper.CamionMapper;
import utn.backend.tpi.tpi_flota_viajes.model.Camion;
import utn.backend.tpi.tpi_flota_viajes.model.EstadoCamion;
import utn.backend.tpi.tpi_flota_viajes.model.Transportista;
import utn.backend.tpi.tpi_flota_viajes.repository.CamionRepository;
import utn.backend.tpi.tpi_flota_viajes.repository.TransportistaRepository;
import utn.backend.tpi.tpi_flota_viajes.exception.NotFoundException;
import utn.backend.tpi.tpi_flota_viajes.exception.ConflictException;
import utn.backend.tpi.tpi_flota_viajes.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamionService {
    private final CamionRepository camionRepository;
    private final TransportistaRepository transportistaRepository;

    /**
     * Crear un nuevo camión
     * Validar: transportista existe, capacidades positivas
     * Maneja race condition en validación de dominio único
     */
    @Transactional
    public CamionResponse addCamion(CrearCamionRequest dto) {
        log.info("Iniciando creación de camión con dominio: {}", dto.getDominio());

        // Check if exists
        Optional<Camion> existing = camionRepository.findByDominio(dto.getDominio().toUpperCase());

        if (existing.isPresent()) {
            Camion c = existing.get();

            // Validate it belongs to the same transportista
            if (c.getTransportista() == null || !c.getTransportista().getId().equals(dto.getIdTransportista())) {
                log.warn("Conflicto: Camión {} ya existe pero pertenece a otro transportista", dto.getDominio());
                throw new ConflictException(
                        "El dominio " + dto.getDominio() + " ya existe y pertenece a otro transportista");
            }
            log.info("Camion ya existe con dominio: {}. Retornando existente.", dto.getDominio());
            return CamionMapper.toResponse(c);
        }

        // VALIDACIÓN 1: Transportista existe
        Transportista transportista = transportistaRepository.findById(dto.getIdTransportista())
                .orElseThrow(() -> {
                    log.warn("Transportista no encontrado con ID: {}", dto.getIdTransportista());
                    return new NotFoundException("Transportista con ID " + dto.getIdTransportista() + " no encontrado");
                });

        // VALIDACIÓN 1.5: Transportista debe estar ACTIVO
        if (transportista.getActivo() == null || !transportista.getActivo()) {
            log.warn("Intento de crear camión para transportista inactivo: {}", dto.getIdTransportista());
            throw new ConflictException("No se puede crear camión para un transportista inactivo");
        }

        // VALIDACIÓN 2: Capacidades positivas
        if (dto.getCapacidadPeso() <= 0 || dto.getCapacidadVolumen() <= 0) {
            log.warn("Intento de crear camión con capacidades inválidas. Peso: {}, Volumen: {}",
                    dto.getCapacidadPeso(), dto.getCapacidadVolumen());
            throw new BadRequestException("Las capacidades deben ser mayores a 0");
        }

        if (dto.getConsumoCombustiblePromedio() <= 0 || dto.getCostoPorKm() <= 0) {
            log.warn("Intento de crear camión con consumo o costo inválido. Consumo: {}, Costo: {}",
                    dto.getConsumoCombustiblePromedio(), dto.getCostoPorKm());
            throw new BadRequestException("Consumo y costo deben ser mayores a 0");
        }

        try {
            // CREAR CAMIÓN
            Camion camion = Camion.builder()
                    .dominio(dto.getDominio().toUpperCase()) // Normalizar a mayúsculas
                    .capacidadPeso(dto.getCapacidadPeso())
                    .capacidadVolumen(dto.getCapacidadVolumen())
                    .consumoCombustiblePromedio(dto.getConsumoCombustiblePromedio())
                    .costoPorKm(dto.getCostoPorKm())
                    .estado(EstadoCamion.DISPONIBLE) // Nuevo camión siempre comienza DISPONIBLE
                    .transportista(transportista)
                    .tramoEnEjecucion(null) // Sin tramo en ejecución al inicio
                    .kmRecorridos(0.0) // Inicia en 0
                    .build();

            Camion guardado = camionRepository.save(camion);
            log.info("Camión creado exitosamente con ID: {}, dominio: {}, transportista: {}",
                    guardado.getId(), guardado.getDominio(), transportista.getNombre());

            return CamionMapper.toResponse(guardado);

        } catch (DataIntegrityViolationException e) {
            // Captura race condition: dos peticiones simultáneas con mismo dominio
            log.error("Error de integridad: dominio duplicado en BD - {}. Transportista: {}",
                    dto.getDominio(), dto.getIdTransportista(), e);
            throw new ConflictException("El dominio " + dto.getDominio() + " ya está registrado");
        }
    }

    /**
     * Obtener camión por ID
     */
    public CamionResponse obtenerPorId(Long id) {
        log.debug("Buscando camión con ID: {}", id);

        Camion camion = camionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Camión no encontrado con ID: {}", id);
                    return new NotFoundException("Camión con ID " + id + " no encontrado");
                });

        return CamionMapper.toResponse(camion);
    }

    /**
     * Obtener camión por dominio
     */
    /**
     * Obtener camión por dominio
     */
    public CamionResponse obtenerPorDominio(String dominio) {
        log.debug("Buscando camión con dominio: {}", dominio);

        Camion camion = camionRepository.findByDominio(dominio.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("Camión no encontrado con dominio: {}", dominio);
                    return new NotFoundException("Camión con dominio " + dominio + " no encontrado");
                });

        return CamionMapper.toResponse(camion);
    }


    /**
     * Cambiar estado de un camión
     * Valida transiciones de estado válidas
     * Uso interno: cuando inicia/finaliza un tramo o entra en mantenimiento
     */
    @Transactional
    public void cambiarEstado(Long camionId, EstadoCamion nuevoEstado) {
        log.debug("Cambiando estado del camión {} a: {}", camionId, nuevoEstado);

        Camion camion = camionRepository.findById(camionId)
                .orElseThrow(() -> {
                    log.warn("Camión no encontrado para cambiar estado. ID: {}", camionId);
                    return new NotFoundException("Camión con ID " + camionId + " no encontrado");
                });

        EstadoCamion estadoActual = camion.getEstado();

        // VALIDACIÓN: Transiciones permitidas
        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            log.warn("Transición de estado no permitida. Camión: {}, De: {}, A: {}",
                    camionId, estadoActual, nuevoEstado);
            throw new ConflictException(
                    String.format("No se puede cambiar estado de %s a %s", estadoActual, nuevoEstado));
        }

        camion.setEstado(nuevoEstado);
        camionRepository.save(camion);

        log.info("Estado del camión {} actualizado de {} a {}",
                camionId, estadoActual, nuevoEstado);
    }

    /**
     * Valida si la transición de estado es permitida
     * Transiciones válidas:
     * - DISPONIBLE ↔ EN_USO
     * - DISPONIBLE ↔ MANTENIMIENTO
     * - EN_VIAJE → DISPONIBLE (solo cuando se libera)
     */
    private boolean esTransicionValida(EstadoCamion estadoActual, EstadoCamion nuevoEstado) {

        // No cambiar a sí mismo
        if (estadoActual == nuevoEstado) {
            return true; // Es válido no cambiar
        }

        // Transiciones permitidas
        return switch (estadoActual) {
            case DISPONIBLE -> nuevoEstado == EstadoCamion.ASIGNADO ||
                             nuevoEstado == EstadoCamion.PROGRAMADO ||
                             nuevoEstado == EstadoCamion.MANTENIMIENTO;
            case ASIGNADO -> nuevoEstado == EstadoCamion.EN_VIAJE ||
                           nuevoEstado == EstadoCamion.DISPONIBLE ||
                           nuevoEstado == EstadoCamion.PROGRAMADO;
            case PROGRAMADO -> nuevoEstado == EstadoCamion.ASIGNADO ||
                             nuevoEstado == EstadoCamion.DISPONIBLE;
            case EN_VIAJE -> nuevoEstado == EstadoCamion.DISPONIBLE ||
                           nuevoEstado == EstadoCamion.ASIGNADO; // Puede ir a ASIGNADO si tiene más tramos
            case MANTENIMIENTO -> nuevoEstado == EstadoCamion.DISPONIBLE;
        };
    }



    /**
     * Obtener todos los camiones de un transportista
     */
    @Transactional(readOnly = true)
    public List<CamionResponse> obtenerPorTransportista(Long transportistaId) {
        log.debug("Buscando camiones del transportista: {}", transportistaId);

        // VALIDACIÓN: Transportista debe existir
        Transportista transportista = transportistaRepository.findById(transportistaId)
                .orElseThrow(() -> {
                    log.warn("Transportista no encontrado para obtener camiones. ID: {}", transportistaId);
                    return new NotFoundException("Transportista con ID " + transportistaId + " no encontrado");
                });

        List<Camion> camiones = camionRepository.findByTransportistaId(transportistaId);
        log.info("Se encontraron {} camiones para transportista: {} ({})",
                camiones.size(), transportistaId, transportista.getNombre());

        return camiones.stream()
                .map(CamionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Contar camiones disponibles (para estadísticas)
     */
    public Long contarDisponibles() {
        long cantidad = camionRepository.countByEstado(EstadoCamion.DISPONIBLE);
        log.debug("Cantidad de camiones disponibles: {}", cantidad);
        return cantidad;
    }

    @Transactional(readOnly = true)
    public List<CamionResponse> listarTodos() {
        return camionRepository.findAll().stream()
                .map(CamionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Asignar tramo a un camión
     * Se ejecuta cuando se asigna un camión a un tramo
     */
    @Transactional
    public void ocuparCamion(String dominio, Long tramoId) {
        log.debug("Asignando tramo {} al camión {}", tramoId, dominio);

        // VALIDACIÓN: tramoId debe ser válido (capaz esta de mas esta validacion)
        if (tramoId == null || tramoId <= 0) {
            log.warn("Intento de asignar tramoId inválido. Camión: {}, Tramo: {}", dominio, tramoId);
            throw new BadRequestException("ID de tramo inválido");
        }

        Camion camion = camionRepository.findByDominio(dominio)
                .orElseThrow(() -> new NotFoundException("Camión con dominio " + dominio + " no encontrado"));

        // VALIDACIÓN: Camión debe estar ASIGNADO al tramo para poder iniciarlo
        if (camion.getEstado() != EstadoCamion.ASIGNADO && camion.getEstado() != EstadoCamion.PROGRAMADO) {
            log.warn("Intento de iniciar tramo con camión en estado inválido. Camión: {}, Estado: {}, Tramo: {}",
                    dominio, camion.getEstado(), tramoId);
            throw new ConflictException("El camión no está listo para iniciar viajes (Estado actual: " + camion.getEstado() + ")");
        }

        // VALIDACIÓN: Debe ser el próximo tramo en la secuencia
        Long proximoTramo = camion.getProximoTramoAEjecutar();
        if (proximoTramo == null || !proximoTramo.equals(tramoId)) {
            log.warn("Intento de iniciar tramo fuera de secuencia. Camión: {}, ProximoTramo: {}, TramoSolicitado: {}",
                    dominio, proximoTramo, tramoId);
            throw new ConflictException("Debe ejecutar los tramos en orden secuencial. Próximo tramo: " + proximoTramo);
        }

        // VALIDACIÓN: transportista no puede tener otro camión en viaje
        Transportista chofer = camion.getTransportista();
        boolean tieneOtroCamionEnViaje = camionRepository.existsByTransportistaAndEstado(chofer, EstadoCamion.EN_VIAJE);

        if (tieneOtroCamionEnViaje) {
            throw new ConflictException("El transportista ya tiene un tramo en curso. Debe finalizarlo antes de iniciar otro.");
        }

        // Mover tramo de programados a ejecución
        camion.getTramosProgramados().remove(0);  // Remover primer tramo de la lista
        camion.setTramoEnEjecucion(tramoId);      // Marcarlo como en ejecución
        camion.setEstado(EstadoCamion.EN_VIAJE);  // Cambiar estado
        camionRepository.save(camion);

        log.info("Tramo {} asignado al camión {}. Transportista: {}",
                tramoId, dominio, camion.getTransportista().getNombre());
    }

    /**
     * Liberar camión después de finalizar un tramo
     */
    @Transactional
    public void liberarCamion(String dominio, Long tramoId, double kmRecorridos) {
        log.debug("Liberando camión: {}", dominio);

        //codigo correcto (sin que devuelva una lista y obtener el primero)
        Camion camion = camionRepository.findByDominio(dominio)
                .orElseThrow(() -> {
                    log.warn("Camión no encontrado para liberar. Dominio: {}", dominio);
                    return new NotFoundException("Camión con dominio " + dominio + " no encontrado");
                });

        // VALIDACIÓN: Debe estar EN_VIAJE
        if (camion.getEstado() != EstadoCamion.EN_VIAJE) {
            log.warn("Intento de liberar camión que no está en viaje. Dominio: {}, Estado: {}",
                    dominio, camion.getEstado());
            throw new ConflictException("El camión no está en viaje, no puede ser liberado");
        }

        // VALIDACIÓN: Debe estar en el tramo correcto
        if (camion.getTramoEnEjecucion() == null || !camion.getTramoEnEjecucion().equals(tramoId)) {
            log.warn(
                    "Intento de liberar camión de un tramo incorrecto. Dominio: {}, Tramo Camion: {}, Tramo Reportado: {}",
                    dominio, camion.getTramoEnEjecucion(), tramoId);
            throw new ConflictException("El camion no estaba asignado al tramo " + tramoId);
        }

        try {
            // Liberar tramo en ejecución
            camion.setTramoEnEjecucion(null);
            camion.setKmRecorridos(camion.getKmRecorridos() + kmRecorridos); // Acumular KMs

            // Actualizar estado según tramos restantes
            if (camion.getTramosProgramados().isEmpty()) {
                camion.setEstado(EstadoCamion.DISPONIBLE);
                camion.setProximaDisponibilidad(null);
            } else {
                camion.setEstado(EstadoCamion.ASIGNADO); // Listo para próximo tramo
                // La proximaDisponibilidad se mantendrá hasta el último tramo
            }

            camionRepository.save(camion);

            log.info("Camión {} liberado y disponible. Transportista: {}",
                    dominio, camion.getTransportista().getNombre());

        } catch (DataIntegrityViolationException e) {
            log.error("Error al liberar camión {}. Estado inconsistente.", dominio, e);
            throw new ConflictException("Error al liberar el camión. Contacte al administrador.");
        }
    }

    /**
     * Asignar camión a tramo (solo reserva, no inicia)
     * Usado por el operador para asignar trabajo al transportista
     */
    @Transactional
    public void asignarCamionATramo(String dominio, Long tramoId) {
        log.debug("Asignando camión {} al tramo {} (estado ASIGNADO)", dominio, tramoId);

        // VALIDACIÓN: tramoId debe ser válido
        if (tramoId == null || tramoId <= 0) {
            log.warn("Intento de asignar tramoId inválido. Camión: {}, Tramo: {}", dominio, tramoId);
            throw new BadRequestException("ID de tramo inválido");
        }

        Camion camion = camionRepository.findByDominio(dominio)
                .orElseThrow(() -> new NotFoundException("Camión con dominio " + dominio + " no encontrado"));

        // VALIDACIÓN: Camión debe estar disponible (DISPONIBLE o PROGRAMADO)
        if (camion.getEstado() == EstadoCamion.EN_VIAJE || camion.getEstado() == EstadoCamion.MANTENIMIENTO) {
            log.warn("Intento de asignar tramo a camión no disponible. Camión: {}, Estado: {}, Tramo: {}",
                    dominio, camion.getEstado(), tramoId);
            throw new ConflictException("El camión no está disponible para asignación");
        }

        // VALIDACIÓN: transportista no puede tener otro camión en viaje
        Transportista transportista = camion.getTransportista();
        boolean tieneOtroCamionEnViaje = camionRepository.existsByTransportistaAndEstado(transportista, EstadoCamion.EN_VIAJE);

        if (tieneOtroCamionEnViaje) {
            throw new ConflictException("El transportista ya tiene un tramo en curso");
        }

        // Agregar tramo a la lista de programados
        camion.getTramosProgramados().add(tramoId);

        // Actualizar estado
        if (camion.getTramosProgramados().size() == 1) {
            camion.setEstado(EstadoCamion.ASIGNADO); // Listo para ejecutar inmediatamente
        } else {
            camion.setEstado(EstadoCamion.PROGRAMADO); // Tiene múltiples tramos programados
        }

        camionRepository.save(camion);

        log.info("Camión {} asignado al tramo {}. Transportista: {} - Estado: {}",
                dominio, tramoId, transportista.getNombre(), camion.getEstado());
    }

    /**
     * Asignar múltiples tramos consecutivos con validación de disponibilidad
     */
    @Transactional
    public void asignarTramosConsecutivos(String dominio, java.util.List<utn.backend.tpi.tpi_flota_viajes.dto.request.AsignacionTramoDTO> tramos) {
        Camion camion = camionRepository.findByDominio(dominio)
                .orElseThrow(() -> new NotFoundException("Camión con dominio " + dominio + " no encontrado"));

        // Validar disponibilidad en todas las fechas
        for (utn.backend.tpi.tpi_flota_viajes.dto.request.AsignacionTramoDTO tramo : tramos) {
            if (!camion.estaDisponibleEn(tramo.getFechaInicio())) {
                throw new ConflictException("Camión no disponible en " + tramo.getFechaInicio());
            }
        }

        // Agregar tramos programados
        for (utn.backend.tpi.tpi_flota_viajes.dto.request.AsignacionTramoDTO tramo : tramos) {
            camion.getTramosProgramados().add(tramo.getTramoId());
        }

        // Actualizar disponibilidad y estado
        actualizarDisponibilidadCamion(camion, tramos);
        camionRepository.save(camion);

        log.info("Camión {} asignado a {} tramos consecutivos", dominio, tramos.size());
    }

    /**
     * Consultar disponibilidad futura de camiones
     * Valida capacidades y verifica la agenda de cada camión.
     */
    public java.util.List<Camion> buscarCamionesDisponiblesEn(java.time.LocalDateTime fecha,
                                                              Double pesoMinimo,
                                                              Double volumenMinimo) {
        log.info("Buscando camiones para fecha {}, peso={} kg, volumen={} m³",
                fecha, pesoMinimo, volumenMinimo);

        if ((pesoMinimo != null && pesoMinimo <= 0) || (volumenMinimo != null && volumenMinimo <= 0)) {
            throw new BadRequestException("Las capacidades requeridas deben ser mayores a 0");
        }

        // 2. Búsqueda y Filtrado
        return camionRepository.findAll().stream()
                .filter(c -> c.getEstado() != EstadoCamion.MANTENIMIENTO)
                .filter(c -> c.estaDisponibleEn(fecha))
                .filter(c -> pesoMinimo == null || c.getCapacidadPeso() >= pesoMinimo)
                .filter(c -> volumenMinimo == null || c.getCapacidadVolumen() >= volumenMinimo)
                .collect(Collectors.toList());
    }

    /**
     * Reasignar camión liberando tramo anterior
     */
    @Transactional
    public void reasignarCamion(String dominio, Long tramoAnterior, Long tramoNuevo,
                               java.time.LocalDateTime fechaInicio, java.time.LocalDateTime fechaFin) {
        Camion camion = camionRepository.findByDominio(dominio)
                .orElseThrow(() -> new NotFoundException("Camión con dominio " + dominio + " no encontrado"));

        // Remover tramo anterior si existe
        if (tramoAnterior != null) {
            camion.getTramosProgramados().remove(tramoAnterior);
        }

        // Agregar nuevo tramo
        if (tramoNuevo != null) {
            camion.getTramosProgramados().add(tramoNuevo);
        }

        // Recalcular disponibilidad
        recalcularProximaDisponibilidad(camion);
        actualizarEstadoSegunTramos(camion);

        camionRepository.save(camion);

        log.info("Camión {} reasignado del tramo {} al tramo {}", dominio, tramoAnterior, tramoNuevo);
    }

    /**
     * Mapear camión a DTO de disponibilidad
     */
    public utn.backend.tpi.tpi_flota_viajes.dto.response.DisponibilidadCamionResponse mapearDisponibilidad(Camion camion) {
        return utn.backend.tpi.tpi_flota_viajes.dto.response.DisponibilidadCamionResponse.builder()
                .dominio(camion.getDominio())
                .estado(camion.getEstado())
                .proximaDisponibilidad(camion.getProximaDisponibilidad())
                .tramosAsignados(new java.util.ArrayList<>(camion.getTramosProgramados()))
                .tramoEnEjecucion(camion.getTramoEnEjecucion())
                .cantidadTramosAsignados(camion.cantidadTramosAsignados())
                .capacidadPeso(camion.getCapacidadPeso())
                .capacidadVolumen(camion.getCapacidadVolumen())
                .nombreTransportista(camion.getTransportista() != null ?
                                   camion.getTransportista().getNombre() : "Sin asignar")
                .build();
    }

    // Métodos helper privados
    private void actualizarDisponibilidadCamion(Camion camion, java.util.List<utn.backend.tpi.tpi_flota_viajes.dto.request.AsignacionTramoDTO> tramos) {
        if (!tramos.isEmpty()) {
            // La disponibilidad será después del último tramo
            utn.backend.tpi.tpi_flota_viajes.dto.request.AsignacionTramoDTO ultimoTramo = tramos.get(tramos.size() - 1);
            camion.setProximaDisponibilidad(ultimoTramo.getFechaFin());
        }
        actualizarEstadoSegunTramos(camion);
    }

    private void recalcularProximaDisponibilidad(Camion camion) {
        if (camion.getTramosProgramados().isEmpty()) {
            camion.setProximaDisponibilidad(null);
        } else {
            // Aquí se podría consultar a ms-logistica para obtener la fecha fin del último tramo
            // Por simplicidad, mantenemos la fecha actual si ya existe
            if (camion.getProximaDisponibilidad() == null) {
                camion.setProximaDisponibilidad(java.time.LocalDateTime.now().plusHours(8)); // Estimación default
            }
        }
    }

    private void actualizarEstadoSegunTramos(Camion camion) {
        if (camion.getTramoEnEjecucion() != null) {
            camion.setEstado(EstadoCamion.EN_VIAJE);
        } else if (camion.getTramosProgramados().isEmpty()) {
            camion.setEstado(EstadoCamion.DISPONIBLE);
        } else if (camion.getTramosProgramados().size() == 1) {
            camion.setEstado(EstadoCamion.ASIGNADO);
        } else {
            camion.setEstado(EstadoCamion.PROGRAMADO);
        }
    }
}

package com.logistica.service;

import com.logistica.model.*;
import com.logistica.repository.TramoRepository;

import com.logistica.client.FlotaApiClient;
import com.logistica.event.TramoIniciado;
import com.logistica.event.TramoFinalizado;
import com.logistica.exception.TramoNotFoundException;
import com.logistica.exception.RutaNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import com.logistica.client.dto.CamionInfo;

@Service
@RequiredArgsConstructor
@Slf4j
public class TramoService {

    private final TramoRepository tramoRepository;

    private final RabbitTemplate rabbitTemplate;
    private final FlotaApiClient flotaApiClient;

    /**
     * Marca un tramo como INICIADO (llamado por MS Flota)
     * Publica evento a RabbitMQ para MS Solicitudes
     * 
     * @return El tramo actualizado
     */
    @Transactional
    public Tramo marcarTramoIniciado(Long idTramo) {
        // NUEVA VALIDACIÓN: Verificar secuencia de tramos
        validarSecuenciaTramos(idTramo);

        log.info("Marcando tramo {} como EN CURSO", idTramo);

        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new TramoNotFoundException(idTramo));

        Ruta ruta = tramo.getRuta();
        if (ruta == null) {
            throw new RutaNotFoundException(null);
        }

        tramo.setEstado(EstadoTramo.EN_CURSO);
        tramo.setFechaHoraInicioReal(LocalDateTime.now());
        tramoRepository.save(tramo);

        log.info("Tramo {} ahora está EN CURSO, publicando evento", idTramo);

        // Publica evento a RabbitMQ para MS Solicitudes
        try {
            TramoIniciado evento = new TramoIniciado(
                    idTramo,
                    ruta.getNroSolicitudRef(),
                    LocalDateTime.now(),
                    "INICIADO");

            rabbitTemplate.convertAndSend("solicitudes.exchange", "tramo.iniciado", evento);
            log.info("Evento TramoIniciado publicado para solicitud: {}", ruta.getNroSolicitudRef());
        } catch (Exception e) {
            log.error("Error al publicar evento TramoIniciado en RabbitMQ: {}", e.getMessage());
            // No relanzamos la excepción para que el cambio de estado en BD persista
        }

        return tramo;
    }

    /**
     * Marca un tramo como FINALIZADO (llamado por MS Flota)
     * Calcula costos y tiempos reales
     * Publica evento a RabbitMQ para MS Solicitudes
     * 
     * @return El tramo actualizado
     */
    @Transactional
    public Tramo marcarTramoFinalizado(Long idTramo, double kmRecorridos) {
        log.info("Marcando tramo {} como FINALIZADO con {} km recorridos", idTramo, kmRecorridos);

        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new TramoNotFoundException(idTramo));

        Ruta ruta = tramo.getRuta();
        if (ruta == null) {
            throw new RutaNotFoundException(null);
        }

        tramo.setFechaHoraFinReal(LocalDateTime.now());

        // Calcular costo real y tiempo real
        double costoReal = calcularCostoReal(tramo, kmRecorridos);
        double tiempoReal = calcularTiempoReal(tramo);

        tramo.setEstado(EstadoTramo.FINALIZADO);
        tramo.setKmRecorridos(kmRecorridos);
        tramo.setCostoReal(costoReal);
        tramo.setTiempoReal(tiempoReal);

        tramoRepository.save(tramo);

        log.info("Tramo {} finalizado. Costo real: ${}, Tiempo real: {}s",
                idTramo, costoReal, tiempoReal);

        // Publica evento a RabbitMQ para MS Solicitudes
        try {
            TramoFinalizado evento = new TramoFinalizado(
                    idTramo,
                    ruta.getNroSolicitudRef(),
                    kmRecorridos,
                    costoReal,
                    tiempoReal,
                    LocalDateTime.now(),
                    "FINALIZADO");

            rabbitTemplate.convertAndSend("solicitudes.exchange", "tramo.finalizado", evento);
            log.info("Evento TramoFinalizado publicado para solicitud: {}", ruta.getNroSolicitudRef());

            // Verificar si todos los tramos de la ruta están finalizados
            verificarYActualizarEstadoRuta(ruta);
        } catch (Exception e) {
            log.error("Error al publicar evento TramoFinalizado o actualizar ruta en RabbitMQ: {}", e.getMessage());
            // No relanzamos la excepción para que el cambio de estado en BD persista
        }

        return tramo;
    }

    /**
     * Verifica si todos los tramos de una ruta están finalizados
     * y publica evento para actualizar el estado de la solicitud
     */
    private void verificarYActualizarEstadoRuta(Ruta ruta) {
        List<Tramo> tramos = tramoRepository.findByRutaId(ruta.getId());
        boolean todosFinalizados = tramos.stream()
                .allMatch(t -> t.getEstado() == EstadoTramo.FINALIZADO);

        if (todosFinalizados && !tramos.isEmpty()) {
            // Calcular costos totales
            double costoTotalReal = tramos.stream()
                    .mapToDouble(Tramo::getCostoReal)
                    .sum();

            double tiempoTotalReal = tramos.stream()
                    .mapToDouble(Tramo::getTiempoReal)
                    .sum();

            log.info("Todos los tramos de la ruta {} están finalizados. Costo total: ${}, Tiempo total: {}s",
                    ruta.getId(), costoTotalReal, tiempoTotalReal);

            // Publicar evento para actualizar solicitud a ENTREGADA
            TramoFinalizado eventoFinal = new TramoFinalizado(
                    null, // idTramo null indica que es el evento final de toda la ruta
                    ruta.getNroSolicitudRef(),
                    0, // kmRecorridos no aplica
                    costoTotalReal,
                    tiempoTotalReal,
                    LocalDateTime.now(),
                    "ENTREGADA");

            rabbitTemplate.convertAndSend("solicitudes.exchange", "ruta.completada", eventoFinal);
            log.info("Evento de ruta completada publicado para solicitud: {}", ruta.getNroSolicitudRef());
        }
    }

    /**
     * Calcula el costo real del tramo según requerimientos:
     * - Costo por km del camión específico
     * - Costo de combustible (km * consumo del camión * precio del litro)
     * - Estadía en depósito (días * costo diario del depósito)
     */
    private double calcularCostoReal(Tramo tramo, double kmRecorridos) {
        Tarifa tarifa = tramo.getTarifa();
        double costoTotal = 0.0;

        // 1. COSTO DE TRASLADO (BASE DEL CAMIÓN)
        // Requisito: "Los camiones deben conocer su costo base de traslado por km"
        if (tramo.getDominioCamionRef() != null && !tramo.getDominioCamionRef().isEmpty()) {
            try {
                CamionInfo camion = flotaApiClient.obtenerCamionPorDominio(tramo.getDominioCamionRef());

                // A. Costo Base (Chofer, amortización, etc.)
                double costoBaseCamion = kmRecorridos * camion.getCostoPorKm();
                costoTotal += costoBaseCamion;
                log.debug("Costo base del camión {}: ${}", tramo.getDominioCamionRef(), costoBaseCamion);

                // B. Costo de Combustible (Específico del camión + Precio actual)
                // Requisito: "...más el costo de combustible del camión específico"
                // Consumo viene en L/100km, lo dividimos por 100 para tener L/km
                double litrosConsumidos = kmRecorridos * (camion.getConsumoCombustiblePromedio() / 100.0);

                double costoCombustible = litrosConsumidos * tarifa.getCostoLitroCombustible();
                costoTotal += costoCombustible;

                log.debug("Costo combustible: {} L (a ${}/L) = ${}",
                        String.format("%.2f", litrosConsumidos),
                        tarifa.getCostoLitroCombustible(),
                        String.format("%.2f", costoCombustible));
            } catch (Exception e) {
                log.warn("No se pudo obtener información del camión {}, usando tarifa base: {}",
                        tramo.getDominioCamionRef(), e.getMessage());
                // Fallback: usar tarifa base si no se puede obtener el camión
                costoTotal += kmRecorridos * tarifa.getValorKMBase();
            }
        } else {
            // Si no hay camión asignado, usar tarifa base
            costoTotal += kmRecorridos * tarifa.getValorKMBase();
        }

        // 3. Estadía en depósito (calculada entre fin de este tramo y inicio del
        // siguiente)
        // Nota: La estadía se calcula cuando el siguiente tramo inicia desde este
        // depósito
        // Por ahora, si el tramo termina en un depósito, calculamos estadía mínima de 1
        // día
        // La estadía completa se calculará cuando el siguiente tramo inicie
        if (tramo.getDepositoDestino() != null && tramo.getFechaHoraFinReal() != null) {
            // Buscar el siguiente tramo que sale de este depósito
            List<Tramo> tramosRuta = tramoRepository.findByRutaId(tramo.getRuta().getId());
            Tramo siguienteTramo = tramosRuta.stream()
                    .filter(t -> t.getDepositoOrigen() != null &&
                            t.getDepositoOrigen().getId().equals(tramo.getDepositoDestino().getId()) &&
                            t.getFechaHoraInicioReal() != null)
                    .findFirst()
                    .orElse(null);

            if (siguienteTramo != null && siguienteTramo.getFechaHoraInicioReal() != null) {
                // Calcular días entre fin de este tramo e inicio del siguiente
                long diasEstadia = ChronoUnit.DAYS.between(tramo.getFechaHoraFinReal(),
                        siguienteTramo.getFechaHoraInicioReal());
                if (diasEstadia > 0) {
                    double costoEstadia = diasEstadia * tramo.getDepositoDestino().getCostoEstadiaDiario();
                    costoTotal += costoEstadia;
                    log.debug("Costo de estadía en depósito {}: {} días * ${}/día = ${}",
                            tramo.getDepositoDestino().getNombre(), diasEstadia,
                            tramo.getDepositoDestino().getCostoEstadiaDiario(), costoEstadia);
                }
            } else if (tramo.getFechaHoraFinReal() != null) {
                // Si no hay siguiente tramo aún, asumir estadía mínima de 1 día
                // Esto se ajustará cuando el siguiente tramo inicie
                double costoEstadia = 1 * tramo.getDepositoDestino().getCostoEstadiaDiario();
                costoTotal += costoEstadia;
                log.debug("Costo de estadía estimada en depósito {}: 1 día * ${}/día = ${}",
                        tramo.getDepositoDestino().getNombre(),
                        tramo.getDepositoDestino().getCostoEstadiaDiario(), costoEstadia);
            }
        }

        log.info("Costo real calculado para tramo {}: ${}", tramo.getId(), costoTotal);
        return costoTotal;
    }

    /**
     * Calcula el tiempo real del tramo
     */
    private double calcularTiempoReal(Tramo tramo) {
        if (tramo.getFechaHoraInicioReal() == null) {
            return 0;
        }

        // Usar fecha fin real si está disponible, sino la hora actual
        LocalDateTime fin = tramo.getFechaHoraFinReal() != null ?
                           tramo.getFechaHoraFinReal() : LocalDateTime.now();

        return java.time.temporal.ChronoUnit.SECONDS.between(
                tramo.getFechaHoraInicioReal(),
                fin);
    }

    @Transactional
    public void asignarCamionConPlanificacion(Long idTramo, String dominio,
                                            LocalDateTime fechaInicioEstimada,
                                            LocalDateTime fechaFinEstimada) {
        Tramo tramo = tramoRepository.findById(idTramo)
                .orElseThrow(() -> new TramoNotFoundException(idTramo));

        // 1. Actualizar tramo en MS-LOGÍSTICA
        tramo.setDominioCamionRef(dominio);
        tramo.setEstado(EstadoTramo.ASIGNADO);
        tramo.setFechaHoraInicioEstimada(fechaInicioEstimada);
        tramo.setFechaHoraFinEstimada(fechaFinEstimada);

        tramoRepository.save(tramo);
        log.info("Camión {} asignado al tramo {}. Planificado para: {} - {}",
                dominio, idTramo, fechaInicioEstimada, fechaFinEstimada);

        // 2. Notificar a MS-FLOTA de la asignación
        try {
            notificarAsignacionAFlota(dominio, idTramo);
            log.info("MS-Flota notificado exitosamente de asignación: {} -> {}", dominio, idTramo);
        } catch (Exception e) {
            log.error("Error notificando asignación a MS-Flota para camión {} y tramo {}: {}",
                     dominio, idTramo, e.getMessage());
            // Relanzar excepción para hacer rollback de la transacción
            throw new RuntimeException("Error sincronizando con MS-Flota: " + e.getMessage(), e);
        }
    }

    public List<Tramo> obtenerTramosPendientes() {
        // Retorna tramos que están ASIGNADO, INICIADO o ESTIMADO (no FINALIZADO)
        return tramoRepository.findByEstadoNot(EstadoTramo.FINALIZADO);
    }

    public Optional<Tramo> obtenerTramo(Long id) {
        return tramoRepository.findById(id);
    }

    public List<Tramo> listarTramos() {
        return tramoRepository.findAll();
    }

    /**
     * Validar secuencia de tramos antes de iniciar
     */
    public void validarSecuenciaTramos(Long tramoId) {
        Tramo tramo = tramoRepository.findById(tramoId)
            .orElseThrow(() -> new TramoNotFoundException(tramoId));

        List<Tramo> tramosRuta = tramoRepository.findByRutaId(tramo.getRuta().getId());

        // Ordenar tramos por ID (asumiendo que IDs mayores = tramos posteriores)
        tramosRuta.sort((t1, t2) -> t1.getId().compareTo(t2.getId()));

        // Verificar que tramos anteriores estén finalizados
        boolean tramoEncontrado = false;
        for (Tramo t : tramosRuta) {
            if (t.getId().equals(tramoId)) {
                tramoEncontrado = true;
                break;
            }

            if (t.getEstado() != EstadoTramo.FINALIZADO) {
                throw new IllegalStateException(
                    "Debe finalizar el tramo " + t.getId() + " antes de iniciar el tramo " + tramoId);
            }
        }

        if (!tramoEncontrado) {
            throw new TramoNotFoundException(tramoId);
        }

        log.info("Secuencia de tramos validada correctamente para tramo {}", tramoId);
    }

    /**
     * Asignar múltiples tramos consecutivos
     */
    @Transactional
    public void asignarTramosConsecutivos(String dominio, List<Long> tramoIds,
                                         List<LocalDateTime> fechasInicio,
                                         List<LocalDateTime> fechasFin) {

        if (tramoIds.size() != fechasInicio.size() || tramoIds.size() != fechasFin.size()) {
            throw new IllegalArgumentException("Las listas de tramos y fechas deben tener el mismo tamaño");
        }

        // Validar que todas las fechas sean secuenciales y coherentes
        validarSecuenciaFechas(fechasInicio, fechasFin);

        // Asignar cada tramo individualmente
        for (int i = 0; i < tramoIds.size(); i++) {
            asignarCamionConPlanificacion(tramoIds.get(i), dominio,
                fechasInicio.get(i), fechasFin.get(i));
        }

        log.info("Camión {} asignado a {} tramos consecutivos", dominio, tramoIds.size());
    }

    /**
     * Reasignar tramo liberando camión anterior
     */
    @Transactional
    // CAMBIO: Ahora devuelve 'Tramo' en lugar de 'void'
    public Tramo reasignarTramo(Long tramoId, String nuevoDominio,
                                LocalDateTime nuevaFechaInicio,
                                LocalDateTime nuevaFechaFin) {

        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new TramoNotFoundException(tramoId));

        // Validación de estado (para no cambiar choferes en movimiento)
        if (tramo.getEstado() == EstadoTramo.EN_CURSO || tramo.getEstado() == EstadoTramo.FINALIZADO) {
            throw new IllegalStateException("No se puede reasignar un tramo que está " + tramo.getEstado());
        }

        String dominioAnterior = tramo.getDominioCamionRef();

        // Actualizar datos
        tramo.setDominioCamionRef(nuevoDominio);
        tramo.setEstado(EstadoTramo.ASIGNADO);
        tramo.setFechaHoraInicioEstimada(nuevaFechaInicio);
        tramo.setFechaHoraFinEstimada(nuevaFechaFin);

        // Guardar y capturar el objeto actualizado
        Tramo tramoActualizado = tramoRepository.save(tramo);

        log.info("Tramo {} reasignado del camión {} al camión {}", tramoId, dominioAnterior, nuevoDominio);

        // Notificar a MS-FLOTA (sin cambios aquí)
        try {
            notificarReasignacionAFlota(dominioAnterior, nuevoDominio, tramoId);
        } catch (Exception e) {
            log.error("Error notificando reasignación a MS-Flota: {}", e.getMessage());
            throw new RuntimeException("Error sincronizando con MS-Flota: " + e.getMessage(), e);
        }

        // CAMBIO: Retornar el objeto
        return tramoActualizado;
    }

    /**
     * Obtener tramos por ruta (para validaciones de secuencia)
     */
    public List<Tramo> obtenerTramosPorRuta(Long rutaId) {
        return tramoRepository.findByRutaId(rutaId);
    }

    // Métodos helper privados
    private void validarSecuenciaFechas(List<LocalDateTime> fechasInicio, List<LocalDateTime> fechasFin) {
        for (int i = 0; i < fechasInicio.size(); i++) {
            LocalDateTime inicio = fechasInicio.get(i);
            LocalDateTime fin = fechasFin.get(i);

            // Validar que inicio < fin para cada tramo
            if (inicio.isAfter(fin) || inicio.equals(fin)) {
                throw new IllegalArgumentException(
                    "Fecha de inicio debe ser anterior a fecha de fin para tramo " + (i + 1));
            }

            // Validar que el tramo actual inicie después del anterior termine
            if (i > 0) {
                LocalDateTime finAnterior = fechasFin.get(i - 1);
                if (inicio.isBefore(finAnterior)) {
                    throw new IllegalArgumentException(
                        "El tramo " + (i + 1) + " no puede iniciar antes de que termine el tramo " + i);
                }
            }
        }
    }

    /**
     * Notifica a MS-FLOTA sobre la asignación de un tramo a un camión
     */
    private void notificarAsignacionAFlota(String dominio, Long tramoId) {
        try {
            flotaApiClient.notificarAsignacionMultiple(dominio, List.of(tramoId));
        } catch (Exception e) {
            log.error("Error en comunicación con MS-Flota: {}", e.getMessage());
            throw e; // Propagar para rollback
        }
    }

    /**
     * Notifica a MS-FLOTA sobre reasignación de tramo
     */
    private void notificarReasignacionAFlota(String dominioAnterior, String dominioNuevo, Long tramoId) {
        try {
            flotaApiClient.notificarReasignacion(dominioAnterior, dominioNuevo, tramoId);
        } catch (Exception e) {
            log.error("Error en reasignación con MS-Flota: {}", e.getMessage());
            throw e; // Propagar para rollback
        }
    }
}

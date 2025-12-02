package utn.backend.tpi.tpi_flota_viajes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utn.backend.tpi.tpi_flota_viajes.clients.LogisticaApiClient;
import utn.backend.tpi.tpi_flota_viajes.clients.dto.TramoDTO;
import utn.backend.tpi.tpi_flota_viajes.dto.response.TramoResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.response.TramoPendienteResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.mapper.TramoMapper;
import utn.backend.tpi.tpi_flota_viajes.model.Camion;
import utn.backend.tpi.tpi_flota_viajes.model.EstadoCamion;
import utn.backend.tpi.tpi_flota_viajes.repository.CamionRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TramoExecutionService {

    private final LogisticaApiClient logisticaApiClient;
    private final CamionService camionService;
    private final TramoMapper tramoMapper;
    private final CamionRepository camionRepository;


    /**
     * Inicia un tramo buscando automáticamente el camión asignado
     *
     * @param idTramo ID del tramo a iniciar
     */
    @Transactional
    public TramoResponse iniciarTramo(Long idTramo) {
        log.info("Iniciando tramo {}", idTramo);

        // 1. Obtener información del tramo para saber qué camión está asignado
        TramoDTO tramoInfo = logisticaApiClient.obtenerTramo(idTramo);

        if (tramoInfo.getDominioCamionRef() == null || tramoInfo.getDominioCamionRef().isEmpty()) {
            throw new RuntimeException("El tramo no tiene un camión asignado");
        }

        String dominioCamion = tramoInfo.getDominioCamionRef();
        log.info("Tramo {} asignado al camión {}", idTramo, dominioCamion);

        // 2. Cambiar estado del camión: ASIGNADO → EN_VIAJE
        camionService.ocuparCamion(dominioCamion, idTramo);

        // 3. Notificar a ms-logistica que el tramo está iniciando
        TramoDTO tramoLogistica = logisticaApiClient.iniciarTramo(idTramo);
        log.info("Tramo {} iniciado exitosamente en ms-logistica", idTramo);

        return tramoMapper.toResponse(tramoLogistica, dominioCamion);
    }

    /**
     * Finaliza un tramo buscando automáticamente el camión asignado
     *
     * @param idTramo      ID del tramo a finalizar
     * @param kmRecorridos Kilómetros recorridos durante el tramo
     */
    @Transactional
    public TramoResponse finalizarTramo(Long idTramo, double kmRecorridos) {
        log.info("Finalizando tramo {}. KMs recorridos: {}", idTramo, kmRecorridos);

        // 1. Obtener información del tramo para saber qué camión está asignado
        TramoDTO tramoInfo = logisticaApiClient.obtenerTramo(idTramo);

        if (tramoInfo.getDominioCamionRef() == null || tramoInfo.getDominioCamionRef().isEmpty()) {
            throw new RuntimeException("El tramo no tiene un camión asignado");
        }

        String dominioCamion = tramoInfo.getDominioCamionRef();
        log.info("Finalizando tramo {} del camión {}", idTramo, dominioCamion);

        // 2. Notificar a ms-logistica que el tramo está finalizando
        TramoDTO tramoLogistica = logisticaApiClient.finalizarTramo(idTramo, kmRecorridos);
        log.info("Tramo {} finalizado exitosamente en ms-logistica", idTramo);

        // 3. Si exitoso en ms-logistica, liberar el camión localmente: EN_VIAJE → DISPONIBLE
        camionService.liberarCamion(dominioCamion, idTramo, kmRecorridos);

        return tramoMapper.toResponse(tramoLogistica, dominioCamion);
    }


    //este metodo esta raro osea le actualiza el estado al camion y lo pasa EN VIAJE
    // se supone que deberia haber estado antes en ASIGNADO, osea que fue asignado por el operador
    //y ademas hay algo raro que es porque le avisa al ms-logistica que el camion esta en viaje?
    @Transactional
    public void asignarTramo(Long idTramo, String dominioCamion) {
        log.info("Asignando tramo {} a camión {}", idTramo, dominioCamion);

        // 1. Asignar localmente (valida estado y actualiza a EN_VIAJE - wait, should it
        // be EN_VIAJE or ASIGNADO?)
        // Requisito: "Asignar camión a tramo" vs "Iniciar viaje".
        // Si asignamos, el camión queda comprometido.
        // CamionService.asignarTramo pone el estado en EN_VIAJE.
        // Tal vez deberíamos tener un estado ASIGNADO en Camion también?
        // Por ahora usaremos la lógica existente de asignarTramo que lo pone en
        // EN_VIAJE (o asumimos que asignar = iniciar para este flujo simple,
        // pero el usuario pidió endpoint separado).
        // Si el usuario pidió endpoint separado, probablemente quiera reservar el
        // camión.
        // Voy a usar camionService.asignarTramo pero idealmente debería ser un estado
        // intermedio.
        // Usar el nuevo método que asigna (DISPONIBLE → ASIGNADO) sin iniciar
        camionService.asignarCamionATramo(dominioCamion, idTramo);

        // 2. Notificar a ms-logistica
        try {
            logisticaApiClient.asignarCamion(idTramo, dominioCamion);
        } catch (Exception e) {
            log.error("Error al llamar a ms-logistica para asignar camión al tramo {}: {}", idTramo, e.getMessage(), e);
            // Esto es lo que causa el 500 genérico
            throw new RuntimeException("Error en servicio de logística al asignar camión: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los tramos asignados de un transportista
     * Solo tramos en estado ASIGNADO (pendientes de iniciar)
     */
    public List<TramoPendienteResponse> obtenerTramosAsignadosDelTransportista(Long transportistaId) {
        log.info("Buscando tramos asignados para transportista: {}", transportistaId);

        // 1. Buscar camiones del transportista con tramos asignados o programados
        List<Camion> camionesConTramos = camionRepository.findByTransportistaId(transportistaId).stream()
                .filter(c -> c.getEstado() == EstadoCamion.ASIGNADO ||
                           c.getEstado() == EstadoCamion.PROGRAMADO ||
                           c.getEstado() == EstadoCamion.EN_VIAJE)
                .filter(c -> c.tieneTramosAsignados())
                .toList();

        if (camionesConTramos.isEmpty()) {
            log.info("No hay camiones con tramos asignados para transportista: {}", transportistaId);
            return List.of();
        }

        List<TramoPendienteResponse> tramos = new ArrayList<>();

        for (Camion camion : camionesConTramos) {
            // Procesar tramos programados
            for (Long tramoId : camion.getTramosProgramados()) {
                try {
                    // 2. Consultar detalles del tramo en ms-logistica
                    TramoDTO tramoInfo = logisticaApiClient.obtenerTramo(tramoId);

                    // 3. Mapear a DTO para respuesta (tramos programados)
                    boolean esProximoTramo = camion.getProximoTramoAEjecutar() != null &&
                                           camion.getProximoTramoAEjecutar().equals(tramoId);

                    TramoPendienteResponse tramo = TramoPendienteResponse.builder()
                            .idTramo(tramoInfo.getId())
                            .dominioCamion(camion.getDominio())
                            .estadoTramo(tramoInfo.getEstado())
                            .fechaAsignacion(tramoInfo.getFechaHoraInicioEstimada()) // Usar fecha estimada
                            .tipo(tramoInfo.getTipo())
                            .kmEstimados(tramoInfo.getKmEstimados())
                            .puedeIniciar(esProximoTramo) // Solo puede iniciar el próximo en secuencia
                            .build();
                    tramos.add(tramo);

                } catch (Exception e) {
                    log.error("Error obteniendo detalles del tramo {} para camión {}: {}",
                            tramoId, camion.getDominio(), e.getMessage());
                    // Continuar con otros tramos
                }
            }

            // También agregar tramo en ejecución si existe
            if (camion.getTramoEnEjecucion() != null) {
                try {
                    TramoDTO tramoEnEjecucion = logisticaApiClient.obtenerTramo(camion.getTramoEnEjecucion());

                    TramoPendienteResponse tramo = TramoPendienteResponse.builder()
                            .idTramo(tramoEnEjecucion.getId())
                            .dominioCamion(camion.getDominio())
                            .estadoTramo("EN_CURSO") // Estado actual
                            .fechaAsignacion(tramoEnEjecucion.getFechaHoraInicioReal()) // Fecha real de inicio
                            .tipo(tramoEnEjecucion.getTipo())
                            .kmEstimados(tramoEnEjecucion.getKmEstimados())
                            .puedeIniciar(false) // Ya está iniciado
                            .build();
                    tramos.add(0, tramo); // Agregar al inicio (más importante)

                } catch (Exception e) {
                    log.error("Error obteniendo detalles del tramo en ejecución {} para camión {}: {}",
                            camion.getTramoEnEjecucion(), camion.getDominio(), e.getMessage());
                }
            }
        }

        log.info("Encontrados {} tramos asignados para transportista: {}", tramos.size(), transportistaId);
        return tramos;
    }
}

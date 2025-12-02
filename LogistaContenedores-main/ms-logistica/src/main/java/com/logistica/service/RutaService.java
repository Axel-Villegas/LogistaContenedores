package com.logistica.service;

import com.logistica.dto.response.DistanciaResponse;
import com.logistica.model.*;
import com.logistica.repository.RutaRepository;
import com.logistica.repository.TramoRepository;
import com.logistica.repository.DepositoRepository;
import com.logistica.repository.TarifaRepository;
import com.logistica.client.OsrmClient2;
import com.logistica.client.OsrmDistanceResponse;
import com.logistica.dto.response.RutaPlanningResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class RutaService {

    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final DepositoRepository depositoRepository;
    private final TarifaRepository tarifaRepository;
    private final OsrmClient2 osrmClient;

    /**
     * Planifica una ruta calculando tramos, distancias y costos estimados
     * Devuelve la Ruta con todos sus Tramos y los totales calculados
     */
    @Transactional
    public RutaPlanningResponse planificarRuta(String nroSolicitud,
            List<Deposito> depositosIntermedios,
            Double latOrigen, Double lonOrigen,
            Double latDestino, Double lonDestino,
            Tarifa tarifa) {

        log.info("Planificando ruta para solicitud: {}", nroSolicitud);

        // Check if exists
        Ruta existing = rutaRepository.findByNroSolicitudRef(nroSolicitud);
        if (existing != null) {
            log.info("Ruta ya existe para solicitud: {}. Retornando existente.", nroSolicitud);

            // Calculate totals from existing tramos
            double costoTotal = existing.getTramos().stream().mapToDouble(Tramo::getCostoEstimado).sum();
            double tiempoTotal = existing.getTramos().stream().mapToDouble(Tramo::getTiempoEstimado).sum();

            return new RutaPlanningResponse(existing, costoTotal, tiempoTotal);
        }

        // Crear Ruta
        Ruta ruta = new Ruta();
        ruta.setNroSolicitudRef(nroSolicitud);
        ruta.setTramos(new ArrayList<>());

        // Crear tramos
        double distanciaTotal = 0;
        double tiempoTotal = 0;
        double costoTotal = 0;
        int cantidadTramos = 0;

        Double latActual = latOrigen;
        Double lonActual = lonOrigen;

        // Tramos hacia depósitos intermedios
        Deposito depositoAnterior = null;

        for (Deposito deposito : depositosIntermedios) {
            OsrmDistanceResponse response = osrmClient.calcularDistancia(
                    latActual, lonActual, deposito.getLatitud(), deposito.getLongitud());

            Tramo tramo = new Tramo();
            tramo.setRuta(ruta);
            tramo.setDepositoOrigen(depositoAnterior);
            tramo.setDepositoDestino(deposito);
            tramo.setTarifa(tarifa);
            tramo.setTipo(cantidadTramos == 0 ? "Origen-Deposito" : "Deposito-Deposito");
            tramo.setEstado(EstadoTramo.ESTIMADO);
            tramo.setKmEstimados(response.getDistanceKm());
            tramo.setTiempoEstimado(response.getDurationSeconds());
            tramo.setCostoEstimado(response.getDistanceKm() * tarifa.getValorKMBase());

            ruta.getTramos().add(tramo);

            distanciaTotal += response.getDistanceKm();
            tiempoTotal += response.getDurationSeconds();
            costoTotal += tramo.getCostoEstimado();
            cantidadTramos++;

            latActual = deposito.getLatitud();
            lonActual = deposito.getLongitud();
            depositoAnterior = deposito;
        }

        // Tramo final hacia destino
        OsrmDistanceResponse responseFinal = osrmClient.calcularDistancia(
                latActual, lonActual, latDestino, lonDestino);

        Tramo tramoFinal = new Tramo();
        tramoFinal.setRuta(ruta);
        tramoFinal.setDepositoOrigen(depositoAnterior);
        tramoFinal.setDepositoDestino(null);
        tramoFinal.setTarifa(tarifa);
        tramoFinal.setTipo(depositosIntermedios.isEmpty() ? "Origen-Destino" : "Deposito-Destino");
        tramoFinal.setEstado(EstadoTramo.ESTIMADO);
        tramoFinal.setKmEstimados(responseFinal.getDistanceKm());
        tramoFinal.setTiempoEstimado(responseFinal.getDurationSeconds());
        tramoFinal.setCostoEstimado(responseFinal.getDistanceKm() * tarifa.getValorKMBase());

        ruta.getTramos().add(tramoFinal);

        distanciaTotal += responseFinal.getDistanceKm();
        tiempoTotal += responseFinal.getDurationSeconds();
        costoTotal += tramoFinal.getCostoEstimado();
        cantidadTramos++;

        // Guardar totales en Ruta (solo cantidad y distancia)
        ruta.setCantidadTramos(cantidadTramos);
        ruta.setDistanciaTotal(distanciaTotal);

        Ruta rutaGuardada = rutaRepository.save(ruta);
        log.info("Ruta planificada: {} tramos, {} km, ${} estimado",
                cantidadTramos, distanciaTotal, costoTotal);

        // ✅ Devolver Response con costos para MS Solicitudes
        return new RutaPlanningResponse(rutaGuardada, costoTotal, tiempoTotal);
    }

    public Ruta obtenerRuta(Long id) {
        return rutaRepository.findById(id)
                .orElseThrow(() -> new com.logistica.exception.RutaNotFoundException(id));
    }

    public Ruta obtenerRutaPorSolicitud(String nroSolicitud) {
        return rutaRepository.findByNroSolicitudRef(nroSolicitud);
    }

    /**
     * Calcula distancia, tiempo y costo entre dos puntos con depósitos intermedios
     */
    public DistanciaResponse calcularDistancia(Double latOrigen, Double lonOrigen,
            Double latDestino, Double lonDestino,
            List<Deposito> depositosIntermedios) {

        log.info("Calculando distancia con {} depósitos intermedios",
                depositosIntermedios != null ? depositosIntermedios.size() : 0);

        double distanciaTotal = 0;
        double tiempoTotal = 0;

        Double latActual = latOrigen;
        Double lonActual = lonOrigen;

        // Calcular tramos intermedios
        if (depositosIntermedios != null && !depositosIntermedios.isEmpty()) {
            for (Deposito deposito : depositosIntermedios) {
                OsrmDistanceResponse response = osrmClient.calcularDistancia(
                        latActual, lonActual, deposito.getLatitud(), deposito.getLongitud());
                distanciaTotal += response.getDistanceKm();
                tiempoTotal += response.getDurationSeconds();

                latActual = deposito.getLatitud();
                lonActual = deposito.getLongitud();
            }
        }

        // Calcular tramo final
        OsrmDistanceResponse responseFinal = osrmClient.calcularDistancia(
                latActual, lonActual, latDestino, lonDestino);

        distanciaTotal += responseFinal.getDistanceKm();
        tiempoTotal += responseFinal.getDurationSeconds();

        return new DistanciaResponse(distanciaTotal, tiempoTotal);
    }
}
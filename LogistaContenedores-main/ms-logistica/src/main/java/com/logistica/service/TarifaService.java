package com.logistica.service;

import com.logistica.model.Tarifa;
import com.logistica.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    public Tarifa crearTarifa(Tarifa tarifa) {
        log.info("Creando tarifa vigente desde: {}", tarifa.getFechaVigencia());

        // Check if exists
        List<Tarifa> existing = tarifaRepository.findByFechaVigencia(tarifa.getFechaVigencia());
        if (!existing.isEmpty()) {
            log.info("Tarifa ya existe para fecha: {}. Retornando existente.", tarifa.getFechaVigencia());
            return existing.get(0);
        }

        return tarifaRepository.save(tarifa);
    }

    public List<Tarifa> listarTarifas() {
        return tarifaRepository.findAll();
    }

    public Tarifa obtenerTarifa(Long id) {
        return tarifaRepository.findById(id)
                .orElseThrow(() -> new com.logistica.exception.NotFoundException("Tarifa no encontrada: " + id));
    }

    public Tarifa actualizarTarifa(Long id, Tarifa tarifaActualizada) {
        Tarifa tarifa = obtenerTarifa(id);

        tarifa.setValorKMBase(tarifaActualizada.getValorKMBase());
        tarifa.setCostoLitroCombustible(tarifaActualizada.getCostoLitroCombustible());
        tarifa.setFechaVigencia(tarifaActualizada.getFechaVigencia());
        tarifa.setPorcentajeRecargo(tarifaActualizada.getPorcentajeRecargo());

        return tarifaRepository.save(tarifa);
    }
}
package com.logistica.dto.mapper;

import com.logistica.dto.response.RutaResponse;
import com.logistica.dto.response.TramoResponse;
import com.logistica.model.Ruta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RutaMapper {

    private final TramoMapper tramoMapper;

    public RutaResponse toResponse(Ruta entity) {
        if (entity == null) return null;

        // Convertir la lista de entidades Tramo a DTOs TramoResponse
        List<TramoResponse> tramosDto = entity.getTramos().stream()
                .map(tramoMapper::toResponse)
                .toList();

        return RutaResponse.builder()
                .id(entity.getId())
                .nroSolicitudRef(entity.getNroSolicitudRef())
                .cantidadTramos(entity.getCantidadTramos())
                .distanciaTotal(entity.getDistanciaTotal())
                .tramos(tramosDto)
                .build();
    }
}
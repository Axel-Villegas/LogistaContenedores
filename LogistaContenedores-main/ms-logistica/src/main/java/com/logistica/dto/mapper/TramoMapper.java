package com.logistica.dto.mapper;

import com.logistica.dto.response.TramoResponse;
import com.logistica.model.Tramo;
import org.springframework.stereotype.Component;


@Component
public class TramoMapper {

    public TramoResponse toResponse(Tramo entity) {
        if (entity == null) {
            return null;
        }

        return TramoResponse.builder()
                .id(entity.getId())
                .estado(entity.getEstado().name())
                .tipo(entity.getTipo())

                // Nombres legibles para el usuario
                .origen(entity.getDepositoOrigen() != null ?
                        entity.getDepositoOrigen().getNombre() : "Origen Solicitud")
                .destino(entity.getDepositoDestino() != null ?
                        entity.getDepositoDestino().getNombre() : "Destino Final")

                .dominioCamionRef(entity.getDominioCamionRef())

                // Datos numéricos
                .kmEstimados(entity.getKmEstimados())
                .kmRecorridos(entity.getKmRecorridos())
                .costoEstimado(entity.getCostoEstimado())
                .costoReal(entity.getCostoReal())
                .tiempoEstimado(entity.getTiempoEstimado())
                .tiempoReal(entity.getTiempoReal())

                // Todas las fechas para trazabilidad completa
                .fechaHoraInicioEstimada(entity.getFechaHoraInicioEstimada())
                .fechaHoraFinEstimada(entity.getFechaHoraFinEstimada())
                .fechaHoraInicioReal(entity.getFechaHoraInicioReal())
                .fechaHoraFinReal(entity.getFechaHoraFinReal())

                .build();
    }
}
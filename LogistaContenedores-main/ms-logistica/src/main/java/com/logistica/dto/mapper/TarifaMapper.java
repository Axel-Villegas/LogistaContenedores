package com.logistica.dto.mapper;

import com.logistica.dto.request.CrearTarifaRequest;
import com.logistica.dto.response.TarifaResponse;
import com.logistica.model.Tarifa;
import org.springframework.stereotype.Component;

@Component
public class TarifaMapper {

    public Tarifa toEntity(CrearTarifaRequest request) {
        Tarifa tarifa = new Tarifa();
        tarifa.setValorKMBase(request.getValorKMBase());
        tarifa.setCostoLitroCombustible(request.getCostoLitroCombustible());
        tarifa.setPorcentajeRecargo(request.getPorcentajeRecargo());
        tarifa.setFechaVigencia(request.getFechaVigencia());
        return tarifa;
    }

    public TarifaResponse toResponse(Tarifa entity) {
        return TarifaResponse.builder()
                .id(entity.getId())
                .valorKMBase(entity.getValorKMBase())
                .costoLitroCombustible(entity.getCostoLitroCombustible())
                .porcentajeRecargo(entity.getPorcentajeRecargo())
                .fechaVigencia(entity.getFechaVigencia())
                .build();
    }

    // Método helper por si necesitas actualizar una entidad existente desde el request
    public void updateEntity(Tarifa entity, CrearTarifaRequest request) {
        entity.setValorKMBase(request.getValorKMBase());
        entity.setCostoLitroCombustible(request.getCostoLitroCombustible());
        entity.setPorcentajeRecargo(request.getPorcentajeRecargo());
        entity.setFechaVigencia(request.getFechaVigencia());
    }
}
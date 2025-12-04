package com.logistica.dto.mapper;

import com.logistica.dto.request.CrearDepositoRequest;
import com.logistica.dto.response.DepositoResponse;
import com.logistica.model.Deposito;
import org.springframework.stereotype.Component;

@Component
public class DepositoMapper {

    public Deposito toEntity(CrearDepositoRequest request) {
        Deposito deposito = new Deposito();
        deposito.setNombre(request.getNombre());
        deposito.setDireccion(request.getDireccion());
        deposito.setLatitud(request.getLatitud());
        deposito.setLongitud(request.getLongitud());
        deposito.setCostoEstadiaDiario(request.getCostoEstadiaDiario());
        return deposito;
    }

    public DepositoResponse toResponse(Deposito entity) {
        if (entity == null) return null;

        return DepositoResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .direccion(entity.getDireccion())
                .latitud(entity.getLatitud())
                .longitud(entity.getLongitud())
                .costoEstadiaDiario(entity.getCostoEstadiaDiario())
                .build();
    }
}
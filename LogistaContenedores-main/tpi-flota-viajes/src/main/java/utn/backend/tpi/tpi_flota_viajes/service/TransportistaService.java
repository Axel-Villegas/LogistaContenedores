package utn.backend.tpi.tpi_flota_viajes.service;

import org.springframework.dao.DataIntegrityViolationException;
import utn.backend.tpi.tpi_flota_viajes.dto.mapper.CamionMapper;
import utn.backend.tpi.tpi_flota_viajes.dto.request.CrearTransportistaRequest;
import utn.backend.tpi.tpi_flota_viajes.dto.response.CamionResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.response.TransportistaResponse;
import utn.backend.tpi.tpi_flota_viajes.dto.mapper.TransportistaMapper;
import utn.backend.tpi.tpi_flota_viajes.model.Camion;
import utn.backend.tpi.tpi_flota_viajes.model.Transportista;
import utn.backend.tpi.tpi_flota_viajes.repository.TransportistaRepository;
import utn.backend.tpi.tpi_flota_viajes.exception.ConflictException;
import utn.backend.tpi.tpi_flota_viajes.exception.NotFoundException;
import utn.backend.tpi.tpi_flota_viajes.repository.CamionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransportistaService {
    private final TransportistaRepository transportistaRepository;
    private final CamionRepository camionRepository;

    /**
     * Crear un nuevo transportista
     * Validar que la licencia sea única
     */
    @Transactional
    public TransportistaResponse crear(CrearTransportistaRequest dto) {
        log.info("Creando transportista con licencia: {}", dto.getLicencia());

        // Check if exists
        List<Transportista> existing = transportistaRepository.findByLicencia(dto.getLicencia().toUpperCase());
        if (!existing.isEmpty()) {
            log.info("Transportista ya existe con licencia: {}. Retornando existente.", dto.getLicencia());
            return TransportistaMapper.toResponse(existing.get(0));
        }

        try {
            Transportista transportista = Transportista.builder()
                    .nombre(dto.getNombre())
                    .licencia(dto.getLicencia().toUpperCase()) // Normalizar
                    .contacto(dto.getContacto())
                    .activo(true) // Nuevo transportista siempre activo
                    .build();

            Transportista guardado = transportistaRepository.save(transportista);
            log.info("Transportista creado exitosamente con ID: {}", guardado.getId());
            return TransportistaMapper.toResponse(guardado);

        } catch (DataIntegrityViolationException e) {
            log.error("Error de integridad al crear transportista", e);
            throw new ConflictException("La licencia " + dto.getLicencia() + " ya está registrada");
        }
    }

    /**
     * Obtener transportista por ID
     */
    public TransportistaResponse obtenerPorId(Long id) {
        log.debug("Buscando transportista con ID: {}", id);

        Transportista transportista = transportistaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Transportista no encontrado con ID: {}", id);
                    return new NotFoundException("Transportista con ID " + id + " no encontrado");
                });

        return TransportistaMapper.toResponse(transportista);
    }

    /**
     * Eliminar (desactivar) un transportista
     * Validar que no tenga camiones asignados
     * Implementa validación doble: en memoria + en BD
     */
    @Transactional
    public void eliminar(Long transportistaId) {
        log.info("Eliminando transportista: {}", transportistaId);

        // PASO 1: Obtener transportista
        Transportista transportista = transportistaRepository.findById(transportistaId)
                .orElseThrow(() -> {
                    log.warn("Transportista no encontrado: {}", transportistaId);
                    return new NotFoundException("Transportista con ID " + transportistaId + " no encontrado");
                });

        // VALIDACIÓN 1: Verificar lista en memoria
        if (transportista.getCamiones() != null && !transportista.getCamiones().isEmpty()) {
            log.warn("Intento de eliminar transportista con camiones activos: {}", transportistaId);
            throw new ConflictException(
                    "No se puede desactivar. El transportista tiene " +
                            transportista.getCamiones().size() +
                            " camiones asignados. Reasigne los camiones primero.");
        }

        // VALIDACIÓN 2: Verificar en BD (garantía adicional contra inconsistencias)
        long countCamiones = camionRepository.countByTransportistaId(transportistaId);
        if (countCamiones > 0) {
            log.error(
                    "ALERTA CRÍTICA: Inconsistencia detectada. Transportista {} tiene {} camiones en BD pero lista vacía en memoria",
                    transportistaId, countCamiones);
            throw new ConflictException(
                    "Error de integridad en BD: Se detectó inconsistencia. Contacte al administrador.");
        }

        // PASO 3: Soft delete (marcar como inactivo, no eliminar)
        transportista.setActivo(false);
        transportistaRepository.save(transportista);

        log.info("Transportista desactivado correctamente: {}", transportistaId);
    }

    /**
     * Obtener todos los camiones de un transportista
     */
    public List<CamionResponse> obtenerCamionesPorTransportista(Long transportistaId) {
        log.debug("Buscando camiones del transportista: {}", transportistaId);

        // Nota: findByIdWithCamionesActivo() solo retorna transportistas ACTIVOS
        // Si está inactivo, lanzará NotFoundException (404)
        Transportista transportista = transportistaRepository.findByIdWithCamionesActivo(transportistaId)
                .orElseThrow(() -> {
                    log.warn("Transportista no encontrado: {}", transportistaId);
                    return new NotFoundException("Transportista con ID " + transportistaId + " no encontrado");
                });

        // Obtener camiones
        List<Camion> camiones = transportista.getCamiones() != null ? transportista.getCamiones() : new ArrayList<>();

        log.info("Se encontraron {} camiones para transportista: {}", camiones.size(), transportistaId);

        return camiones.stream()
                .map(CamionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los transportistas activos
     */
    public List<TransportistaResponse> obtenerTodos() {
        log.debug("Obteniendo todos los transportistas activos");

        List<Transportista> transportistas = transportistaRepository.findAllActivos();

        log.info("Se encontraron {} transportistas activos", transportistas.size());

        return transportistas.stream()
                .map(TransportistaMapper::toResponse)
                .toList();
    }

}

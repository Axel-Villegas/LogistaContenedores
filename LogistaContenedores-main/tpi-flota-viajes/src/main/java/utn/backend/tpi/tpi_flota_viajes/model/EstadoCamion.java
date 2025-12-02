package utn.backend.tpi.tpi_flota_viajes.model;
public enum EstadoCamion {
    DISPONIBLE,      // Listo para trabajar, sin tramos asignados
    ASIGNADO,        // Asignado a un tramo inmediato (próximo a ejecutar)
    PROGRAMADO,      // Con tramos futuros asignados pero no inmediatos
    EN_VIAJE,        // Está ejecutando un tramo actualmente
    MANTENIMIENTO    // No disponible por mantenimiento
}

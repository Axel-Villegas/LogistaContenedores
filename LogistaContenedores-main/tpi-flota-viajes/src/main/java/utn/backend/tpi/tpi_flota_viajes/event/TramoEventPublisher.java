package utn.backend.tpi.tpi_flota_viajes.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Publisher de eventos para RabbitMQ.
 * Solo se crea si RabbitTemplate está disponible (RabbitMQ configurado).
 */
@Component
@ConditionalOnBean(RabbitTemplate.class)
@RequiredArgsConstructor
@Slf4j
public class TramoEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    // Nombre del exchange (tema principal)
    private static final String EXCHANGE_NAME = "flota-viajes-events";

    /**
     * Publicar evento: Tramo INICIADO
     * Este evento notifica a otros MSs que un tramo comenzó
     * Lo reciben: MS Solicitudes (para actualizar tracking), MS Logística (para actualizar estado)
     */
    public void publicarTramoIniciado(TramoIniciadoEvent evento) {
        log.info("Publicando evento 'tramo.iniciado' para tramo: {}", evento.getIdTramo());

        try {
            rabbitTemplate.convertAndSend(
                    EXCHANGE_NAME,
                    "tramo.iniciado",
                    evento
            );
            log.debug("Evento 'tramo.iniciado' enviado exitosamente a RabbitMQ");
        } catch (Exception e) {
            log.error("Error al publicar evento 'tramo.iniciado': {}", e.getMessage(), e);
            // Nota: En producción, podrías implementar reintentOS o guardar en BD para reintentOS posteriores
        }
    }

    /**
     * Publicar evento: Tramo FINALIZADO
     * Este evento notifica a otros MSs que un tramo terminó
     * Lo reciben: MS Solicitudes (para actualizar tracking), MS Logística (para calcular costos finales)
     */
    public void publicarTramoFinalizado(TramoFinalizadoEvent evento) {
        log.info("Publicando evento 'tramo.finalizado' para tramo: {}", evento.getIdTramo());

        try {
            rabbitTemplate.convertAndSend(
                    EXCHANGE_NAME,
                    "tramo.finalizado",
                    evento
            );
            log.debug("Evento 'tramo.finalizado' enviado exitosamente a RabbitMQ");
        } catch (Exception e) {
            log.error("Error al publicar evento 'tramo.finalizado': {}", e.getMessage(), e);
        }
    }
}

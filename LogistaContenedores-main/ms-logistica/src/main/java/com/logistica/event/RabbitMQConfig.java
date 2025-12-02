package com.logistica.event;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    @Bean
    public TopicExchange solicitudesExchange() {
        return new TopicExchange("solicitudes.exchange", true, false);
    }

    // Queues
    @Bean
    public Queue tramoIniciado() {
        return new Queue("tramos.iniciado.queue", true);
    }

    @Bean
    public Queue tramoFinalizado() {
        return new Queue("tramos.finalizado.queue", true);
    }

    // Bindings
    @Bean
    public Binding bindingTramoIniciado(Queue tramoIniciado, TopicExchange solicitudesExchange) {
        return BindingBuilder.bind(tramoIniciado)
                .to(solicitudesExchange)
                .with("tramo.iniciado");
    }

    @Bean
    public Binding bindingTramoFinalizado(Queue tramoFinalizado, TopicExchange solicitudesExchange) {
        return BindingBuilder.bind(tramoFinalizado)
                .to(solicitudesExchange)
                .with("tramo.finalizado");
    }
}
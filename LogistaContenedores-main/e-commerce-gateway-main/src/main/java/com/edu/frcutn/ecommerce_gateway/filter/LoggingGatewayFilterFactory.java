package com.edu.frcutn.ecommerce_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class LoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoggingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            long startTime = System.currentTimeMillis();
            String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);
            ServerHttpRequest request = exchange.getRequest();
            
            log.info("┌─ Incoming Request [{}] ────────────────────────────────────────", requestId);
            log.info("│ Time: {}", LocalDateTime.now().format(DATE_FORMATTER));
            log.info("│ Method: {}", request.getMethod());
            log.info("│ URI: {}", request.getURI());
            log.info("│ Remote Address: {}", request.getRemoteAddress());
            log.info("│ User-Agent: {}", request.getHeaders().getFirst("User-Agent"));
            log.info("│ Content-Type: {}", request.getHeaders().getFirst("Content-Type"));
            
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                long duration = System.currentTimeMillis() - startTime;
                log.info("│ Status: {}", response.getStatusCode());
                log.info("│ Duration: {} ms", duration);
                log.info("└─ Request [{}] completed ────────────────────────────────────────", requestId);
            }));
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}


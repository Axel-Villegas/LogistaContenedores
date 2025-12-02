package com.transporte.ms_solicitudes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                // Permitir acceso anónimo a Swagger UI [cite: 115]
                                                .requestMatchers("/swagger-ui.html", "/v3/api-docs/**",
                                                                "/swagger-ui/**")
                                                .permitAll()

                                                // Requerimiento: GET /solicitudes/pendientes es para 'Operador' [cite:
                                                // 48, 49]
                                                .requestMatchers(HttpMethod.GET, "/api/v1/solicitudes/pendientes")
                                                .hasRole("OPERADOR")

                                                // Requerimiento: PUT /solicitudes/{nro}/aceptar es para 'Operador'
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/solicitudes/*/aceptar")
                                                .hasRole("OPERADOR")

                                                // Requerimiento: POST /solicitudes es para 'Cliente' [cite: 40]
                                                .requestMatchers(HttpMethod.POST, "/api/v1/solicitudes")
                                                .hasRole("CLIENTE")

                                                // Requerimiento: GET /solicitudes/{nro} es para 'Cliente' [cite: 30]
                                                .requestMatchers(HttpMethod.GET, "/api/v1/solicitudes/*")
                                                .hasRole("CLIENTE")

                                                // Denegar todo lo demás por defecto
                                                .anyRequest().authenticated())
                                // Usar validación de token JWT (Resource Server)
                                .oauth2ResourceServer(
                                                oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter())));

                return http.build();
        }

        @Bean
        public org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthenticationConverter() {
                org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter converter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(
                                new org.springframework.core.convert.converter.Converter<org.springframework.security.oauth2.jwt.Jwt, java.util.Collection<org.springframework.security.core.GrantedAuthority>>() {
                                        @Override
                                        public java.util.Collection<org.springframework.security.core.GrantedAuthority> convert(
                                                        org.springframework.security.oauth2.jwt.Jwt jwt) {
                                                java.util.Map<String, Object> realmAccess = (java.util.Map<String, Object>) jwt
                                                                .getClaims()
                                                                .get("realm_access");
                                                if (realmAccess == null || realmAccess.isEmpty()) {
                                                        return new java.util.ArrayList<>();
                                                }
                                                java.util.Collection<String> roles = (java.util.Collection<String>) realmAccess
                                                                .get("roles");
                                                return roles.stream()
                                                                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                                                "ROLE_" + role.toUpperCase()))
                                                                .collect(java.util.stream.Collectors.toList());
                                        }
                                });
                return converter;
        }
}
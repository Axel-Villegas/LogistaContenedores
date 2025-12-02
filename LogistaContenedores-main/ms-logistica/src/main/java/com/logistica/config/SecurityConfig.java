package com.logistica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs sin estado
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoints internos para otros microservicios
                        .requestMatchers("/api/v1/tramos/**").authenticated()
                        // Endpoints de gestión para el OPERADOR
                        .requestMatchers("/api/v1/depositos/**").hasRole("OPERADOR")
                        .requestMatchers("/api/v1/tarifas/**").hasRole("OPERADOR")
                        .requestMatchers("/api/v1/rutas/**").hasRole("OPERADOR")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
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
                        java.util.Map<String, Object> realmAccess = (java.util.Map<String, Object>) jwt.getClaims()
                                .get("realm_access");
                        if (realmAccess == null || realmAccess.isEmpty()) {
                            return new java.util.ArrayList<>();
                        }
                        java.util.Collection<String> roles = (java.util.Collection<String>) realmAccess.get("roles");
                        return roles.stream()
                                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                        "ROLE_" + role))
                                .collect(java.util.stream.Collectors.toList());
                    }
                });
        return converter;
    }
}
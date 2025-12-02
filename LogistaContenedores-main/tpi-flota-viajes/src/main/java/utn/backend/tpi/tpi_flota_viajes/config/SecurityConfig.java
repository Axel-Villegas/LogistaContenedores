package utn.backend.tpi.tpi_flota_viajes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                // Permitir acceso a Swagger UI
                                                .requestMatchers("/swagger-ui.html", "/v3/api-docs/**",
                                                                "/swagger-ui/**")
                                                .permitAll()

                                                // Endpoints de FLOTA (Camiones, Transportistas) para ROL OPERADOR
                                                .requestMatchers(HttpMethod.POST, "/api/flota/camiones")
                                                .hasRole("OPERADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/flota/camiones")
                                                .hasRole("OPERADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/flota/camiones/{id}")
                                                .hasRole("OPERADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/flota/camiones/dominio/**")
                                                .authenticated() // Permitir acceso interno desde otros microservicios
                                                .requestMatchers(HttpMethod.POST, "/api/flota/camiones/disponibles")
                                                .hasRole("OPERADOR")
                                                .requestMatchers(HttpMethod.POST, "/api/flota/transportistas")
                                                .hasRole("OPERADOR")
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/flota/transportistas/{id}/camiones")
                                                .hasRole("OPERADOR")

                                                // Permitir asignar tramo a OPERADOR
                                                .requestMatchers(HttpMethod.POST, "/api/flota/tramos/*/asignar")
                                                .hasRole("OPERADOR")

                                                // Endpoints de VIAJES (Iniciar/Finalizar) para ROL TRANSPORTISTA
                                                // (Se validará por @PreAuthorize en el controlador, pero podemos
                                                // ponerlo aquí
                                                // también)
                                                .requestMatchers("/api/flota/tramos/**").hasRole("TRANSPORTISTA")

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
                                                                                "ROLE_" + role))
                                                                .collect(java.util.stream.Collectors.toList());
                                        }
                                });
                return converter;
        }
}
package com.gestion.config;

import com.gestion.properties.CorsProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@AllArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // sin origenes configurados no se registra ninguna configuracion: solo same-origin
        if (corsProperties.allowedOrigins().isEmpty()) {
            return source;
        }

        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(corsProperties.allowedOrigins());
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // solo los headers que manda el front: el body y el token CSRF (la sesion va en cookie).
        // No se expone Set-Cookie: los navegadores nunca lo dejan leer desde JS.
        corsConfig.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}

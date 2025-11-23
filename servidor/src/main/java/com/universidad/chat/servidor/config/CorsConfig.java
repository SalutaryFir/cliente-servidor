package com.universidad.chat.servidor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuración de CORS para permitir llamadas desde API Gateway y aplicación web
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permitir credenciales
        config.setAllowCredentials(true);
        
        // Permitir todos los orígenes (ajustar en producción a dominios específicos)
        config.addAllowedOriginPattern("*");
        
        // Permitir todos los headers
        config.addAllowedHeader("*");
        
        // Permitir todos los métodos HTTP
        config.addAllowedMethod("*");
        
        // Exponer headers para que el cliente pueda leerlos
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        
        source.registerCorsConfiguration("/api/**", config);
        
        return new CorsFilter(source);
    }
}

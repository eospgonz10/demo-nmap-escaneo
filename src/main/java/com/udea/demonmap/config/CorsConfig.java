package com.udea.demonmap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing).
 * 
 * Permite que la API sea consumida desde cualquier origen (frontend en diferentes dominios/puertos).
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permitir credenciales (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);
        
        // Permitir todos los orígenes
        config.addAllowedOriginPattern("*");
        
        // Permitir todos los headers
        config.addAllowedHeader("*");
        
        // Permitir todos los métodos HTTP (GET, POST, PUT, DELETE, etc.)
        config.addAllowedMethod("*");
        
        // Exponer headers de respuesta al cliente
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        
        // Aplicar configuración a todos los endpoints
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}

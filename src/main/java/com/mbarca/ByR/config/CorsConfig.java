package com.mbarca.ByR.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import java.util.Arrays;
import java.util.List;
@Configuration
public class CorsConfig {
    @Bean public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.allowed-origins:https://inmobiliariabyr.com.ar,https://www.inmobiliariabyr.com.ar,http://localhost:3000,http://127.0.0.1:3000}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(origin -> !origin.isBlank()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN", "X-Requested-With"));
        config.setExposedHeaders(List.of("X-CSRF-TOKEN"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

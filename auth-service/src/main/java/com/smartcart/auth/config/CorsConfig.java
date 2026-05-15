package com.smartcart.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS configuration for the Auth Service.
 *
 * Allowed origins are set in application.yml:
 *   app.cors.allowed-origins: http://localhost:4200,https://smartcart.netlify.app
 *
 * SecurityConfig reads corsConfigurationSource() bean via:
 *   .cors(cors -> cors.configurationSource(corsConfigurationSource))
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Only allow known origins — never wildcard when credentials are sent
        config.setAllowedOrigins(allowedOrigins);

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "User-Agent",
                "X-Forwarded-For"
        ));

        // Expose Authorization so Angular can read the response header if needed
        config.setExposedHeaders(List.of("Authorization"));

        // Required so Angular can send Authorization header (credentials = true)
        config.setAllowCredentials(true);

        // Cache preflight for 1 hour — reduces OPTIONS requests
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
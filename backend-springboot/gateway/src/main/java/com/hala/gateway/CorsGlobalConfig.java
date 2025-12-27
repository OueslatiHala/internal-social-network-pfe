package com.hala.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsGlobalConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:4200");  // Angular local
        corsConfig.addAllowedMethod("*");                      // GET, POST, etc.
        corsConfig.addAllowedHeader("*");                      // tous les headers
        corsConfig.setAllowCredentials(true);                  // cookies, headers sécurisés

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);   // toutes les routes

        return new CorsWebFilter(source);
    }
}

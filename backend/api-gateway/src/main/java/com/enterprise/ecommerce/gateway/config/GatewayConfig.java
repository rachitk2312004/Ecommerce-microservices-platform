package com.enterprise.ecommerce.gateway.config;

import com.enterprise.ecommerce.gateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GatewayConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, JwtAuthenticationFilter jwtFilter) {
        return builder.routes()
                .route("user-auth", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://USER-SERVICE"))
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://USER-SERVICE"))
                .route("product-service", r -> r.path("/api/products/**", "/api/categories/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://PRODUCT-SERVICE"))
                .route("inventory-service", r -> r.path("/api/inventory/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://INVENTORY-SERVICE"))
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://ORDER-SERVICE"))
                .route("payment-service", r -> r.path("/api/payments/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://PAYMENT-SERVICE"))
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://NOTIFICATION-SERVICE"))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}

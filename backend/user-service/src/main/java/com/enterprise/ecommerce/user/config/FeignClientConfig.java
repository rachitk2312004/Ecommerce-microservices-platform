package com.enterprise.ecommerce.user.config;

import com.enterprise.ecommerce.user.security.AuthenticatedUser;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignClientConfig {

    @Value("${app.internal.service-key}")
    private String internalServiceKey;

    @Bean
    public RequestInterceptor feignAuthInterceptor() {
        return template -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
                template.header("X-User-Id", String.valueOf(user.getUserId()));
                template.header("X-User-Role", user.getRole());
                if (user.getEmail() != null) {
                    template.header("X-User-Email", user.getEmail());
                }
            } else {
                template.header("X-Internal-Service-Key", internalServiceKey);
            }
        };
    }
}

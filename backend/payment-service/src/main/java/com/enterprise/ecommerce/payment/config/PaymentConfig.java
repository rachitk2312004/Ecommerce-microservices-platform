package com.enterprise.ecommerce.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class PaymentConfig {

    @Bean
    public Random random() {
        return new Random();
    }
}

package com.enterprise.ecommerce.order.service;

import com.enterprise.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrderRepository orderRepository;

    public String generate() {
        String datePart = LocalDate.now().format(DATE_FORMAT);
        for (int attempt = 0; attempt < 20; attempt++) {
            int suffix = RANDOM.nextInt(10_000);
            String orderNumber = String.format("ORD-%s-%04d", datePart, suffix);
            if (!orderRepository.existsByOrderNumber(orderNumber)) {
                return orderNumber;
            }
        }
        throw new IllegalStateException("Unable to generate unique order number");
    }
}

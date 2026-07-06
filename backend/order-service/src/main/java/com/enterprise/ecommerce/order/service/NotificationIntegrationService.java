package com.enterprise.ecommerce.order.service;

import com.enterprise.ecommerce.order.client.NotificationClient;
import com.enterprise.ecommerce.order.client.dto.CreateNotificationRequest;
import com.enterprise.ecommerce.order.enums.NotificationType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationIntegrationService {

    private final NotificationClient notificationClient;

    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendNotificationFallback")
    @Retry(name = "notificationService")
    public void sendNotification(Long userId, NotificationType type, String title, String message) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .build();
        notificationClient.sendNotification(request);
    }

    private void sendNotificationFallback(Long userId, NotificationType type, String title, String message,
                                          Throwable throwable) {
        log.warn("Failed to send {} notification to user {} after retries: {}", type, userId, throwable.getMessage());
    }
}

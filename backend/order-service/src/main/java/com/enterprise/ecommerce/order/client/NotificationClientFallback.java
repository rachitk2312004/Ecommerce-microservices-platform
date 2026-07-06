package com.enterprise.ecommerce.order.client;

import com.enterprise.ecommerce.order.client.dto.CreateNotificationRequest;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public ApiResponse<Void> sendNotification(CreateNotificationRequest request) {
        log.warn("Notification service fallback triggered for user {} type {}",
                request.getUserId(), request.getType());
        return ApiResponse.success("Notification skipped due to service unavailability", null);
    }
}

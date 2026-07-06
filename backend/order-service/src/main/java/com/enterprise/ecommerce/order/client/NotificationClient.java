package com.enterprise.ecommerce.order.client;

import com.enterprise.ecommerce.order.client.dto.CreateNotificationRequest;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE", path = "/api/notifications", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping
    ApiResponse<Void> sendNotification(@RequestBody CreateNotificationRequest request);
}

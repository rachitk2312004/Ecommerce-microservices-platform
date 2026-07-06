package com.enterprise.ecommerce.user.client;

import com.enterprise.ecommerce.user.dto.ApiResponse;
import com.enterprise.ecommerce.user.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE", path = "/api/notifications")
public interface NotificationClient {

    @PostMapping
    ApiResponse<Void> sendNotification(@RequestBody NotificationRequest request);
}

package com.enterprise.ecommerce.notification.mapper;

import com.enterprise.ecommerce.notification.dto.NotificationResponse;
import com.enterprise.ecommerce.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

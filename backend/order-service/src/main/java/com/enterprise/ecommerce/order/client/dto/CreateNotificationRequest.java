package com.enterprise.ecommerce.order.client.dto;

import com.enterprise.ecommerce.order.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
}

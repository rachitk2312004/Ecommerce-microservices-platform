package com.enterprise.ecommerce.user.dto;

import com.enterprise.ecommerce.user.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
}

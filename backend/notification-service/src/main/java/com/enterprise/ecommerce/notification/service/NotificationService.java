package com.enterprise.ecommerce.notification.service;

import com.enterprise.ecommerce.notification.dto.CreateNotificationRequest;
import com.enterprise.ecommerce.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void createNotification(CreateNotificationRequest request);

    List<NotificationResponse> getNotificationsByUserId(Long userId);
}

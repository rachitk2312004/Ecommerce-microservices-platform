package com.enterprise.ecommerce.notification.service.impl;

import com.enterprise.ecommerce.notification.dto.CreateNotificationRequest;
import com.enterprise.ecommerce.notification.dto.NotificationResponse;
import com.enterprise.ecommerce.notification.entity.Notification;
import com.enterprise.ecommerce.notification.mapper.NotificationMapper;
import com.enterprise.ecommerce.notification.repository.NotificationRepository;
import com.enterprise.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .build();

        notificationRepository.save(notification);

        log.info("Notification created for user {}: [{}] {} - {}",
                request.getUserId(), request.getType(), request.getTitle(), request.getMessage());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }
}

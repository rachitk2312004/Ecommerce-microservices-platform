package com.enterprise.ecommerce.notification.service;

import com.enterprise.ecommerce.notification.dto.CreateNotificationRequest;
import com.enterprise.ecommerce.notification.dto.NotificationResponse;
import com.enterprise.ecommerce.notification.entity.Notification;
import com.enterprise.ecommerce.notification.enums.NotificationType;
import com.enterprise.ecommerce.notification.mapper.NotificationMapper;
import com.enterprise.ecommerce.notification.repository.NotificationRepository;
import com.enterprise.ecommerce.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper = new NotificationMapper();
    private NotificationServiceImpl notificationService;

    private CreateNotificationRequest request;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository, notificationMapper);

        request = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.ORDER_CONFIRMED)
                .title("Order Confirmed")
                .message("Your order has been confirmed")
                .build();
    }

    @Test
    void createNotification_savesAndLogs() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            return notification;
        });

        notificationService.createNotification(request);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getNotificationsByUserId_returnsMappedList() {
        Notification notification = Notification.builder()
                .id(1L)
                .userId(1L)
                .type(NotificationType.PAYMENT_SUCCESS)
                .title("Payment Success")
                .message("Payment completed")
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(notification));

        List<NotificationResponse> results = notificationService.getNotificationsByUserId(1L);

        assertEquals(1, results.size());
        assertEquals("Payment Success", results.get(0).getTitle());
        assertEquals(NotificationType.PAYMENT_SUCCESS, results.get(0).getType());
    }
}

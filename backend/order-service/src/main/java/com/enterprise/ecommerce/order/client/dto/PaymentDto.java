package com.enterprise.ecommerce.order.client.dto;

import com.enterprise.ecommerce.order.enums.PaymentMethod;
import com.enterprise.ecommerce.order.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String transactionId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

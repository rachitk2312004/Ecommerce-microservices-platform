package com.enterprise.ecommerce.payment.dto;

import com.enterprise.ecommerce.payment.enums.PaymentMethod;
import com.enterprise.ecommerce.payment.enums.PaymentStatus;
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
public class PaymentResponse {

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

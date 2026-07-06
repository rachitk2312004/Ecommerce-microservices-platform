package com.enterprise.ecommerce.payment.service;

import com.enterprise.ecommerce.payment.dto.PaymentResponse;
import com.enterprise.ecommerce.payment.dto.ProcessPaymentRequest;
import com.enterprise.ecommerce.payment.entity.Payment;
import com.enterprise.ecommerce.payment.enums.PaymentMethod;
import com.enterprise.ecommerce.payment.enums.PaymentStatus;
import com.enterprise.ecommerce.payment.mapper.PaymentMapper;
import com.enterprise.ecommerce.payment.repository.PaymentRepository;
import com.enterprise.ecommerce.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentSimulator paymentSimulator;

    private final PaymentMapper paymentMapper = new PaymentMapper();
    private PaymentServiceImpl paymentService;

    private ProcessPaymentRequest request;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, paymentMapper, paymentSimulator);

        request = ProcessPaymentRequest.builder()
                .orderId(1L)
                .userId(2L)
                .amount(new BigDecimal("99.99"))
                .paymentMethod(PaymentMethod.CARD)
                .idempotencyKey("idem-key-1")
                .build();
    }

    @Test
    void processPayment_success() {
        when(paymentRepository.findByIdempotencyKey("idem-key-1")).thenReturn(Optional.empty());
        when(paymentSimulator.simulateSuccess()).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(10L);
            return payment;
        });

        PaymentResponse result = paymentService.processPayment(request);

        assertEquals(PaymentStatus.SUCCESS, result.getPaymentStatus());
        assertNotNull(result.getTransactionId());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processPayment_failure() {
        when(paymentRepository.findByIdempotencyKey("idem-key-1")).thenReturn(Optional.empty());
        when(paymentSimulator.simulateSuccess()).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(10L);
            return payment;
        });

        PaymentResponse result = paymentService.processPayment(request);

        assertEquals(PaymentStatus.FAILED, result.getPaymentStatus());
        assertEquals("Payment simulation failed", result.getFailureReason());
        assertNull(result.getTransactionId());
        verify(paymentRepository).save(any(Payment.class));
    }
}

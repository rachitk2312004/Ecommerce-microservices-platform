package com.enterprise.ecommerce.payment.service.impl;

import com.enterprise.ecommerce.payment.dto.PaymentResponse;
import com.enterprise.ecommerce.payment.dto.ProcessPaymentRequest;
import com.enterprise.ecommerce.payment.entity.Payment;
import com.enterprise.ecommerce.payment.enums.PaymentStatus;
import com.enterprise.ecommerce.payment.exception.BadRequestException;
import com.enterprise.ecommerce.payment.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.payment.mapper.PaymentMapper;
import com.enterprise.ecommerce.payment.repository.PaymentRepository;
import com.enterprise.ecommerce.payment.service.PaymentService;
import com.enterprise.ecommerce.payment.service.PaymentSimulator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentSimulator paymentSimulator;

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .map(paymentMapper::toResponse)
                .orElseGet(() -> paymentMapper.toResponse(createPayment(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only successful payments can be refunded");
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    private Payment createPayment(ProcessPaymentRequest request) {
        boolean success = paymentSimulator.simulateSuccess();

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .idempotencyKey(request.getIdempotencyKey())
                .paymentStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .transactionId(success ? "TXN-" + UUID.randomUUID() : null)
                .failureReason(success ? null : "Payment simulation failed")
                .build();

        return paymentRepository.save(payment);
    }
}

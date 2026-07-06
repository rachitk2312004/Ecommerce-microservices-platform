package com.enterprise.ecommerce.payment.service;

import com.enterprise.ecommerce.payment.dto.PaymentResponse;
import com.enterprise.ecommerce.payment.dto.ProcessPaymentRequest;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(ProcessPaymentRequest request);

    List<PaymentResponse> getPaymentsByOrderId(Long orderId);

    PaymentResponse refundPayment(Long paymentId);
}

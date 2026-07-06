package com.enterprise.ecommerce.order.client;

import com.enterprise.ecommerce.order.client.dto.PaymentDto;
import com.enterprise.ecommerce.order.client.dto.ProcessPaymentRequest;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import com.enterprise.ecommerce.order.exception.PaymentFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PaymentClientFallback implements PaymentClient {

    @Override
    public ApiResponse<PaymentDto> processPayment(ProcessPaymentRequest request) {
        log.error("Payment service fallback triggered for order {}", request.getOrderId());
        throw new PaymentFailedException("Payment service is currently unavailable");
    }

    @Override
    public ApiResponse<List<PaymentDto>> getPaymentsByOrderId(Long orderId) {
        log.error("Payment service fallback triggered for order lookup {}", orderId);
        throw new PaymentFailedException("Payment service is currently unavailable");
    }

    @Override
    public ApiResponse<PaymentDto> refundPayment(Long paymentId) {
        log.error("Payment service fallback triggered for refund {}", paymentId);
        throw new PaymentFailedException("Payment service is currently unavailable");
    }
}

package com.enterprise.ecommerce.order.client;

import com.enterprise.ecommerce.order.client.dto.PaymentDto;
import com.enterprise.ecommerce.order.client.dto.ProcessPaymentRequest;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "PAYMENT-SERVICE", path = "/api/payments", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @PostMapping("/process")
    ApiResponse<PaymentDto> processPayment(@RequestBody ProcessPaymentRequest request);

    @GetMapping("/order/{orderId}")
    ApiResponse<List<PaymentDto>> getPaymentsByOrderId(@PathVariable("orderId") Long orderId);

    @PostMapping("/{paymentId}/refund")
    ApiResponse<PaymentDto> refundPayment(@PathVariable("paymentId") Long paymentId);
}

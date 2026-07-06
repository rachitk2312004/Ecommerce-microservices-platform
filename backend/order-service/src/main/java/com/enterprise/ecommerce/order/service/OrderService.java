package com.enterprise.ecommerce.order.service;

import com.enterprise.ecommerce.order.dto.CreateOrderRequest;
import com.enterprise.ecommerce.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, Long userId);

    OrderResponse getOrderById(Long orderId, Long userId, boolean isAdmin);

    List<OrderResponse> getMyOrders(Long userId);

    OrderResponse cancelOrder(Long orderId, Long userId, boolean isAdmin);
}

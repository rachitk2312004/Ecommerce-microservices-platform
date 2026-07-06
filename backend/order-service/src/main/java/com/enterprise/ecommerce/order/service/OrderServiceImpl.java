package com.enterprise.ecommerce.order.service;

import com.enterprise.ecommerce.order.client.InventoryClient;
import com.enterprise.ecommerce.order.client.PaymentClient;
import com.enterprise.ecommerce.order.client.dto.*;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import com.enterprise.ecommerce.order.dto.CreateOrderRequest;
import com.enterprise.ecommerce.order.dto.OrderItemRequest;
import com.enterprise.ecommerce.order.dto.OrderResponse;
import com.enterprise.ecommerce.order.entity.Order;
import com.enterprise.ecommerce.order.entity.OrderItem;
import com.enterprise.ecommerce.order.enums.NotificationType;
import com.enterprise.ecommerce.order.enums.OrderStatus;
import com.enterprise.ecommerce.order.enums.PaymentMethod;
import com.enterprise.ecommerce.order.enums.PaymentStatus;
import com.enterprise.ecommerce.order.exception.*;
import com.enterprise.ecommerce.order.mapper.OrderMapper;
import com.enterprise.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductIntegrationService productIntegrationService;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final NotificationIntegrationService notificationIntegrationService;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        validateItems(request.getItems());

        List<ResolvedItem> resolvedItems = resolveAndPriceItems(request.getItems());
        BigDecimal totalAmount = resolvedItems.stream()
                .map(ResolvedItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String orderNumber = orderNumberGenerator.generate();
        Order order = orderRepository.save(Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .items(new ArrayList<>())
                .build());

        List<OrderItem> orderItems = buildOrderItems(order, resolvedItems);
        order.getItems().addAll(orderItems);
        order = orderRepository.save(order);

        try {
            checkAndReserveInventory(order, orderItems);
            order.setOrderStatus(OrderStatus.INVENTORY_RESERVED);
            orderRepository.save(order);

            order.setOrderStatus(OrderStatus.PAYMENT_PROCESSING);
            orderRepository.save(order);

            PaymentDto payment = processPayment(order, request.getPaymentMethod());

            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                confirmInventory(order, orderItems);
                order.setOrderStatus(OrderStatus.CONFIRMED);
                order.setPaymentStatus(PaymentStatus.SUCCESS);
                orderRepository.save(order);
                sendOrderConfirmedNotifications(order);
                return orderMapper.toResponse(order);
            }

            releaseInventory(order, orderItems);
            order.setOrderStatus(OrderStatus.FAILED);
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            notificationIntegrationService.sendNotification(
                    order.getUserId(),
                    NotificationType.PAYMENT_FAILED,
                    "Payment Failed",
                    "Payment failed for order " + order.getOrderNumber()
            );
            throw new PaymentFailedException(
                    payment.getFailureReason() != null
                            ? payment.getFailureReason()
                            : "Payment failed for order " + order.getOrderNumber()
            );
        } catch (InsufficientStockException ex) {
            order.setOrderStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId, boolean isAdmin) {
        Order order = findOrder(orderId);
        assertOwnerOrAdmin(order, userId, isAdmin);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId, boolean isAdmin) {
        Order order = findOrder(orderId);
        assertOwnerOrAdmin(order, userId, isAdmin);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new OrderCancellationException("Order is already cancelled");
        }
        if (order.getOrderStatus() == OrderStatus.FAILED) {
            throw new OrderCancellationException("Failed orders cannot be cancelled");
        }

        switch (order.getOrderStatus()) {
            case PENDING -> { /* no inventory action needed */ }
            case INVENTORY_RESERVED, PAYMENT_PROCESSING -> releaseInventory(order, order.getItems());
            case CONFIRMED -> {
                restoreInventory(order, order.getItems());
                refundPayment(order);
            }
            default -> throw new OrderCancellationException(
                    "Order cannot be cancelled in status " + order.getOrderStatus()
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        orderRepository.save(order);

        notificationIntegrationService.sendNotification(
                order.getUserId(),
                NotificationType.ORDER_CANCELLED,
                "Order Cancelled",
                "Your order " + order.getOrderNumber() + " has been cancelled"
        );

        return orderMapper.toResponse(order);
    }

    private void validateItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }
    }

    private List<ResolvedItem> resolveAndPriceItems(List<OrderItemRequest> items) {
        List<ResolvedItem> resolved = new ArrayList<>();
        for (OrderItemRequest item : items) {
            ProductDto product = productIntegrationService.getProduct(item.getProductId());
            if (!product.isActive()) {
                throw new ProductUnavailableException("Product is not active: " + item.getProductId());
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            resolved.add(new ResolvedItem(product, item.getQuantity(), subtotal));
        }
        return resolved;
    }

    private List<OrderItem> buildOrderItems(Order order, List<ResolvedItem> resolvedItems) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (ResolvedItem resolved : resolvedItems) {
            ProductDto product = resolved.product();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(resolved.quantity())
                    .unitPrice(product.getPrice())
                    .subtotal(resolved.subtotal())
                    .build();
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    private void checkAndReserveInventory(Order order, List<OrderItem> items) {
        for (OrderItem item : items) {
            CheckAvailabilityRequest checkRequest = CheckAvailabilityRequest.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .build();
            ApiResponse<CheckAvailabilityResponse> checkResponse = inventoryClient.checkAvailability(checkRequest);
            if (checkResponse == null || !checkResponse.isSuccess()
                    || checkResponse.getData() == null || !checkResponse.getData().isAvailable()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product " + item.getProductId());
            }

            InventoryOperationRequest reserveRequest = InventoryOperationRequest.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .orderId(order.getId())
                    .build();
            ApiResponse<Void> reserveResponse = inventoryClient.reserve(reserveRequest);
            if (reserveResponse == null || !reserveResponse.isSuccess()) {
                throw new InsufficientStockException(
                        "Failed to reserve stock for product " + item.getProductId());
            }
        }
    }

    private void confirmInventory(Order order, List<OrderItem> items) {
        for (OrderItem item : items) {
            InventoryOperationRequest request = InventoryOperationRequest.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .orderId(order.getId())
                    .build();
            inventoryClient.confirm(request);
        }
    }

    private void releaseInventory(Order order, List<OrderItem> items) {
        for (OrderItem item : items) {
            InventoryOperationRequest request = InventoryOperationRequest.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .orderId(order.getId())
                    .build();
            inventoryClient.release(request);
        }
    }

    private void restoreInventory(Order order, List<OrderItem> items) {
        for (OrderItem item : items) {
            InventoryOperationRequest request = InventoryOperationRequest.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .orderId(order.getId())
                    .build();
            inventoryClient.restore(request);
        }
    }

    private PaymentDto processPayment(Order order, PaymentMethod paymentMethod) {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .amount(order.getTotalAmount())
                .paymentMethod(paymentMethod)
                .idempotencyKey(order.getOrderNumber())
                .build();

        ApiResponse<PaymentDto> response = paymentClient.processPayment(request);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new PaymentFailedException("Payment processing failed for order " + order.getOrderNumber());
        }
        return response.getData();
    }

    private void refundPayment(Order order) {
        ApiResponse<List<PaymentDto>> paymentsResponse = paymentClient.getPaymentsByOrderId(order.getId());
        if (paymentsResponse == null || !paymentsResponse.isSuccess() || paymentsResponse.getData() == null) {
            throw new PaymentFailedException("Unable to retrieve payments for order " + order.getOrderNumber());
        }

        PaymentDto successfulPayment = paymentsResponse.getData().stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.SUCCESS)
                .findFirst()
                .orElseThrow(() -> new PaymentFailedException(
                        "No successful payment found for order " + order.getOrderNumber()));

        ApiResponse<PaymentDto> refundResponse = paymentClient.refundPayment(successfulPayment.getId());
        if (refundResponse == null || !refundResponse.isSuccess()) {
            throw new PaymentFailedException("Refund failed for order " + order.getOrderNumber());
        }
    }

    private void sendOrderConfirmedNotifications(Order order) {
        notificationIntegrationService.sendNotification(
                order.getUserId(),
                NotificationType.ORDER_CONFIRMED,
                "Order Confirmed",
                "Your order " + order.getOrderNumber() + " has been confirmed"
        );
        notificationIntegrationService.sendNotification(
                order.getUserId(),
                NotificationType.PAYMENT_SUCCESS,
                "Payment Successful",
                "Payment for order " + order.getOrderNumber() + " was successful"
        );
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private void assertOwnerOrAdmin(Order order, Long userId, boolean isAdmin) {
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this order");
        }
    }

    private record ResolvedItem(ProductDto product, Integer quantity, BigDecimal subtotal) {
    }
}

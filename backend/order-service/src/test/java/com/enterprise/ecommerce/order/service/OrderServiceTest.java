package com.enterprise.ecommerce.order.service;

import com.enterprise.ecommerce.order.client.InventoryClient;
import com.enterprise.ecommerce.order.client.NotificationClient;
import com.enterprise.ecommerce.order.client.PaymentClient;
import com.enterprise.ecommerce.order.client.ProductClient;
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
import com.enterprise.ecommerce.order.exception.InsufficientStockException;
import com.enterprise.ecommerce.order.exception.OrderCancellationException;
import com.enterprise.ecommerce.order.exception.PaymentFailedException;
import com.enterprise.ecommerce.order.mapper.OrderMapper;
import com.enterprise.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductClient productClient;
    @Mock private InventoryClient inventoryClient;
    @Mock private PaymentClient paymentClient;
    @Mock private NotificationClient notificationClient;

    private final OrderMapper orderMapper = new OrderMapper();
    private OrderNumberGenerator orderNumberGenerator;
    private ProductIntegrationService productIntegrationService;
    private NotificationIntegrationService notificationIntegrationService;
    private OrderServiceImpl orderService;

    private ProductDto product;
    private CreateOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        orderNumberGenerator = new OrderNumberGenerator(orderRepository);
        productIntegrationService = new ProductIntegrationService(productClient);
        notificationIntegrationService = new NotificationIntegrationService(notificationClient);
        orderService = new OrderServiceImpl(
                orderRepository,
                productIntegrationService,
                inventoryClient,
                paymentClient,
                notificationIntegrationService,
                orderNumberGenerator,
                orderMapper
        );

        product = ProductDto.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("50.00"))
                .active(true)
                .build();

        createRequest = CreateOrderRequest.builder()
                .items(List.of(OrderItemRequest.builder().productId(1L).quantity(2).build()))
                .paymentMethod(PaymentMethod.CARD)
                .build();
    }

    private void stubUniqueOrderNumber() {
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
    }

    @Test
    void createOrder_success() {
        stubUniqueOrderNumber();
        when(productClient.getProductById(1L)).thenReturn(ApiResponse.success(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(100L);
                order.setCreatedAt(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
            }
            return order;
        });
        when(inventoryClient.checkAvailability(any())).thenReturn(
                ApiResponse.success(CheckAvailabilityResponse.builder().available(true).build()));
        when(inventoryClient.reserve(any())).thenReturn(ApiResponse.success("Stock reserved", null));
        when(inventoryClient.confirm(any())).thenReturn(ApiResponse.success("Reservation confirmed", null));
        when(paymentClient.processPayment(any())).thenReturn(ApiResponse.success("Payment processed",
                PaymentDto.builder()
                        .id(1L)
                        .orderId(100L)
                        .paymentStatus(PaymentStatus.SUCCESS)
                        .amount(new BigDecimal("100.00"))
                        .build()));

        OrderResponse result = orderService.createOrder(createRequest, 10L);

        assertEquals(OrderStatus.CONFIRMED, result.getOrderStatus());
        assertEquals(PaymentStatus.SUCCESS, result.getPaymentStatus());
        assertEquals(new BigDecimal("100.00"), result.getTotalAmount());
        assertNotNull(result.getOrderNumber());
        assertTrue(result.getOrderNumber().startsWith("ORD-"));
        verify(inventoryClient).reserve(any());
        verify(inventoryClient).confirm(any());
        verify(notificationClient, atLeastOnce()).sendNotification(any());
    }

    @Test
    void createOrder_insufficientStock() {
        stubUniqueOrderNumber();
        when(productClient.getProductById(1L)).thenReturn(ApiResponse.success(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(101L);
            return order;
        });
        when(inventoryClient.checkAvailability(any())).thenReturn(
                ApiResponse.success(CheckAvailabilityResponse.builder().available(false).build()));

        InsufficientStockException ex = assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(createRequest, 10L)
        );

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(paymentClient, never()).processPayment(any());
        verify(orderRepository, atLeastOnce()).save(argThat(order ->
                order.getOrderStatus() == OrderStatus.FAILED));
    }

    @Test
    void createOrder_paymentFailure() {
        stubUniqueOrderNumber();
        when(productClient.getProductById(1L)).thenReturn(ApiResponse.success(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(102L);
            }
            return order;
        });
        when(inventoryClient.checkAvailability(any())).thenReturn(
                ApiResponse.success(CheckAvailabilityResponse.builder().available(true).build()));
        when(inventoryClient.reserve(any())).thenReturn(ApiResponse.success("Stock reserved", null));
        when(inventoryClient.release(any())).thenReturn(ApiResponse.success("Reservation released", null));
        when(paymentClient.processPayment(any())).thenReturn(ApiResponse.success("Payment processed",
                PaymentDto.builder()
                        .paymentStatus(PaymentStatus.FAILED)
                        .failureReason("Card declined")
                        .build()));

        PaymentFailedException ex = assertThrows(
                PaymentFailedException.class,
                () -> orderService.createOrder(createRequest, 10L)
        );

        assertEquals("Card declined", ex.getMessage());
        verify(inventoryClient).release(any());
        verify(notificationClient).sendNotification(argThat(request ->
                request.getType() == NotificationType.PAYMENT_FAILED));
        verify(orderRepository, atLeastOnce()).save(argThat(order ->
                order.getOrderStatus() == OrderStatus.FAILED
                        && order.getPaymentStatus() == PaymentStatus.FAILED));
    }

    @Test
    void cancelOrder_fromInventoryReserved_releasesStock() {
        Order order = buildOrder(OrderStatus.INVENTORY_RESERVED, PaymentStatus.PENDING);
        OrderItem item = buildOrderItem(order);
        order.getItems().add(item);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryClient.release(any())).thenReturn(ApiResponse.success("Reservation released", null));

        OrderResponse result = orderService.cancelOrder(100L, 10L, false);

        assertEquals(OrderStatus.CANCELLED, result.getOrderStatus());
        verify(inventoryClient).release(any());
        verify(notificationClient).sendNotification(argThat(request ->
                request.getType() == NotificationType.ORDER_CANCELLED));
    }

    @Test
    void cancelOrder_fromConfirmed_restoresAndRefunds() {
        Order order = buildOrder(OrderStatus.CONFIRMED, PaymentStatus.SUCCESS);
        OrderItem item = buildOrderItem(order);
        order.getItems().add(item);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryClient.restore(any())).thenReturn(ApiResponse.success("Stock restored", null));
        when(paymentClient.getPaymentsByOrderId(100L)).thenReturn(ApiResponse.success(List.of(
                PaymentDto.builder().id(5L).paymentStatus(PaymentStatus.SUCCESS).build()
        )));
        when(paymentClient.refundPayment(5L)).thenReturn(ApiResponse.success("Payment refunded",
                PaymentDto.builder().id(5L).paymentStatus(PaymentStatus.REFUNDED).build()));

        OrderResponse result = orderService.cancelOrder(100L, 10L, false);

        assertEquals(OrderStatus.CANCELLED, result.getOrderStatus());
        assertEquals(PaymentStatus.REFUNDED, result.getPaymentStatus());
        verify(inventoryClient).restore(any());
        verify(paymentClient).refundPayment(5L);
    }

    @Test
    void cancelOrder_alreadyCancelled_throws() {
        Order order = buildOrder(OrderStatus.CANCELLED, PaymentStatus.PENDING);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(OrderCancellationException.class,
                () -> orderService.cancelOrder(100L, 10L, false));
    }

    @Test
    void processPayment_usesOrderNumberAsIdempotencyKey() {
        stubUniqueOrderNumber();
        when(productClient.getProductById(1L)).thenReturn(ApiResponse.success(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(200L);
            return order;
        });
        when(inventoryClient.checkAvailability(any())).thenReturn(
                ApiResponse.success(CheckAvailabilityResponse.builder().available(true).build()));
        when(inventoryClient.reserve(any())).thenReturn(ApiResponse.success("Stock reserved", null));
        when(inventoryClient.confirm(any())).thenReturn(ApiResponse.success("Reservation confirmed", null));
        when(paymentClient.processPayment(any())).thenReturn(ApiResponse.success("Payment processed",
                PaymentDto.builder().paymentStatus(PaymentStatus.SUCCESS).build()));

        orderService.createOrder(createRequest, 10L);

        ArgumentCaptor<ProcessPaymentRequest> captor = ArgumentCaptor.forClass(ProcessPaymentRequest.class);
        verify(paymentClient).processPayment(captor.capture());
        assertNotNull(captor.getValue().getIdempotencyKey());
        assertTrue(captor.getValue().getIdempotencyKey().startsWith("ORD-"));
    }

    private Order buildOrder(OrderStatus status, PaymentStatus paymentStatus) {
        return Order.builder()
                .id(100L)
                .orderNumber("ORD-20260706-0001")
                .userId(10L)
                .totalAmount(new BigDecimal("100.00"))
                .orderStatus(status)
                .paymentStatus(paymentStatus)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private OrderItem buildOrderItem(Order order) {
        return OrderItem.builder()
                .id(1L)
                .order(order)
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .build();
    }
}

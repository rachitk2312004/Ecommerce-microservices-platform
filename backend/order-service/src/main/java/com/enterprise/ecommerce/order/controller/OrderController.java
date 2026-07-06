package com.enterprise.ecommerce.order.controller;

import com.enterprise.ecommerce.order.dto.ApiResponse;
import com.enterprise.ecommerce.order.dto.CreateOrderRequest;
import com.enterprise.ecommerce.order.dto.OrderResponse;
import com.enterprise.ecommerce.order.security.AuthenticatedUser;
import com.enterprise.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID (owner or admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id, user.getUserId(), isAdmin(user));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get current user's order history")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(user.getUserId())));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        OrderResponse response = orderService.cancelOrder(id, user.getUserId(), isAdmin(user));
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", response));
    }

    private boolean isAdmin(AuthenticatedUser user) {
        return user != null && "ADMIN".equals(user.getRole());
    }
}

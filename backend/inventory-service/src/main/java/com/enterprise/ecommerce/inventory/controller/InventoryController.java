package com.enterprise.ecommerce.inventory.controller;

import com.enterprise.ecommerce.inventory.dto.*;
import com.enterprise.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory management and reservation operations")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory for a product")
    public ResponseEntity<ApiResponse<InventoryResponse>> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getByProductId(productId)));
    }

    @PostMapping("/check")
    @Operation(summary = "Check product availability")
    public ResponseEntity<ApiResponse<CheckAvailabilityResponse>> checkAvailability(
            @Valid @RequestBody CheckAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.checkAvailability(request)));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for an order")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserve(
            @Valid @RequestBody InventoryOperationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock reserved", inventoryService.reserve(request)));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm reserved stock after order completion")
    public ResponseEntity<ApiResponse<InventoryResponse>> confirm(
            @Valid @RequestBody InventoryOperationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reservation confirmed", inventoryService.confirm(request)));
    }

    @PostMapping("/release")
    @Operation(summary = "Release reserved stock when order is cancelled")
    public ResponseEntity<ApiResponse<InventoryResponse>> release(
            @Valid @RequestBody InventoryOperationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reservation released", inventoryService.release(request)));
    }

    @PostMapping("/restore")
    @Operation(summary = "Restore stock after a confirmed order is cancelled")
    public ResponseEntity<ApiResponse<InventoryResponse>> restore(
            @Valid @RequestBody InventoryOperationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock restored", inventoryService.restore(request)));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin update available stock quantity")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock updated", inventoryService.updateStock(productId, request)));
    }
}

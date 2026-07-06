package com.enterprise.ecommerce.order.client;

import com.enterprise.ecommerce.order.client.dto.CheckAvailabilityRequest;
import com.enterprise.ecommerce.order.client.dto.CheckAvailabilityResponse;
import com.enterprise.ecommerce.order.client.dto.InventoryOperationRequest;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import com.enterprise.ecommerce.order.exception.InsufficientStockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public ApiResponse<CheckAvailabilityResponse> checkAvailability(CheckAvailabilityRequest request) {
        log.error("Inventory service fallback triggered for check on product {}", request.getProductId());
        throw new InsufficientStockException("Inventory service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> reserve(InventoryOperationRequest request) {
        log.error("Inventory service fallback triggered for reserve on product {}", request.getProductId());
        throw new InsufficientStockException("Inventory service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> confirm(InventoryOperationRequest request) {
        log.error("Inventory service fallback triggered for confirm on product {}", request.getProductId());
        throw new InsufficientStockException("Inventory service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> release(InventoryOperationRequest request) {
        log.error("Inventory service fallback triggered for release on product {}", request.getProductId());
        throw new InsufficientStockException("Inventory service is currently unavailable");
    }

    @Override
    public ApiResponse<Void> restore(InventoryOperationRequest request) {
        log.error("Inventory service fallback triggered for restore on product {}", request.getProductId());
        throw new InsufficientStockException("Inventory service is currently unavailable");
    }
}

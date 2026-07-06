package com.enterprise.ecommerce.order.client;

import com.enterprise.ecommerce.order.client.dto.CheckAvailabilityRequest;
import com.enterprise.ecommerce.order.client.dto.CheckAvailabilityResponse;
import com.enterprise.ecommerce.order.client.dto.InventoryOperationRequest;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "INVENTORY-SERVICE", path = "/api/inventory", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @PostMapping("/check")
    ApiResponse<CheckAvailabilityResponse> checkAvailability(@RequestBody CheckAvailabilityRequest request);

    @PostMapping("/reserve")
    ApiResponse<Void> reserve(@RequestBody InventoryOperationRequest request);

    @PostMapping("/confirm")
    ApiResponse<Void> confirm(@RequestBody InventoryOperationRequest request);

    @PostMapping("/release")
    ApiResponse<Void> release(@RequestBody InventoryOperationRequest request);

    @PostMapping("/restore")
    ApiResponse<Void> restore(@RequestBody InventoryOperationRequest request);
}

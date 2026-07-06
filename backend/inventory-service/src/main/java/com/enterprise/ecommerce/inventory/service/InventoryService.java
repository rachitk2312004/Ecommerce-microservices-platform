package com.enterprise.ecommerce.inventory.service;

import com.enterprise.ecommerce.inventory.dto.*;

public interface InventoryService {

    InventoryResponse getByProductId(Long productId);

    CheckAvailabilityResponse checkAvailability(CheckAvailabilityRequest request);

    InventoryResponse reserve(InventoryOperationRequest request);

    InventoryResponse confirm(InventoryOperationRequest request);

    InventoryResponse release(InventoryOperationRequest request);

    InventoryResponse restore(InventoryOperationRequest request);

    InventoryResponse updateStock(Long productId, UpdateInventoryRequest request);
}

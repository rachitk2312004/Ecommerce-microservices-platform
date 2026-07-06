package com.enterprise.ecommerce.inventory.config;

import com.enterprise.ecommerce.inventory.entity.Inventory;
import com.enterprise.ecommerce.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    public DataSeeder(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || inventoryRepository.count() > 0) {
            return;
        }

        for (long productId = 1; productId <= 6; productId++) {
            inventoryRepository.save(Inventory.builder()
                    .productId(productId)
                    .availableQuantity(100)
                    .reservedQuantity(0)
                    .build());
        }
    }
}

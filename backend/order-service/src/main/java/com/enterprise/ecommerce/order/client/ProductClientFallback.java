package com.enterprise.ecommerce.order.client;

import com.enterprise.ecommerce.order.client.dto.ProductDto;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import com.enterprise.ecommerce.order.exception.ProductUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ApiResponse<ProductDto> getProductById(Long id) {
        log.error("Product service fallback triggered for product id {}", id);
        throw new ProductUnavailableException("Product service is currently unavailable");
    }
}

package com.enterprise.ecommerce.order.service;

import com.enterprise.ecommerce.order.client.ProductClient;
import com.enterprise.ecommerce.order.client.dto.ProductDto;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import com.enterprise.ecommerce.order.exception.ProductUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductIntegrationService {

    private final ProductClient productClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    @Retry(name = "productService")
    public ProductDto getProduct(Long productId) {
        ApiResponse<ProductDto> response = productClient.getProductById(productId);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new ProductUnavailableException("Product not found: " + productId);
        }
        return response.getData();
    }

    private ProductDto getProductFallback(Long productId, Throwable throwable) {
        throw new ProductUnavailableException("Product service unavailable for product " + productId);
    }
}

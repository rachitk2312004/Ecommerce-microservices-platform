package com.enterprise.ecommerce.order.client;

import com.enterprise.ecommerce.order.client.dto.ProductDto;
import com.enterprise.ecommerce.order.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/products", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable("id") Long id);
}

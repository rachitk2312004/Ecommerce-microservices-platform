package com.enterprise.ecommerce.product.service;

import com.enterprise.ecommerce.product.dto.ProductRequest;
import com.enterprise.ecommerce.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<ProductResponse> getProducts(Pageable pageable, boolean includeInactive);

    ProductResponse getProductById(Long id, boolean includeInactive);

    Page<ProductResponse> searchProducts(String name, Pageable pageable, boolean includeInactive);

    List<ProductResponse> getProductsByCategory(Long categoryId, boolean includeInactive);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}

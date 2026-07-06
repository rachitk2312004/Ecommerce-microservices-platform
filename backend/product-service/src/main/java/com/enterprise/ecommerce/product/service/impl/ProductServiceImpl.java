package com.enterprise.ecommerce.product.service.impl;

import com.enterprise.ecommerce.product.dto.ProductRequest;
import com.enterprise.ecommerce.product.dto.ProductResponse;
import com.enterprise.ecommerce.product.entity.Product;
import com.enterprise.ecommerce.product.exception.BadRequestException;
import com.enterprise.ecommerce.product.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.product.mapper.ProductMapper;
import com.enterprise.ecommerce.product.repository.CategoryRepository;
import com.enterprise.ecommerce.product.repository.ProductRepository;
import com.enterprise.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductResponse> getProducts(Pageable pageable, boolean includeInactive) {
        Page<Product> products = includeInactive
                ? productRepository.findAll(pageable)
                : productRepository.findByActiveTrue(pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    public ProductResponse getProductById(Long id, boolean includeInactive) {
        Product product = findProduct(id, includeInactive);
        return productMapper.toResponse(product);
    }

    @Override
    public Page<ProductResponse> searchProducts(String name, Pageable pageable, boolean includeInactive) {
        Page<Product> products = includeInactive
                ? productRepository.findByNameContainingIgnoreCase(name, pageable)
                : productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId, boolean includeInactive) {
        validateCategoryExists(categoryId);
        List<Product> products = includeInactive
                ? productRepository.findByCategoryId(categoryId)
                : productRepository.findByCategoryIdAndActiveTrue(categoryId);
        return products.stream().map(productMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        validateCategoryExists(request.getCategoryId());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id, true);
        validateCategoryExists(request.getCategoryId());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategoryId(request.getCategoryId());
        product.setImageUrl(request.getImageUrl());

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id, true);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findProduct(Long id, boolean includeInactive) {
        if (includeInactive) {
            return productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        }
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BadRequestException("Category not found with id: " + categoryId);
        }
    }
}

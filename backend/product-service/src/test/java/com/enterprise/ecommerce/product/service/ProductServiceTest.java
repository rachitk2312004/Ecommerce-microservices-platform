package com.enterprise.ecommerce.product.service;

import com.enterprise.ecommerce.product.dto.ProductRequest;
import com.enterprise.ecommerce.product.dto.ProductResponse;
import com.enterprise.ecommerce.product.entity.Product;
import com.enterprise.ecommerce.product.exception.BadRequestException;
import com.enterprise.ecommerce.product.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.product.mapper.ProductMapper;
import com.enterprise.ecommerce.product.repository.CategoryRepository;
import com.enterprise.ecommerce.product.repository.ProductRepository;
import com.enterprise.ecommerce.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductMapper productMapper;
    @InjectMocks private ProductServiceImpl productService;

    private ProductRequest productRequest;
    private Product product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest();
        productRequest.setName("Wireless Headphones");
        productRequest.setDescription("Noise-cancelling headphones");
        productRequest.setPrice(new BigDecimal("149.99"));
        productRequest.setCategoryId(1L);
        productRequest.setImageUrl("https://example.com/images/headphones.jpg");

        product = Product.builder()
                .id(1L)
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .categoryId(productRequest.getCategoryId())
                .imageUrl(productRequest.getImageUrl())
                .active(true)
                .build();

        productResponse = ProductResponse.builder()
                .id(1L)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategoryId())
                .imageUrl(product.getImageUrl())
                .active(true)
                .build();
    }

    @Test
    void createProduct_success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.createProduct(productRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Wireless Headphones", result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_invalidCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> productService.createProduct(productRequest));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_success() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.getProductById(1L, false);

        assertEquals(1L, result.getId());
        assertEquals("Wireless Headphones", result.getName());
    }

    @Test
    void getProductById_notFound() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L, false));
    }

    @Test
    void searchProducts_success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue("headphones", pageable))
                .thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        Page<ProductResponse> result = productService.searchProducts("headphones", pageable, false);

        assertEquals(1, result.getTotalElements());
        assertEquals("Wireless Headphones", result.getContent().get(0).getName());
    }
}

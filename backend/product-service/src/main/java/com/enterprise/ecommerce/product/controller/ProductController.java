package com.enterprise.ecommerce.product.controller;

import com.enterprise.ecommerce.product.dto.ApiResponse;
import com.enterprise.ecommerce.product.dto.ProductRequest;
import com.enterprise.ecommerce.product.dto.ProductResponse;
import com.enterprise.ecommerce.product.enums.Role;
import com.enterprise.ecommerce.product.security.AuthenticatedUser;
import com.enterprise.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List products (paginated)")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        boolean isAdmin = isAdmin(user);
        return ResponseEntity.ok(ApiResponse.success(productService.getProducts(pageable, isAdmin)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        boolean isAdmin = isAdmin(user);
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id, isAdmin)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        boolean isAdmin = isAdmin(user);
        return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(name, pageable, isAdmin)));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long categoryId) {
        boolean isAdmin = isAdmin(user);
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByCategory(categoryId, isAdmin)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product (admin)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a product (admin)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a product (admin)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated", null));
    }

    private boolean isAdmin(AuthenticatedUser user) {
        return user != null && Role.ADMIN.name().equals(user.getRole());
    }
}

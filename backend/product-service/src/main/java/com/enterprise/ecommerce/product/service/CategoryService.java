package com.enterprise.ecommerce.product.service;

import com.enterprise.ecommerce.product.dto.CategoryRequest;
import com.enterprise.ecommerce.product.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getCategories(boolean includeInactive);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}

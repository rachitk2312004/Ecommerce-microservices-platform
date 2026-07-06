package com.enterprise.ecommerce.product.mapper;

import com.enterprise.ecommerce.product.dto.CategoryResponse;
import com.enterprise.ecommerce.product.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .build();
    }
}

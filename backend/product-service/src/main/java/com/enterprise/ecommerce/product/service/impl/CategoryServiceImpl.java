package com.enterprise.ecommerce.product.service.impl;

import com.enterprise.ecommerce.product.dto.CategoryRequest;
import com.enterprise.ecommerce.product.dto.CategoryResponse;
import com.enterprise.ecommerce.product.entity.Category;
import com.enterprise.ecommerce.product.exception.BadRequestException;
import com.enterprise.ecommerce.product.exception.DuplicateResourceException;
import com.enterprise.ecommerce.product.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.product.mapper.CategoryMapper;
import com.enterprise.ecommerce.product.repository.CategoryRepository;
import com.enterprise.ecommerce.product.repository.ProductRepository;
import com.enterprise.ecommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getCategories(boolean includeInactive) {
        List<Category> categories = includeInactive
                ? categoryRepository.findAll()
                : categoryRepository.findByActiveTrue();
        return categories.stream().map(categoryMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category already exists with name: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategory(id);

        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Category already exists with name: " + request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategory(id);

        if (productRepository.existsByCategoryId(id)) {
            throw new BadRequestException("Cannot delete category with associated products");
        }

        category.setActive(false);
        categoryRepository.save(category);
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}

package com.tracker.expense.service;

import com.tracker.expense.dto.ExpenseDtos.CategoryRequest;
import com.tracker.expense.dto.ExpenseDtos.CategoryResponse;
import com.tracker.expense.entity.Category;
import com.tracker.expense.exception.ApiExceptions;
import com.tracker.expense.repository.CategoryRepository;
import com.tracker.expense.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAll() {
        return categoryRepository.findByUserId(CurrentUser.id()).stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder()
                .userId(CurrentUser.id())
                .name(request.name())
                .type(request.type())
                .icon(request.icon() != null ? request.icon() : "💰")
                .build();
        return toResponse(categoryRepository.save(category));
    }

    public void delete(UUID id) {
        Category category = categoryRepository.findByIdAndUserId(id, CurrentUser.id())
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("Category not found: " + id));
        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getType(), c.getIcon());
    }
}

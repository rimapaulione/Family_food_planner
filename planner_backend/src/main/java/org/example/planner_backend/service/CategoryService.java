package org.example.planner_backend.service;


import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.category.CategoryResponseDto;
import org.example.planner_backend.mapper.CategoryMapper;
import org.example.planner_backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDto> getAll() {

        return categoryRepository.findAll().stream().map(categoryMapper::toResponse).toList();
    }
}

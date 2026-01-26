package org.example.category;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'active'")
    public List<CategoryResponseDto> getActiveCategories() {
        log.info(">>> [CACHE] MISS - Fetching active categories from DB");
        return categoryRepository.findAllActive().stream()
                .map(CategoryResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDto createCategory(CategoryResponseDto dto) {
        log.info(">>> [CACHE] Evicting 'categories' cache for createCategory");
        Category category = Category.builder()
                .name(dto.getName())
                .displayOrder(dto.getDisplayOrder())
                .isActive("1")
                .build();
        return CategoryResponseDto.fromEntity(categoryRepository.save(category));
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDto updateCategory(Long id, CategoryResponseDto dto) {
        log.info(">>> [CACHE] Evicting 'categories' cache for updateCategory. categoryId={}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.setName(dto.getName());
        category.setDisplayOrder(dto.getDisplayOrder());
        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : "1");
        return CategoryResponseDto.fromEntity(categoryRepository.save(category));
    }
}

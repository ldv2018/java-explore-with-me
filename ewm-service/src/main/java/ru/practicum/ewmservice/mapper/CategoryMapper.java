package ru.practicum.ewmservice.mapper;

import ru.practicum.ewmservice.dto.CategoryDto;
import ru.practicum.ewmservice.model.Category;

public class CategoryMapper {
    public static Category toCategory(CategoryDto categoryDto) {
        return Category.builder()
                .name(categoryDto.getName())
                .build();
    }
}

package ru.practicum.ewmservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.dto.CategoryDto;
import ru.practicum.ewmservice.mapper.CategoryMapper;
import ru.practicum.ewmservice.model.Category;
import ru.practicum.ewmservice.service.CategoryService;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    //admin path
    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Category add(@RequestBody @Validated CategoryDto categoryDto) {
        log.info("Request for add Category: {}", categoryDto);
        Category category = CategoryMapper.toCategory(categoryDto);

        return categoryService.add(category);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int catId) {
        log.info("Request for delete category id = {}", catId);
        categoryService.delete(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public Category update(@RequestBody @Validated CategoryDto categoryDto,
                           @PathVariable int catId) {
        log.info("Request for patch category id = {}", catId);
        Category category = CategoryMapper.toCategory(categoryDto);
        category.setId(catId);

        return categoryService.update(catId, category);
    }

    //public path
    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<Category> getAll(@RequestParam(required = false, defaultValue = "0") @Min(0) int from,
                                 @RequestParam(required = false, defaultValue = "10") @Min(1) int size) {
        log.info("Request for get all categories, from = {}, size = {}", from, size);

        return categoryService.findAll(from, size);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public Category getById(@PathVariable int catId) {
        return categoryService.findById(catId);
    }
}

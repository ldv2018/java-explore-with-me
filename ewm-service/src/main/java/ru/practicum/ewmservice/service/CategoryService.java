package ru.practicum.ewmservice.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.model.Category;
import ru.practicum.ewmservice.storage.CategoryRepository;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryService {
    final CategoryRepository categoryRepository;

    public Category add(Category category) {
        return categoryRepository.save(category);
    }

    public void delete(int id) {
        throwIfCategoryNotExist(id);
        categoryRepository.deleteById(id);
    }

    public Category update(int id, Category category) {
        throwIfCategoryNotExist(id);

        return categoryRepository.save(category);
    }

    public List<Category> findAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return categoryRepository.findAll(pageable)
                .getContent();
    }

    public Category findById(int id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND,
                        "Category with id=" + id + " was not found"));
    }

    private void throwIfCategoryNotExist(int id) {
        if (!categoryRepository.existsById(id)) {
            log.info("Category id={} not found", id);
            throw new NotFoundException(HttpStatus.NOT_FOUND, "Category with id=" + id + " was not found");
        }
    }
}

package ru.practicum.ewmservice.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewmservice.model.Compilation;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Integer> {

    Page<Compilation> findAllByPinnedEquals(Boolean pinned, Pageable pageable);
}

package ru.practicum.ewmservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmservice.model.CompilationToEvent;

import java.util.List;

public interface CompilationToEventRepository extends JpaRepository<CompilationToEvent, Integer> {

    List<CompilationToEvent> findAllByCompilationIdEqualsAndEventIdIn(int compId, List<Integer> eventIds);
}



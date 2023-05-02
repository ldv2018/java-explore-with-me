package ru.practicum.ewmservice.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.model.Compilation;
import ru.practicum.ewmservice.model.CompilationToEvent;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.storage.CompilationRepository;
import ru.practicum.ewmservice.storage.CompilationToEventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationService {
    final CompilationRepository compilationRepository;
    final CompilationToEventRepository compilationToEventRepository;

    public Compilation create(Compilation compilation) {
        return compilationRepository.save(compilation);
    }

    public Compilation get(int id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND, "Compilation not found"));
    }

    public void delete(int id) {
        throwIfCompilationNotExist(id);
        compilationRepository.deleteById(id);
    }

    public Compilation update(Compilation newCompilation) {
        Compilation compilation = get(newCompilation.getId());

        Compilation.CompilationBuilder newCompilationBuilder = compilation.toBuilder();

        if (newCompilation.getTitle() != null
                && !compilation.getTitle().equals(newCompilation.getTitle())) {
            newCompilationBuilder.title(newCompilation.getTitle());
        }

        if (newCompilation.getPinned() != null
                && !compilation.getPinned().equals(newCompilation.getPinned())) {
            newCompilationBuilder.pinned(newCompilation.getPinned());
        }

        if (newCompilation.getEvents() != null
                && !compilation.getEvents().equals(newCompilation.getEvents())) {

            List<Integer> oldEventIds = compilation.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());
            List<Integer> newEventIds = newCompilation.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());

            List<Integer> addedEventIds = newEventIds.stream()
                    .filter(id -> !oldEventIds.contains(id))
                    .collect(Collectors.toList());
            List<Integer> removedEventIds = oldEventIds.stream()
                    .filter(id -> !newEventIds.contains(id))
                    .collect(Collectors.toList());

            List<CompilationToEvent> addedEventLinks = addedEventIds.stream()
                    .map(eventId -> CompilationToEvent.builder()
                            .compilationId(compilation.getId())
                            .eventId(eventId)
                            .build())
                    .collect(Collectors.toList());
            compilationToEventRepository.saveAll(addedEventLinks);

            List<CompilationToEvent> removedEventLinks = compilationToEventRepository
                    .findAllByCompilationIdEqualsAndEventIdIn(
                            compilation.getId(), removedEventIds
                    );
            compilationToEventRepository.deleteAll(removedEventLinks);

            newCompilationBuilder.events(null);
        }

        return compilationRepository.save(newCompilationBuilder.build());
    }

    public Compilation getById(int compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(HttpStatus.NOT_FOUND, "Compilation not found"));
    }

    public Page<Compilation> getAll(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return pinned != null
                ? compilationRepository.findAllByPinnedEquals(pinned, pageable)
                : compilationRepository.findAll(pageable);
    }

    private void throwIfCompilationNotExist(int id) {
        if (!compilationRepository.existsById(id)) {
            throw new NotFoundException(HttpStatus.NOT_FOUND, "Compilation not found");
        }
    }
}

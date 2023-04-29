package ru.practicum.ewmservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.dto.CompilationRequestDto;
import ru.practicum.ewmservice.dto.CompilationResponseDto;
import ru.practicum.ewmservice.mapper.CompilationMapper;
import ru.practicum.ewmservice.model.Compilation;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.service.CompilationService;
import ru.practicum.ewmservice.service.EventService;

import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewmservice.mapper.CompilationMapper.toCompilation;
import static ru.practicum.ewmservice.mapper.CompilationMapper.toCompilationResponseDto;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationController {
    final CompilationService compilationService;
    final EventService eventService;

//Admin path start
    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationResponseDto add(@RequestBody @Validated CompilationRequestDto compilationRequestDto) {
        List<Integer> eventIds = Optional.ofNullable(compilationRequestDto.getEvents())
                .orElse(Collections.emptyList());
        List<Event> events = eventIds.size() > 0
                ? eventService.getAllByIds(eventIds)
                : Collections.emptyList();
        Compilation compilation = toCompilation(compilationRequestDto).toBuilder()
                .events(events)
                .build();

        return toCompilationResponseDto(compilationService.create(compilation));
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int compId) {
        compilationService.delete(compId);
    }

    @PatchMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationResponseDto update(
            @PathVariable int compId,
            @RequestBody(required = false) CompilationRequestDto compilationRequestDto
    ) {
        log.info("ADMIN PATCH");
        log.info("Request for update compilation: {}", compilationRequestDto);
        if (compilationRequestDto == null) {
            return toCompilationResponseDto(compilationService.get(compId));
        }
        List<Integer> eventIds = Optional.of(compilationRequestDto)
                .map(CompilationRequestDto::getEvents)
                .orElse(Collections.emptyList());
        List<Event> events = eventIds.size() > 0
                ? eventService.getAllByIds(eventIds)
                : Collections.emptyList();

        Compilation compilation = toCompilation(compilationRequestDto).toBuilder()
                .id(compId)
                .events(events)
                .build();

        return toCompilationResponseDto(compilationService.update(compilation));
    }
//Admin path end

//Public path start
    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationResponseDto> getById(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0", required = false) @Min(0) int from,
            @RequestParam(defaultValue = "10", required = false) @Min(1) int size
    ) {
        return compilationService.getAll(pinned, from, size).stream()
                .map(CompilationMapper::toCompilationResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationResponseDto getById(@PathVariable int compId) {
        return toCompilationResponseDto(compilationService.getById(compId));
    }
//Public path end
}

package ru.practicum.ewmservice.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.ewmservice.dto.CompilationRequestDto;
import ru.practicum.ewmservice.dto.CompilationResponseDto;
import ru.practicum.ewmservice.model.Compilation;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor
public class CompilationMapper {
    public static Compilation toCompilation(CompilationRequestDto compilationRequestDto) {
        return Compilation.builder()
                .title(compilationRequestDto.getTitle())
                .pinned(Optional.ofNullable(compilationRequestDto.getPinned())
                        .orElse(false))
                .build();
    }

    public static CompilationResponseDto toCompilationResponseDto(Compilation compilation) {
        return CompilationResponseDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(Optional.ofNullable(compilation.getEvents())
                        .map(list -> list.stream()
                                .map(EventMapper::toEventResponseDto)
                                .collect(Collectors.toList())
                        )
                        .orElse(Collections.emptyList()))
                .build();
    }
}

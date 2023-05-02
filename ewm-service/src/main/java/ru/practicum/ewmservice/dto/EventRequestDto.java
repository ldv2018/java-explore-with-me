package ru.practicum.ewmservice.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestDto {
    @NotNull
    String annotation;
    @NotNull
    int category;
    @NotNull
    String description;
    @NotNull
    String eventDate;
    @NotNull
    LocationDto location;
    @NotNull
    boolean paid;
    @NotNull
    @Positive
    int participantLimit;
    @NotNull
    boolean requestModeration;
    @NotNull
    String title;
}
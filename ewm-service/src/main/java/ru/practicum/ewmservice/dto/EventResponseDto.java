package ru.practicum.ewmservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewmservice.model.Category;
import ru.practicum.ewmservice.model.State;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class EventResponseDto {
    int id;
    String annotation;
    Category category;
    String description;
    int confirmedRequests;
    String createdOn;
    String eventDate;
    boolean paid;
    int participantLimit;
    boolean requestModeration;
    String publishedOn;
    String title;
    UserResponseDto initiator;
    LocationDto location;
    State state;
    Integer views;
}

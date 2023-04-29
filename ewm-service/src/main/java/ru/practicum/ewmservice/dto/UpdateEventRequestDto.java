package ru.practicum.ewmservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewmservice.model.UpdateEventStateAction;

import javax.validation.constraints.Positive;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventRequestDto {
    String title;
    String description;
    String annotation;
    String eventDate;
    LocationDto location;
    @Positive
    Integer participantLimit;
    Boolean paid;
    Boolean requestModeration;
    Integer category;
    UpdateEventStateAction stateAction;
}

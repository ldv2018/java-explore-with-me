package ru.practicum.ewmservice.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewmservice.model.ParticipationRequestState;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestDto {
    @Positive
    Integer id;
    @NotBlank
    private String created;
    @NotNull
    ParticipationRequestState status;
    @Positive
    Integer event;
    @Positive
    Integer requester;
}

package ru.practicum.ewmservice.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestStatusUpdateResponseDto {
    @NotEmpty
    List<ParticipationRequestDto> confirmedRequests;
    @NotEmpty
    List<ParticipationRequestDto> rejectedRequests;
}

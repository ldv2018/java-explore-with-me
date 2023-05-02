package ru.practicum.ewmservice.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.ewmservice.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.model.ParticipationRequest;

import static ru.practicum.ewmservice.util.DateTimeUtils.format;

@NoArgsConstructor
public class ParticipationRequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest pr) {
        return ParticipationRequestDto.builder()
                .id(pr.getId())
                .created(format(pr.getCreatedOn()))
                .status(pr.getStatus())
                .event(pr.getEvent().getId())
                .requester(pr.getRequester().getId())
                .build();
    }
}
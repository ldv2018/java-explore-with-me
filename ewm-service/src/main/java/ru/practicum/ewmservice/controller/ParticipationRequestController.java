package ru.practicum.ewmservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.dto.ParticipationRequestStatusUpdateRequestDto;
import ru.practicum.ewmservice.dto.ParticipationRequestStatusUpdateResponseDto;
import ru.practicum.ewmservice.exception.BadRequestException;
import ru.practicum.ewmservice.mapper.ParticipationRequestMapper;
import ru.practicum.ewmservice.model.Event;
import ru.practicum.ewmservice.model.ParticipationRequest;
import ru.practicum.ewmservice.model.User;
import ru.practicum.ewmservice.service.EventService;
import ru.practicum.ewmservice.service.ParticipationRequestService;
import ru.practicum.ewmservice.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewmservice.dto.ParticipationRequestStatusUpdateRequestDto.ParticipationRequestStatusUpdateAction.CONFIRMED;
import static ru.practicum.ewmservice.dto.ParticipationRequestStatusUpdateRequestDto.ParticipationRequestStatusUpdateAction.REJECTED;
import static ru.practicum.ewmservice.mapper.ParticipationRequestMapper.toParticipationRequestDto;
import static ru.practicum.ewmservice.dto.ParticipationRequestStatusUpdateRequestDto.ParticipationRequestStatusUpdateAction;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestController {
    final UserService userService;
    final EventService eventService;
    final ParticipationRequestService participationRequestService;

    @GetMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllByRequester(@PathVariable int userId) {
        userService.findById(userId);

        return participationRequestService.getAllByRequester(userId).stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllByInitiator(@PathVariable int userId,
                                                           @PathVariable int eventId) {
        userService.findById(userId);

        return participationRequestService.getAllForEventIdByInitiator(null, eventId, userId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable int userId,
                                          @RequestParam int eventId) {
        Event event = eventService.get(eventId);
        User requester = userService.findById(userId);
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(requester)
                .event(event)
                .build();

        return toParticipationRequestDto(participationRequestService.create(participationRequest));
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelEventRequest(@PathVariable int userId,
                                                      @PathVariable int requestId) {
        return toParticipationRequestDto(participationRequestService
                .cancelParticipationRequest(requestId, userId));
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestStatusUpdateResponseDto statusUpdate(
            @PathVariable int userId,
            @PathVariable int eventId,
            @RequestBody ParticipationRequestStatusUpdateRequestDto participationRequestStatusUpdateRequestDto) {
        List<Integer> requestIds = participationRequestStatusUpdateRequestDto.getRequestIds();
        ParticipationRequestStatusUpdateResponseDto
                .ParticipationRequestStatusUpdateResponseDtoBuilder<?, ?> responseBuilder
                = ParticipationRequestStatusUpdateResponseDto.builder();

        ParticipationRequestStatusUpdateAction newStatus = participationRequestStatusUpdateRequestDto.getStatus();
        if (CONFIRMED.equals(newStatus)) {
            List<ParticipationRequestDto> confirmedParticipationRequests =
                    participationRequestService.confirmParticipationRequest(requestIds, eventId, userId).stream()
                            .map(ParticipationRequestMapper::toParticipationRequestDto)
                            .collect(Collectors.toList());
            responseBuilder.confirmedRequests(confirmedParticipationRequests);
        } else if (REJECTED.equals(newStatus)) {
            List<ParticipationRequestDto> rejectedParticipationRequests =
                    participationRequestService.rejectParticipationRequest(requestIds, eventId, userId).stream()
                            .map(ParticipationRequestMapper::toParticipationRequestDto)
                            .collect(Collectors.toList());

            responseBuilder.rejectedRequests(rejectedParticipationRequests);
        } else {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Unexpected status: " + newStatus);
        }

        return responseBuilder.build();
    }
}

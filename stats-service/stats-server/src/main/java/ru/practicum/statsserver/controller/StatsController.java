package ru.practicum.statsserver.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.Stat;
import ru.practicum.statsserver.mapper.HitMapper;
import ru.practicum.statsserver.model.Hit;
import ru.practicum.statsserver.service.StatsService;

import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class StatsController {

    final StatsService statsService;

    @PostMapping(path = "/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitDto add(@RequestBody @Validated HitDto hitDto) {
        Hit hit = HitMapper.toHit(hitDto);
        Hit savedHit = statsService.add(hit);
        log.info("Save hit {}", savedHit);

        return HitMapper.toHitDto(savedHit);
    }

    @GetMapping(path = "/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<Stat> get(@RequestParam String start,
                          @RequestParam String end,
                          @RequestParam(required = false) List<String> uris,
                          @RequestParam(required = false) boolean unique) {
        log.info("Get statistic for start = {}, end = {}, uris = {}, unique = {}", start, end, uris, unique);

        return statsService.get(start, end, uris, unique);
    }
}
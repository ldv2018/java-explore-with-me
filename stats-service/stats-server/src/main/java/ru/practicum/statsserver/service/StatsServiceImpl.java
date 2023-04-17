package ru.practicum.statsserver.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.statsdto.Stat;
import ru.practicum.statsserver.exception.BadRequestException;
import ru.practicum.statsserver.model.Hit;
import ru.practicum.statsserver.storage.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class StatsServiceImpl implements StatsService {

    final StatsRepository repository;
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Hit add(Hit hit) {
        return repository.save(hit);
    }

    @Override
    public List<Stat> get(String start, String end, List<String> uris, boolean unique) {
        LocalDateTime startFormatted = LocalDateTime.parse(start, dateTimeFormatter);
        LocalDateTime endFormatted = LocalDateTime.parse(end, dateTimeFormatter);
        throwIfDateTimeNotValid(startFormatted, endFormatted);
        List<Stat> statistic;
        if (uris == null) {
            uris = new ArrayList<>();
        }
        if (unique) {
            log.info("Find statistic unique, uri = {}", uris);
            statistic = uris.isEmpty() ?
                    repository.countHitsUnique(startFormatted, endFormatted) :
                    repository.countHitsUnique(startFormatted, endFormatted, uris);
        } else {
            log.info("Find statistic non unique, uri = {}", uris);
            statistic = uris.isEmpty() ?
                    repository.countHits(startFormatted, endFormatted) :
                    repository.countHits(startFormatted, endFormatted, uris);
        }

        return statistic;
    }

    private void throwIfDateTimeNotValid(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            log.info("end time {} is before start {}", end, start);
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "wrong times value for statistics");
        }
    }
}

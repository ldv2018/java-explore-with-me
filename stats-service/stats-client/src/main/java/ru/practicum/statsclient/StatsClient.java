package ru.practicum.statsclient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.Stat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsClient {
    @Value("${stats-server.uri}")
    String local;
    final RestTemplate restTemplate = new RestTemplate();
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void saveStat(HitDto hitDto) {
        log.info("Request for save {}", hitDto);
        restTemplate.postForLocation(local + "/hit", hitDto);
    }

    public List<Stat> getStat(LocalDateTime start,
                              LocalDateTime end,
                              List<String> uris,
                              boolean unique) {
        String startFormatted = start.format(dateTimeFormatter);
        String endFormatted = end.format(dateTimeFormatter);
        log.info("Request for get statistic from {} to {}, unique - {}",
                startFormatted,
                endFormatted,
                unique);
        ResponseEntity<Stat[]> stats = restTemplate.getForEntity(local + "/stats?start=" + startFormatted +
                "&end=" + endFormatted + "&uris=" + uris + "&unique=" + unique, Stat[].class);

        return Arrays.asList(Objects.requireNonNull(stats.getBody()));
    }
}

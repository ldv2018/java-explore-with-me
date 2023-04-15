package ru.practicum.statsserver.mapper;

import lombok.NoArgsConstructor;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsserver.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
public class HitMapper {
    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Hit toHit(HitDto hitDto) {
        return Hit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), dateTimeFormatter))
                .build();
    }

    public static HitDto toHitDto(Hit hit) {
        return HitDto.builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp().format(dateTimeFormatter))
                .build();
    }
}

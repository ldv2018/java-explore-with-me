package ru.practicum.ewmservice.mapper;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.practicum.ewmservice.dto.LocationDto;
import ru.practicum.ewmservice.model.Location;

@NoArgsConstructor
public class LocationMapper {
    public static Location toLocation(@NonNull LocationDto locationDto) {
        return Location.builder()
                .lat(locationDto.getLat())
                .lon(locationDto.getLon())
                .build();
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLon(), location.getLat());
    }
}

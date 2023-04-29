package ru.practicum.ewmservice.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.model.Location;
import ru.practicum.ewmservice.storage.LocationRepository;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationService {

    final LocationRepository locationRepository;

    public Location add(Location location) {
        return locationRepository.save(location);
    }
}

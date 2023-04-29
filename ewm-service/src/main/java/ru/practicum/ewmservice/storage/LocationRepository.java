package ru.practicum.ewmservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewmservice.model.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
}

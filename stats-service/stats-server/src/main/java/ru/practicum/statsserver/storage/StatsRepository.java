package ru.practicum.statsserver.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.statsdto.Stat;
import ru.practicum.statsserver.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Hit, Integer> {

    @Query(name = "GetNotUniqueIpNoUriStat", nativeQuery = true)
    List<Stat> countHits(LocalDateTime start, LocalDateTime end);

    @Query(name = "GetNotUniqueIpStat", nativeQuery = true)
    List<Stat> countHits(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(name = "GetUniqueIpNoUriStat", nativeQuery = true)
    List<Stat> countHitsUnique(LocalDateTime start, LocalDateTime end);

    @Query(name = "GetUniqueIpStat", nativeQuery = true)
    List<Stat> countHitsUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(name = "GetWithoutTime", nativeQuery = true)
    List<Stat> countHitsWithoutTime(List<String> uris);
}

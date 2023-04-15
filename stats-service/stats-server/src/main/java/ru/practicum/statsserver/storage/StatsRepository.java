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

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(h.ip) AS hits " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp BETWEEN ?1 AND ?2) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<Stat> countHits(LocalDateTime start, LocalDateTime end);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(h.ip) AS hits " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp BETWEEN  ?1 AND ?2) AND h.uri in ?3 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<Stat> countHits(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp BETWEEN ?1 AND ?2) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<Stat> countHitsUnique(LocalDateTime start, LocalDateTime end);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits " +
            "FROM Hit AS h " +
            "WHERE (h.timestamp BETWEEN ?1 AND ?2) AND h.uri in ?3 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hits DESC")
    List<Stat> countHitsUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

}

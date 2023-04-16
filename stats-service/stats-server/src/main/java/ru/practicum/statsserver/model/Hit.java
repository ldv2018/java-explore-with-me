package ru.practicum.statsserver.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.statsdto.Stat;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "hits", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Entity
@NamedNativeQueries({
        @NamedNativeQuery(name = "GetNotUniqueIpStat", resultSetMapping = "HitToDtoMapping",
                query = "SELECT h.app AS app, h.uri AS uri, COUNT(h.ip) AS hits " +
                        "FROM hits AS h " +
                        "WHERE (h.req_time BETWEEN  :start AND :end) AND h.uri in :uris " +
                        "GROUP BY h.app, h.uri " +
                        "ORDER BY hits DESC"
        ),
        @NamedNativeQuery(name = "GetUniqueIpStat", resultSetMapping = "HitToDtoMapping",
                query = "SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits " +
                        "FROM hits AS h " +
                        "WHERE (h.req_time BETWEEN :start AND :end) AND h.uri in :uris " +
                        "GROUP BY h.app, h.uri " +
                        "ORDER BY hits DESC"
        ),
        @NamedNativeQuery(name = "GetNotUniqueIpNoUriStat", resultSetMapping = "HitToDtoMapping",
                query = "SELECT h.app AS app, h.uri AS uri, COUNT(h.ip) AS hits " +
                        "FROM hits AS h " +
                        "WHERE (h.req_time BETWEEN :start AND :end) " +
                        "GROUP BY h.app, h.uri " +
                        "ORDER BY hits DESC"
        ),
        @NamedNativeQuery(name = "GetUniqueIpNoUriStat", resultSetMapping = "HitToDtoMapping",
                query = "SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits " +
                        "FROM hits AS h " +
                        "WHERE (h.req_time BETWEEN :start AND :end) " +
                        "GROUP BY h.app, h.uri " +
                        "ORDER BY hits DESC"
        )
})
@SqlResultSetMapping(name = "HitToDtoMapping",
        classes = {
                @ConstructorResult(
                        targetClass = Stat.class,
                        columns = {
                                @ColumnResult(name = "app", type = String.class),
                                @ColumnResult(name = "uri", type = String.class),
                                @ColumnResult(name = "hits", type = Integer.class)
                        }
                )}
)
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hit_id")
    int id;
    String app;
    String uri;
    String ip;
    @Column(name = "req_time")
    LocalDateTime timestamp;
}

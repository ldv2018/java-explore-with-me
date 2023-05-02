package ru.practicum.ewmservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "compilations", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compilation_id")
    int id;
    Boolean pinned;
    String title;
    @ManyToMany
    @JoinTable(
            name = "events_to_compilations",
            joinColumns = @JoinColumn(name = "compilation_id", referencedColumnName = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id", referencedColumnName = "event_id")
    )
    List<Event> events;
}

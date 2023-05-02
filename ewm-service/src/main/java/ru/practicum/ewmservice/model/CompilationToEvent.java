package ru.practicum.ewmservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@Table(name = "events_to_compilations", schema = "public")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationToEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(name = "event_id", nullable = false)
    Integer eventId;
    @Column(name = "compilation_id", nullable = false)
    Integer compilationId;

}
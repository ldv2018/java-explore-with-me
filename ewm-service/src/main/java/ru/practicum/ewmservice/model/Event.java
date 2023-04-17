package ru.practicum.ewmservice.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "events", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    int id;
    @Column(name = "category_id")
    int category;
    @Column(name = "confirmed_request")
    int confirmedRequest;
    LocalDateTime created;
    String description;
    String annotation;
    @Column(name = "event_date")
    LocalDateTime eventDate;
    int initiator;
    int location;
    boolean paid;
    @Column(name = "participant_limit")
    int participantLimit;
    @Column(name = "published_on")
    LocalDateTime publishedOn;
    @Column(name ="request_moderation")
    boolean requestModeration;
    @Enumerated
    State state;
    String title;
    int views;
}

package ru.practicum.ewmservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "participation_request", schema = "public")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_request_id")
    Integer id;
    @Column(name = "created", nullable = false, updatable = false)
    LocalDateTime createdOn;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    ParticipationRequestState status = ParticipationRequestState.PENDING;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    Event event;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester", nullable = false)
    User requester;
}
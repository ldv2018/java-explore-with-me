package ru.practicum.ewmservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    int id;
    String name;
}

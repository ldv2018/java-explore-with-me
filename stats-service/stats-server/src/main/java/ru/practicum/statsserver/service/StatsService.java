package ru.practicum.statsserver.service;

import ru.practicum.statsdto.Stat;
import ru.practicum.statsserver.model.Hit;

import java.util.List;

public interface StatsService {

    Hit add(Hit hit);

    List<Stat> get(String start,
                   String end,
                   List<String> uris,
                   boolean unique);
}

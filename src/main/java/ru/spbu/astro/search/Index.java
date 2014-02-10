package ru.spbu.astro.search;

import ru.spbu.astro.model.Point;

public interface Index {
    int getNearestNeighbor(final Point p);
}

package ru.spbu.astro.db;

import ru.spbu.astro.model.Point;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PointDepot {
    Point get(int id);

    Map<Integer, Point> get(Collection<Integer> ids);

    int add(Point p);

    List<Integer> add(List<Point> points);

    void drop();
}

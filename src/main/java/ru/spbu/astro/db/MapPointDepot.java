package ru.spbu.astro.db;

import ru.spbu.astro.model.Point;

import java.util.*;

public final class MapPointDepot implements PointDepot {

    private final Map<Integer, Point> id2point = new HashMap<>();
    private int maxId = -1;

    @Override
    public Point get(int id) {
        return id2point.get(id);
    }

    @Override
    public Map<Integer, Point> get(final Iterable<Integer> ids) {
        final Map<Integer, Point> id2point = new HashMap<>();
        for (int id : ids) {
            id2point.put(id, get(id));
        }
        return id2point;
    }

    @Override
    public int add(final Point p) {
        id2point.put(maxId + 1, p);
        maxId++;
        return maxId;
    }

    @Override
    public List<Integer> add(final Iterable<Point> points) {
        final List<Integer> ids = new ArrayList<>();
        for (final Point p : points) {
            ids.add(add(p));
        }
        return ids;
    }

    @Override
    public void clear() {
        id2point.clear();
        maxId = -1;
    }

}

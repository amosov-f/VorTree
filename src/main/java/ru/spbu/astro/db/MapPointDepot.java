package ru.spbu.astro.db;

import ru.spbu.astro.model.Point;

import java.util.*;

public class MapPointDepot implements PointDepot {
    private HashMap<Integer, Point> id2point = new HashMap();
    private int maxId = -1;

    @Override
    public Point get(int id) {
        return id2point.get(id);
    }

    @Override
    public HashMap<Integer, Point> get(Iterable<Integer> ids) {
        HashMap<Integer, Point> id2point = new HashMap();
        for (int id : ids) {
            id2point.put(id, get(id));
        }
        return null;
    }

    @Override
    public int add(Point p) {
        id2point.put(maxId + 1, p);
        maxId++;
        return maxId;
    }

    @Override
    public ArrayList<Integer> add(Iterable<Point> points) {
        ArrayList<Integer> ids = new ArrayList();
        for (Point p : points) {
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

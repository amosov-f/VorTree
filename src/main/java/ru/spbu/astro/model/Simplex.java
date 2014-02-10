package ru.spbu.astro.model;

import ru.spbu.astro.graphics.Framable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Simplex implements Framable {
    List<Point> vertices = new ArrayList();

    public Simplex(Point... points) {
        this(Arrays.asList(points));
    }

    public Simplex(Collection<Point> points) {
        vertices = new ArrayList(points);
    }

    public List<Point> getVertices() {
        return vertices;
    }

    public Point getCenter() {
        Point center = new Point(vertices.get(0).dim());
        for (Point v : vertices) {
            center = center.add(v);
        }
        return center.multiply(1.0 / vertices.size());
    }

    @Override
    public Rectangle getFrameRectangle() {
        return new Rectangle(vertices);
    }
}

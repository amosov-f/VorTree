package ru.spbu.astro.model;

import ru.spbu.astro.graphics.Framable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class Simplex implements Framable {

    private final List<Point> vertices;

    public Simplex(final Point... points) {
        this(Arrays.asList(points));
    }

    public Simplex(final Collection<Point> points) {
        vertices = new ArrayList<>(points);
    }

    public List<Point> getVertices() {
        return vertices;
    }

    public Point getCenter() {
        Point center = new Point(dim());
        for (final Point v : vertices) {
            center = center.add(v);
        }
        return center.multiply(1.0 / vertices.size());
    }

    public int dim() {
        return vertices.get(0).dim();
    }

    @Override
    public Rectangle getFrameRectangle() {
        return new Rectangle(vertices);
    }

}

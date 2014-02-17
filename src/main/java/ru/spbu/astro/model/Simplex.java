package ru.spbu.astro.model;

import ru.spbu.astro.graphics.Framable;

import java.util.*;

public final class Simplex implements Framable, Iterable<Line> {

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

    @Override
    public Iterator<Line> iterator() {
        final List<Line> lines = new ArrayList<>();
        for (int i = 0; i < vertices.size(); ++i) {
            for (int j = i + 1; j < vertices.size(); ++j) {
                lines.add(new Line(vertices.get(i), vertices.get(j)));
            }
        }
        return lines.iterator();
    }

}

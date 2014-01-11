package ru.spbu.astro.model;

import ru.spbu.astro.model.Point;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Rectangle {
    private Point minVertex;
    private Point maxVertex;

    public Rectangle(final Point[] points) {
        this(Arrays.asList(points));
    }

    public Rectangle(final Collection<Point> points) {
        for (Point p : points) {
            if (minVertex == null) {
                minVertex = p;
            }
            if (maxVertex == null) {
                maxVertex = p;
            }
            minVertex = minVertex.min(p);
            maxVertex = maxVertex.max(p);
        }
    }

    public long getX() {
        return minVertex.get(0);
    }

    public long getY() {
        return minVertex.get(1);
    }

    public long getWidth() {
        return (maxVertex.get(0) - minVertex.get(0));
    }

    public long getHeight() {
        return (maxVertex.get(1) - minVertex.get(1));
    }

    public Point getMinVertex() {
        return minVertex;
    }

    public Point getMaxVertex() {
        return maxVertex;
    }

    public int dim() {
        return minVertex.dim();
    }

    @Override
    public String toString() {
        return "ru.spbu.astro.model.Rectangle(" +
                "minVertex = " + minVertex +
                ", maxVertex = " + maxVertex +
                ')';
    }
}

package ru.spbu.astro.model;

import ru.spbu.astro.model.Point;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Rectangle {
    private Point minVertex;
    private Point maxVertex;

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

    public double getX() {
        return minVertex.get(0);
    }

    public double getY() {
        return minVertex.get(1);
    }

    public double getWidth() {
        return (maxVertex.get(0) - minVertex.get(0));
    }

    public double getHeight() {
        return (maxVertex.get(1) - minVertex.get(1));
    }

    public Point getMinVertex() {
        return minVertex;
    }

    public Point getMaxVertex() {
        return maxVertex;
    }

    @Override
    public String toString() {
        return "ru.spbu.astro.model.Rectangle(" +
                "minVertex = " + minVertex +
                ", maxVertex = " + maxVertex +
                ')';
    }
}

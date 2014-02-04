package ru.spbu.astro.model;

import org.w3c.dom.css.Rect;
import ru.spbu.astro.graphics.Framable;
import ru.spbu.astro.model.Point;

import java.util.*;

public class Rectangle implements Framable {
    private Point minVertex;
    private Point maxVertex;

    public Rectangle(Point... points) {
        this(Arrays.asList(points));
    }

    public Rectangle(Collection<Point> points) {
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

    public Rectangle add(Rectangle rect) {
        return new Rectangle(minVertex.min(rect.getMinVertex()), maxVertex.max(rect.getMaxVertex()));
    }

    public int getX() {
        return minVertex.getX();
    }

    public int getY() {
        return minVertex.getY();
    }

    public int getWidth() {
        return maxVertex.getX() - minVertex.getX();
    }

    public int getHeight() {
        return maxVertex.getY() - minVertex.getY();
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

    public Point getCenter() {
        return minVertex.add(maxVertex).multiply(0.5);
    }

    @Override
    public String toString() {
        return "ru.spbu.astro.model.Rectangle(" +
                "minVertex = " + minVertex +
                ", maxVertex = " + maxVertex +
                ')';
    }

    @Override
    public Rectangle getFrameRectangle() {
        return this;
    }
}

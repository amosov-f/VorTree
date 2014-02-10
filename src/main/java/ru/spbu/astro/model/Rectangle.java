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

    public long getX() {
        return minVertex.getX();
    }

    public long getY() {
        return minVertex.getY();
    }

    public long getWidth() {
        return maxVertex.getX() - minVertex.getX();
    }

    public long getHeight() {
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

    public boolean contains(Point p) {
        if (p.dim() != dim()) {
            return false;
        }
        for (int i = 0; i < p.dim(); ++i) {
            if (p.get(i) < minVertex.get(i) ||  maxVertex.get(i) < p.get(i)) {
                return false;
            }
        }
        return true;
    }

    public long distance2to(Point p) {
        if (contains(p)) {
            return 0;
        }

        long distance2 = 0;
        for (int i = 0; i < p.dim(); ++i) {
            if (minVertex.get(i) <= p.get(i) && p.get(i) <= maxVertex.get(i)) {
                continue;
            }
            distance2 += Math.min(Math.abs(p.get(i) - minVertex.get(i)), Math.abs(p.get(i) - maxVertex.get(i)));
        }
        return distance2;
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

    @Override
    public Object clone() {
        Rectangle rect;
        try {
            rect = (Rectangle) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
        rect.minVertex = (Point) minVertex.clone();
        rect.maxVertex = (Point) maxVertex.clone();
        return rect;
    }
}

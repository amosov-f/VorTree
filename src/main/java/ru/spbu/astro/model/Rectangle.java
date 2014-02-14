package ru.spbu.astro.model;

import com.google.common.collect.Iterables;
import ru.spbu.astro.Message;
import ru.spbu.astro.graphics.Framable;

import java.util.Arrays;

public final class Rectangle implements Framable {
    private final Point minVertex;
    private final Point maxVertex;

    public Rectangle(final Point... points) {
        this(Arrays.asList(points));
    }

    public Rectangle(final Iterable<Point> points) {
        Point minVertex = Iterables.get(points, 0);
        Point maxVertex = Iterables.get(points, 0);
        for (final Point p : points) {
            minVertex = minVertex.min(p);
            maxVertex = maxVertex.max(p);
        }
        this.minVertex = minVertex;
        this.maxVertex = maxVertex;
    }

    public Rectangle(final Rectangle rect) {
        minVertex = rect.minVertex;
        maxVertex = rect.maxVertex;
    }

    public Rectangle add(final Rectangle rect) {
        return new Rectangle(minVertex.min(rect.minVertex), maxVertex.max(rect.maxVertex));
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

    public boolean contains(final Point p) {
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

    public long distance2to(final Point p) {
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
        return "Rectangle(minVertex = " + minVertex + ", maxVertex = " + maxVertex + ")";
    }

    @Override
    public Rectangle getFrameRectangle() {
        return this;
    }

    public Message.Rectangle toMessage() {
        final Message.Rectangle.Builder builder = Message.Rectangle.newBuilder();
        builder.setMinVertex(minVertex.toMessage());
        builder.setMaxVertex(maxVertex.toMessage());
        return builder.build();
    }

    public static Rectangle fromMessage(Message.Rectangle message) {
        return new Rectangle(Point.fromMessage(message.getMinVertex()), Point.fromMessage(message.getMaxVertex()));
    }

}

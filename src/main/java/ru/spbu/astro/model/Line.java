package ru.spbu.astro.model;

import ru.spbu.astro.graphics.Framable;

public class Line implements Framable {
    Point p1;
    Point p2;

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Point getFirst() {
        return p1;
    }

    public Point getSecond() {
        return p2;
    }

    public Point getProjection(Point p) {
        Point del = p2.subtract(p1);
        double t = (double) del.multiply(p.subtract(p1)) / del.multiply(del);
        return p1.add(del.multiply(t));
    }

    @Override
    public Rectangle getFrameRectangle() {
        return new Rectangle(p1, p2);
    }
}

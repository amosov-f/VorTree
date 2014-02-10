package ru.spbu.astro.model;

import ru.spbu.astro.graphics.Framable;

public final class Line implements Framable {
    private final Point p1;
    private final Point p2;

    public Line(final Point p1, final Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Point getFirst() {
        return p1;
    }

    public Point getSecond() {
        return p2;
    }

    public Point getProjection(final Point p) {
        final Point dp = p2.subtract(p1);
        double t = (double) dp.multiply(p.subtract(p1)) / dp.multiply(dp);
        return p1.add(dp.multiply(t));
    }

    @Override
    public Rectangle getFrameRectangle() {
        return new Rectangle(p1, p2);
    }
}

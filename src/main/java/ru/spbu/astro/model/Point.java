package ru.spbu.astro.model;

import com.google.common.primitives.Longs;
import ru.spbu.astro.Message;
import ru.spbu.astro.graphics.Framable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public final class Point implements Iterable<Long>, Framable, Serializable {
    private final long[] coordinates;

    public Point(int dim) {
        coordinates = new long[dim];
    }

    public Point(final long... coordinates) {
        this.coordinates = coordinates.clone();
    }


    public Point(final Point p) {
        this(p.coordinates);
    }

    public Point(final Collection<Long> coordinates) {
        this(Longs.toArray(coordinates));
    }

    public Point(final String[] s) {
        coordinates = new long[s.length];
        for (int i = 0; i < coordinates.length; ++i) {
            coordinates[i] = new Long(s[i]);
        }
    }

    public Point(String s) {
        this(s.substring(1, s.length() - 1).split(", "));
    }

    public long get(int i) {
        return coordinates[i];
    }

    private void set(int i, long val) {
        coordinates[i] = val;
    }

    public int dim() {
        return coordinates.length;
    }

    public long distance2to(final Point p) {
        return subtract(p).sqr();
    }

    public long sqr() {
        return multiply(this);
    }

    public Point min(final Point other) {
        final Point min = new Point(dim());
        for (int i = 0; i < dim(); ++i) {
            min.set(i, Math.min(get(i), other.get(i)));
        }
        return min;
    }

    public Point max(final Point other) {
        final Point max = new Point(dim());
        for (int i = 0; i < dim(); ++i) {
            max.set(i, Math.max(get(i), other.get(i)));
        }
        return max;
    }

    public long getX() {
        if (dim() > 0) {
            return (int) get(0);
        }
        return 0;
    }

    public long getY() {
        if (dim() > 1) {
            return (int) get(1);
        }
        return 0;
    }

    public Point fill(long val) {
        Point p = new Point(dim());
        for (int i = 0; i < p.dim(); ++i) {
            p.set(i, val);
        }
        return p;
    }

    public Point add(final Point p) throws IllegalArgumentException {
        if (dim() != p.dim()) {
            throw new IllegalArgumentException("Dimensions of points must be equal");
        }

        Point sum = new Point(dim());
        for (int i = 0; i < sum.dim(); ++i) {
            sum.set(i, get(i) + p.get(i));
        }
        return sum;
    }

    public Point add(long shift) {
        return add(new Point(dim()).fill(shift));
    }

    public Point add(final long... p) {
        return add(new Point(p));
    }

    public Point subtract(final Point p) {
        return add(p.multiply(-1));
    }

    public Point multiply(double scale) {
        Point mult = new Point(dim());
        for (int i = 0; i < mult.dim(); ++i) {
            mult.set(i, (long) (get(i) * scale));
        }
        return mult;
    }

    public Point multiply(long scale) {
        Point mult = new Point(dim());
        for (int i = 0; i < mult.dim(); ++i) {
            mult.set(i, get(i) * scale);
        }
        return mult;
    }

    public long multiply(final Point p) throws IllegalArgumentException {
        if (dim() != p.dim()) {
            throw new IllegalArgumentException("Dimensions of points must be equal");
        }

        long mult = 0;
        for (int i = 0; i < Math.min(dim(), p.dim()); ++i) {
            mult += get(i) * p.get(i);
        }
        return mult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Point)) {
            return false;
        }

        Point point = (Point) o;

        return Arrays.equals(coordinates, point.coordinates);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }

    @Override
    public String toString() {
        return Arrays.toString(coordinates);
    }

    @Override
    public Iterator<Long> iterator() {
        return Longs.asList(coordinates).iterator();
    }

    @Override
    public Rectangle getFrameRectangle() {
        return new Rectangle(this);
    }

    public String[] toStrings() {
        String[] s = new String[coordinates.length];
        for (int i = 0; i < s.length; ++i) {
            s[i] = String.valueOf(coordinates[i]);
        }
        return s;
    }

    public Message.PointMessage toMessage() {
        final Message.PointMessage.Builder builder = Message.PointMessage.newBuilder();
        builder.addAllCoordinates(Longs.asList(coordinates));
        return builder.build();
    }

    public static Point fromMessage(final Message.PointMessage message) {
        return new Point(message.getCoordinatesList());
    }

}

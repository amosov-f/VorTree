package ru.spbu.astro.model;

import com.google.common.primitives.Longs;
import ru.spbu.astro.graphics.Framable;

import java.util.Arrays;
import java.util.Iterator;

public class Point implements Iterable<Long>, Framable {
    private long[] coordinates;

    public Point(int dim) {
        coordinates = new long[dim];
    }

    public Point(long... coordinates) {
        this.coordinates = coordinates.clone();
    }

    public Point(long x, long y) {
        coordinates = new long[]{x, y};
    }

    public long get(int i) {
        return coordinates[i];
    }

    public void set(int i, long val) {
        coordinates[i] = val;
    }

    public int dim() {
        return coordinates.length;
    }

    public long distance2To(final Point other) {
        long distance2 = 0;
        for (int i = 0; i < dim(); ++i) {
            distance2 += Math.pow(get(i) - other.get(i), 2);
        }
        return distance2;
    }

    public long sqr() {
        return distance2To(new Point(dim()));
    }

    public Point min(final Point other) {
        Point result = new Point(dim());
        for (int i = 0; i < dim(); ++i) {
            result.set(i, Math.min(get(i), other.get(i)));
        }
        return result;
    }

    public Point max(final Point other) {
        Point result = new Point(dim());
        for (int i = 0; i < dim(); ++i) {
            result.set(i, Math.max(get(i), other.get(i)));
        }
        return result;
    }

    public int getX() {
        if (dim() > 0) {
            return (int) get(0);
        }
        return 0;
    }

    public int getY() {
        if (dim() > 1) {
            return (int) get(1);
        }
        return 0;
    }

    public java.awt.Point toAwtPoint() {
        return new java.awt.Point((int)coordinates[0], (int)coordinates[1]);
    }

    public Point fill(long val) {
        Point p = new Point(dim());
        for (int i = 0; i < p.dim(); ++i) {
            p.set(i, val);
        }
        return p;
    }

    public Point add(Point p) throws IllegalArgumentException {
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

    public Point add(long... p) {
        return add(new Point(p));
    }

    public Point substract(Point p) {
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



    public long multiply(Point p) {
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
        return "Point(" +
                "coordinates = " + Arrays.toString(coordinates) +
                ')';
    }

    @Override
    public Iterator iterator() {
        return Longs.asList(coordinates).iterator();
    }

    @Override
    public Rectangle getFrameRectangle() {
        return new Rectangle(new Point[]{this});
    }
}

package ru.spbu.astro.model;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import math.geom2d.Point2D;
import org.apache.commons.collections.iterators.ArrayIterator;

import java.util.Arrays;
import java.util.Iterator;

public class Point implements Iterable<Double> {
    private long[] coordinates;

    public Point(int dim) {
        coordinates = new long[dim];
    }

    public Point(long[] coordinates) {
        this.coordinates = coordinates;
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

    public long getX() {
        if (dim() > 0) {
            return get(0);
        }
        return 0;
    }

    public long getY() {
        if (dim() > 1) {
            return get(1);
        }
        return 0;
    }

    public java.awt.Point toAwtPoint() {
        return new java.awt.Point((int)coordinates[0], (int)coordinates[1]);
    }

    public Point2D toPoint2D() {
        return new Point2D(coordinates[0], coordinates[1]);
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

        if (!Arrays.equals(coordinates, point.coordinates)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }

    @Override
    public String toString() {
        return "ru.spbu.astro.model.Point(" +
                "coordinates = " + Arrays.toString(coordinates) +
                ')';
    }

    @Override
    public Iterator iterator() {
        return Longs.asList(coordinates).iterator();
    }
}

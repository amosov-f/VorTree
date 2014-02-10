package ru.spbu.astro.model;

import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.BlockFieldMatrix;
import org.apache.commons.math3.linear.FieldLUDecomposition;
import ru.spbu.astro.graphics.Framable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class Ball implements Framable {
    private final BigFraction[] center;
    private final BigFraction radius2;

    public Ball(final Collection<Point> points) {
        int dim = points.size() - 1;
        final List<Point> pointList = new ArrayList<>(points);

        final BigFraction[][] f = new BigFraction[dim][dim];

        for (int i = 0; i < f.length; ++i) {
            for (int j = 0; j < f[i].length; ++j) {
                f[i][j] = new BigFraction(pointList.get(i + 1).get(j) - pointList.get(0).get(j));
            }
        }

        final BigFraction[] b = new BigFraction[dim];
        for (int i = 0; i < b.length; ++i) {
            b[i] = new BigFraction(pointList.get(i + 1).sqr() - pointList.get(0).sqr(), 2L);
        }

        final FieldLUDecomposition<BigFraction> decomposition = new FieldLUDecomposition<>(new BlockFieldMatrix<>(f));
        center = decomposition.getSolver().solve(new ArrayFieldVector<>(b)).toArray();
        radius2 = distance2to(pointList.get(0));
    }

    private BigFraction distance2to(final Point p2) {
        BigFraction distance2 = BigFraction.ZERO;
        for (int i = 0; i < center.length; ++i) {
            BigFraction cur = center[i].subtract(p2.get(i));
            cur = cur.multiply(cur);
            distance2 = distance2.add(cur);
        }
        return distance2;
    }

    public boolean contains(final Point p) {
        return radius2.compareTo(distance2to(p)) == 1;
    }

    public Ball(final Point... points) {
        this(Arrays.asList(points));
    }

    public Point getCenter() {
        final long[] coordinates = new long[center.length];
        for (int i = 0; i < coordinates.length; ++i) {
            coordinates[i] = center[i].longValue();
        }
        return new Point(coordinates);
    }

    public long getRadius() {
        return Math.round(Math.sqrt(radius2.doubleValue()));
    }

    @Override
    public Rectangle getFrameRectangle() {
        final Point center = getCenter();
        long r = getRadius();
        final Point minVertex = center.add(-r);
        final Point maxVertex = center.add(r);
        return new Rectangle(minVertex, maxVertex);
    }
}

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

public class Ball implements Framable {
    private BigFraction[] center;
    private BigFraction radius2;

    public Ball(Collection<Point> points) {
        int dim = points.size() - 1;
        ArrayList<Point> pointList = new ArrayList(points);

        BigFraction[][] f = new BigFraction[dim][dim];

        for (int i = 0; i < f.length; ++i) {
            for (int j = 0; j < f[i].length; ++j) {
                f[i][j] = new BigFraction(pointList.get(i + 1).get(j) - pointList.get(0).get(j));
            }
        }

        BigFraction[] b = new BigFraction[dim];
        for (int i = 0; i < b.length; ++i) {
            b[i] = new BigFraction(pointList.get(i + 1).sqr() - pointList.get(0).sqr(), 2L);
        }

        FieldLUDecomposition<BigFraction> decomposition = new FieldLUDecomposition(new BlockFieldMatrix<BigFraction>(f));
        center = decomposition.getSolver().solve(new ArrayFieldVector(b)).toArray();
        radius2 = distance2to(pointList.get(0));
    }

    private BigFraction distance2to(Point p2) {
        BigFraction distance2 = BigFraction.ZERO;
        for (int i = 0; i < center.length; ++i) {
            BigFraction cur = center[i].subtract(p2.get(i));
            cur = cur.multiply(cur);
            distance2 = distance2.add(cur);
        }
        return distance2;
    }

    public boolean contains(Point p) {
        return radius2.compareTo(distance2to(p)) == 1;
    }

    public Ball(Point... points) {
        this(Arrays.asList(points));
    }

    public Point getCenter() {
        Point center = new Point(this.center.length);
        for (int i = 0; i < this.center.length; ++i) {
            center.set(i, this.center[i].longValue());
        }
        return center;
    }

    public long getRadius() {
        return Math.round(Math.sqrt(radius2.doubleValue()));
    }

    public long getRadius2() {
        return radius2.longValue();
    }

    @Override
    public Rectangle getFrameRectangle() {
        Point center = getCenter();
        long r = getRadius();
        Point minVertex = center.add(-r);
        Point maxVertex = center.add(r);
        return new Rectangle(minVertex, maxVertex);
    }
}

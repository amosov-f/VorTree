package ru.spbu.astro.utility;

import org.apache.commons.math3.random.RandomDataGenerator;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;

import java.util.*;

public final class PointGenerator {
    public static List<Point> nextUniforms(int n, final Rectangle rect) {
        Set<Point> points = new HashSet<>();
        while (points.size() < n) {
            long[] coordinates = new long[rect.dim()];
            for (int d = 0; d < coordinates.length; ++d) {
                long l = rect.getMinVertex().get(d);
                long r = rect.getMaxVertex().get(d);
                coordinates[d] = new RandomDataGenerator().nextLong(l, r);
            }
            points.add(new Point(coordinates));
        }
        return new ArrayList<>(points);
    }

    public static List<Point> nextUniforms(int n, final Point p) {
        return nextUniforms(n, new Rectangle(new Point[]{new Point(p.dim()), p}));
    }

    public static List<Point> nextUniforms(int n, int width, int height) {
        return nextUniforms(n, new Point(width, height));
    }

    public static List<Point> nextUniforms(int n) {
        return nextUniforms(n, 10000000, 10000000);
    }

    public static List<Point> nextGaussians(int n, final Point center, double sigma) {
        Set<Point> points = new HashSet<>();
        for (int i = 0; i < n; ++i) {
            long[] coordinates = new long[center.dim()];
            for (int d = 0; d < coordinates.length; ++d) {
                coordinates[d] = (long) new RandomDataGenerator().nextGaussian(center.get(d), sigma);
            }
            points.add(new Point(coordinates));
        }
        return new ArrayList<>(points);
    }

    public static List<Point> nextGaussians(int n, int x, int y) {
        return nextGaussians(n, new Point(x, y), 10 * n);
    }

    public static List<Point> nextGaussians(int n) {
        return nextGaussians(n, 0, 0);
    }
}

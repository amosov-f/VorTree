package ru.spbu.astro.utility;

import org.apache.commons.math3.random.RandomDataGenerator;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;

import java.util.*;

public class PointGenerator {
    public static List<Point> nextUniforms(int n, Rectangle rect) {
        Set<Point> points = new HashSet();
        while (points.size() < n) {
            Point p = new Point(rect.dim());
            for (int d = 0; d < p.dim(); ++d) {
                long l = rect.getMinVertex().get(d);
                long r = rect.getMaxVertex().get(d);
                p.set(d, new RandomDataGenerator().nextLong(l, r));
            }
            points.add(p);
        }
        return new ArrayList(points);
    }

    public static List<Point> nextUniforms(int n, Point p) {
        return nextUniforms(n, new Rectangle(new Point[]{new Point(p.dim()), p}));
    }

    public static List<Point> nextUniforms(int n, int width, int height) {
        return nextUniforms(n, new Point(width, height));
    }

    public static List<Point> nextUniforms(int n) {
        return nextUniforms(n, 10000000, 10000000);
    }

    public static List<Point> nextGaussians(int n, Point center, double sigma) {
        Set<Point> points = new HashSet();
        for (int i = 0; i < n; ++i) {
            Point p = new Point(center.dim());
            for (int d = 0; d < p.dim(); ++d) {
                p.set(d, (long) new RandomDataGenerator().nextGaussian(center.get(d), sigma));
            }
            points.add(p);
        }
        return new ArrayList(points);
    }

    public static List<Point> nextGaussians(int n, int x, int y) {
        return nextGaussians(n, new Point(x, y), 10 * n);
    }

    public static List<Point> nextGaussians(int n) {
        return nextGaussians(n, 0, 0);
    }
}

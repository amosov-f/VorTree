package ru.spbu.astro.delaunay;

import math.geom2d.conic.Circle2D;
import ru.spbu.astro.model.Point;

import java.util.*;

/**
 * Created by fedor on 21.12.13.
 */
public class DelaunayBinder {

    private List<Point>[] cells;
    private List<Point> points;
    private DelaunayGraph[] delaunayGraphs;


    public DelaunayBinder(int n, int m, int width, int height) {
        points = new ArrayList();
        for (int i = 0; i < n; ++i) {
            double x = new Random().nextDouble() * width;
            double y = new Random().nextDouble() * height;
            points.add(new Point(new double[]{x, y}));
        }
        Collections.shuffle(points);

        List<Point> pivots = points.subList(0, m);

        int[] point2pivot = new int[points.size()];

        for (int i = 0; i < points.size(); ++i) {
            for (int j = 1; j < pivots.size(); ++j) {
                if (points.get(i).distanceTo(pivots.get(j)) < points.get(i).distanceTo(pivots.get(point2pivot[i]))) {
                    point2pivot[i] = j;
                }
            }
        }

        cells = new List[pivots.size()];
        for (int i = 0; i < cells.length; ++i) {
            cells[i] = new ArrayList();
        }

        for (int i = 0; i < points.size(); ++i) {
            cells[point2pivot[i]].add(points.get(i));
        }

        delaunayGraphs = new DelaunayGraph[cells.length];
        for (int i = 0; i < cells.length; ++i) {
            delaunayGraphs[i] = new DelaunayGraph(cells[i]);
        }
    }

    public List<Point>[] getCells() {
        return cells;
    }

    public List<Point> getPoints() {
        return points;
    }

    public List<DelaunayGraph.Triangle> getCreepTriangles() {
        List<DelaunayGraph.Triangle> creepTriangles = new ArrayList();
        for (int i = 0; i < delaunayGraphs.length; ++i) {
            List<DelaunayGraph.Triangle> triangles = delaunayGraphs[i].getTriangles();

            for (DelaunayGraph.Triangle triangle : triangles) {
                Circle2D circle2D = triangle.getCircle2D();
                for (Point point : points) {
                    if (point.toPoint2D().distance(circle2D.center()) < circle2D.radius() - 0.0000001) {
                        creepTriangles.add(triangle);
                        break;
                    }
                }
            }
        }
        return creepTriangles;
    }

    public List<Point> getBindPoints() {

        Set<Point> border = new HashSet();

        for (int i = 0; i < delaunayGraphs.length; ++i) {
            border.addAll(delaunayGraphs[i].getBorderPoints());
        }

        for (DelaunayGraph.Triangle triangle : getCreepTriangles()) {
            border.addAll(triangle.points);
        }

        return new ArrayList(border);
    }

    public DelaunayGraph[] getDelaunayGraphs() {
        return delaunayGraphs;
    }

    public double getRate() {
        return 1.0 * getBindPoints().size() / points.size();
    }
}

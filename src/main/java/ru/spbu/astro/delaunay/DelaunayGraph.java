package ru.spbu.astro.delaunay;

import javafx.util.Pair;
import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import ru.spbu.astro.model.Point;
import visad.Delaunay;
import visad.VisADException;

import java.util.*;

public class DelaunayGraph {

    public static class Edge {
        public Point first;
        public Point second;

        public Edge(final Point first, final Point second) {
            this.first = first;
            this.second = second;
        }
    }

    public static class Triangle {
        public List<Point> points;

        public Triangle() {
            points = new ArrayList();
        }

        public Triangle(final List<Point> points) {
            this.points = points;
        }

        public void add(final Point p) {
            points.add(p);
        }

        public Circle2D getCircle2D() {
            return Circle2D.create(points.get(0).toPoint2D(), points.get(1).toPoint2D(), points.get(2).toPoint2D());
        }
    }

    private Delaunay delaunay;
    private List<Point> points;

    public DelaunayGraph(final List<Point> points) {
        this.points = points;

        float[][] samples = new float[points.get(0).dim()][points.size()];

        for (int i = 0; i < this.points.size(); ++i) {
            for (int d = 0; d < this.points.get(i).dim(); ++d) {
                samples[d][i] = (float)this.points.get(i).get(d);
            }
        }

        try {
            delaunay = Delaunay.factory(samples, false);
        } catch (VisADException e) {
            e.printStackTrace();
            return;
        }
    }

    public List<Edge> getEdges() {
        if (delaunay == null) {
            return new ArrayList();
        }

        List<Edge>  edges = new ArrayList();
        for (int u = 0; u < delaunay.Vertices.length; ++u) {
            for (int i = 0; i < delaunay.Vertices[u].length; ++i) {
                int t = delaunay.Vertices[u][i];
                for (int j = 0; j < delaunay.Tri[t].length; ++j) {
                    int v = delaunay.Tri[t][j];
                    edges.add(new Edge(points.get(u), points.get(v)));
                }
            }
        }
        return edges;
    }


    public List<Triangle> getTriangles() {
        if (delaunay == null) {
            return new ArrayList();
        }

        List<Triangle> triangles = new ArrayList();
        for (int t = 0; t < delaunay.Tri.length; ++t) {
            Triangle triangle = new Triangle();
            for (int i = 0; i < delaunay.Tri[t].length; ++i) {
                triangle.add(points.get(delaunay.Tri[t][i]));
            }
            triangles.add(triangle);
        }
        return triangles;
    }

    public List<Point> getBorderPoints() {
        if (delaunay == null) {
            return new ArrayList();
        }

        List<Point> borderPoints = new ArrayList();
        for (int v = 0; v < delaunay.Vertices.length; ++v) {
            Map<Integer, Integer> neibCount = new HashMap();
            for (int i = 0; i < delaunay.Vertices[v].length; ++i) {
                int t = delaunay.Vertices[v][i];
                for (int j = 0; j < delaunay.Edges[t].length; ++j) {
                    if (!neibCount.containsKey(delaunay.Edges[t][j])) {
                        neibCount.put(delaunay.Edges[t][j], 1);
                    } else {
                        neibCount.put(delaunay.Edges[t][j], neibCount.get(delaunay.Edges[t][j]) + 1);
                    }
                }
            }
            int count2 = 0;
            for (Integer count : neibCount.values()) {
                if (count == 2) {
                    count2++;

                }
            }
            if (count2 != delaunay.Vertices[v].length) {
                borderPoints.add(points.get(v));
            }
        }
        return borderPoints;
    }

}

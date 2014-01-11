package ru.spbu.astro.delaunay;

import com.google.common.primitives.Ints;
import javafx.util.Pair;
import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.BlockFieldMatrix;
import org.apache.commons.math3.linear.FieldLUDecomposition;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;

import java.util.*;

public abstract class AbstractDelaunayGraphBuilder {
    public final Map<Integer, Point> id2point;
    protected final int DIM;
    protected final int m;

    public AbstractDelaunayGraphBuilder(final Collection<Point> points, int m) {
        List<Point> pointList = new ArrayList(points);
        DIM = pointList.get(0).dim();
        this.m = m;
        id2point = new HashMap();
        for (int i = 0; i < pointList.size(); ++i) {
            id2point.put(i, pointList.get(i));
        }
    }

    public abstract AbstractDelaunayGraph build(final Collection<Integer> pointIds, int level);

    public AbstractDelaunayGraph build(final Collection<Integer> pointIds) {
        return build(pointIds, 0);
    }

    public AbstractDelaunayGraph build() {
        return build(id2point.keySet());
    }


    public Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> split(final Collection<Integer> pointIds, int level) {

        List<Integer> perm = new ArrayList();
        for (Integer pointId : pointIds)  {
            perm.add(pointId);
        }
        Collections.shuffle(perm);


        List<Integer> pivotIds = new ArrayList();
        for (int i = 0; i < Math.min(m, perm.size()); ++i) {
            pivotIds.add(perm.get(i));
        }

        Map<Integer, Integer> pointId2pivotId = new HashMap();
        for (Integer pointId : pointIds) {
            pointId2pivotId.put(pointId, pivotIds.get(0));
            Point p = id2point.get(pointId);
            for (Integer pivotId : pivotIds) {
                if (p.distance2To(id2point.get(pivotId)) < p.distance2To(id2point.get(pointId2pivotId.get(pointId)))) {
                    pointId2pivotId.put(pointId, pivotId);
                }
            }
        }

        Map<Integer, Collection<Integer>> pivotId2cell = new HashMap();

        for (Map.Entry<Integer, Integer> entry : pointId2pivotId.entrySet()) {
            int pointId = entry.getKey();
            int pivotId = entry.getValue();
            if (!pivotId2cell.containsKey(pivotId)) {
                pivotId2cell.put(pivotId, new HashSet());
            }
            pivotId2cell.get(pivotId).add(pointId);
        }

        Collection<AbstractDelaunayGraph> delaunayGraphs = new ArrayList();

        //System.out.println(pivotId2cell.values());

        for (Collection<Integer> cell : pivotId2cell.values()) {
            delaunayGraphs.add(build(cell, level + 1));
        }

        return new Pair(delaunayGraphs, pointId2pivotId);
    }

    public Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> split() {
        return split(id2point.keySet(), 0);
    }

    public List<Point> getPoints(final Collection<Integer> pointIds) {
        List<Point> points = new ArrayList();
        for (Integer pointId : pointIds) {
            points.add(id2point.get(pointId));
        }
        return points;
    }

    public class Edge {
        private final Point first;
        private final Point second;

        public Edge(final Graph.Edge edge) {
            first = id2point.get(edge.getFirst());
            second = id2point.get(edge.getSecond());
        }

        public Point getFirst() {
            return first;
        }

        public Point getSecond() {
            return second;
        }
    }

    public class Triangle {
        List<Point> vertices = new ArrayList();
        int level;

        public Triangle(AbstractDelaunayGraph.Triangle triangle) {
            this.level = triangle.level;
            for (int vertexId : triangle.getVertices()) {
                vertices.add(id2point.get(vertexId));
            }
        }

        public int getLevel() {
            return level;
        }

        public List<Point> getVertices() {
            return vertices;
        }

        public boolean isCreep(final Collection<Integer> pointIds) {

            BigFraction[][] f = new BigFraction[DIM][DIM];

            for (int i = 0; i < f.length; ++i) {
                for (int j = 0; j < f[i].length; ++j) {
                    f[i][j] = new BigFraction(vertices.get(i + 1).get(j) - vertices.get(0).get(j));
                }
            }

            BigFraction[] b = new BigFraction[DIM];
            for (int i = 0; i < b.length; ++i) {
                b[i] = new BigFraction(vertices.get(i + 1).sqr() - vertices.get(0).sqr(), 2L);
            }

            FieldLUDecomposition<BigFraction> decomposition = new FieldLUDecomposition(new BlockFieldMatrix<BigFraction>(f));
            BigFraction[] center = decomposition.getSolver().solve(new ArrayFieldVector(b)).toArray();
            BigFraction radius2 = distance2(center, vertices.get(0));

            for (int pointId : pointIds) {
                if (radius2.compareTo(distance2(center, id2point.get(pointId))) == 1) {
                    //System.out.println(Math.sqrt(radius2.doubleValue()));
                    return true;
                }
            }

            return false;
        }

        private BigFraction distance2(BigFraction[] p1, Point p2) {
            BigFraction distance2 = BigFraction.ZERO;
            for (int i = 0; i < p1.length; ++i) {
                BigFraction cur = p1[i].subtract(p2.get(i));
                cur = cur.multiply(cur);
                distance2 = distance2.add(cur);
            }
            return distance2;
        }

        public boolean containsPoint() {
            long x1 = vertices.get(0).getX();
            long y1 = vertices.get(0).getY();
            long x2 = vertices.get(1).getX();
            long y2 = vertices.get(1).getY();
            long x3 = vertices.get(2).getX();
            long y3 = vertices.get(2).getY();

            for (Point p : id2point.values()) {
                if (p.equals(vertices.get(0)) || p.equals(vertices.get(1)) || p.equals(vertices.get(2))) {
                    continue;
                }

                long tx = p.getX();
                long ty = p.getY();

                long f3 = (tx - x1) * (y1 - y2) - (ty - y1) * (x1 - x2);
                long f1 = (tx - x2) * (y2 - y3) - (ty - y2) * (x2 - x3);
                long f2 = (tx - x3) * (y3 - y1) - (ty - y3) * (x3 - x1);

                if ((f1 >= 0 && f2 >= 0 && f3 >= 0) || (f1 <= 0 && f2 <= 0 && f3 <= 0)) {
                    return true;
                }
            }
            return false;
        }

    }


    public abstract class AbstractDelaunayGraph extends Graph {
        Set<Triangle> triangles = new HashSet();

        AbstractDelaunayGraph(final Collection<Integer> pointIds) {
            //System.out.println("cur: " + pointIds);
            if (pointIds.size() <= DIM) {
                for (int u : pointIds) {
                    for (int v : pointIds) {
                        addEdge(u, v);
                    }
                }
            }
        }

        public class Triangle {
            private int[] vertices;
            private int level;

            public Triangle(int[] vertices, int level) {
                this.vertices = vertices;
                Arrays.sort(vertices);
                this.level = level;
            }

            public Triangle(int[] vertices) {
                this(vertices, 0);
            }

            public Triangle(Collection<Integer> vertices) {
                this.vertices = Ints.toArray(vertices);
            }

            public int getLevel() {
                return level;
            }

            public void setLevel(int level) {
                this.level = level;
            }

            public List<Integer> getVertices() {
                return Ints.asList(vertices);
            }

            public Graph toGraph() {
                Graph g = new Graph();
                for (int u : vertices) {
                    for (int v : vertices) {
                        g.addEdge(u, v);
                    }
                }
                return g;
            }

            public boolean isAjacentTo(Triangle other) {
                int count = 0;
                for (int v : other.vertices) {
                    if (Arrays.binarySearch(vertices, v) == -1) {
                        count++;
                    }
                }
                return count == 1;
            }

            public Collection<Triangle> getSideTriangles() {
                Collection<Triangle> sideTriangles = new ArrayList();

                for (int i = 0; i < vertices.length; ++i) {
                    List<Integer> sideVertices = new ArrayList(Ints.asList(vertices));
                    sideVertices.remove(i);
                    sideTriangles.add(new Triangle(sideVertices));
                }

                return sideTriangles;
            }


            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Triangle)) {
                    return false;
                }

                Triangle triangle = (Triangle) o;

                if (!Arrays.equals(vertices, triangle.vertices)) {
                    return false;
                }

                return true;
            }

            @Override
            public int hashCode() {
                return vertices != null ? Arrays.hashCode(vertices) : 0;
            }

            @Override
            public String toString() {
                return "Triangle(" +
                        "vertices = " + Arrays.toString(vertices) +
                        ')';
            }
        }

        public Collection<Integer> getBorderVertices() {
            Set<Integer> borderVertices = new HashSet();

            Map<Triangle, Integer> count = new HashMap();

            for (Triangle t : triangles) {
                for (Triangle side : t.getSideTriangles()) {
                    if (!count.containsKey(side)) {
                        count.put(side, 0);
                    }
                    count.put(side, count.get(side) + 1);
                }
            }

            for (Map.Entry<Triangle, Integer> entry : count.entrySet()) {
                if (entry.getValue() == 1) {
                    borderVertices.addAll(entry.getKey().getVertices());
                }
            }

            return borderVertices;
        }

        public Collection<Triangle> getTriangles() {
            /*Set<Triangle> triangles = new HashSet();
            for (int u : neighbors.keySet()) {
                for (int v : neighbors.get(u)) {
                    for (int t : neighbors.get(u)) {
                        if (containsEdge(v, t)) {
                            Triangle triangle = new Triangle(new int[]{u, v, t});
                            if (!new AbstractDelaunayGraphBuilder.Triangle(triangle).containsPoint()) {
                                triangles.add(triangle);
                            }
                        }
                    }
                }
            }     */
            return triangles;
        }

        public Collection<AbstractDelaunayGraphBuilder.Triangle> getPointTriangles() {
            Collection<AbstractDelaunayGraphBuilder.Triangle> triangles = new ArrayList();
            for (Triangle triangle : getTriangles()) {
                triangles.add(new AbstractDelaunayGraphBuilder.Triangle(triangle));
            }
            return triangles;
        }

        public Collection<Triangle> getCreepTriangles(Collection<Integer> pointIds) {
            Collection<Triangle> creepTriangles = new ArrayList();
            for (Triangle triangle : getTriangles()) {
                if (new AbstractDelaunayGraphBuilder.Triangle(triangle).isCreep(pointIds)) {
                    creepTriangles.add(triangle);
                }
            }
            return creepTriangles;
        }

        public Collection<Triangle> getCreepTriangles() {
            return getCreepTriangles(id2point.keySet());
        }

        public Collection<AbstractDelaunayGraphBuilder.Triangle> getCreepPointTriangles(Collection<Integer> pointIds) {
            Collection<AbstractDelaunayGraphBuilder.Triangle> creepTriangles = new ArrayList();
            for (Triangle triangle : getCreepTriangles(pointIds)) {
                creepTriangles.add(new AbstractDelaunayGraphBuilder.Triangle(triangle));
            }
            return creepTriangles;
        }

        public Collection<AbstractDelaunayGraphBuilder.Triangle> getCreepPointTriangles() {
            return getCreepPointTriangles(id2point.keySet());
        }

        public Collection<Integer> getBindPointIds(Collection<Integer> pointIds) {
            Collection<Integer> bindPointIds = new HashSet(getBorderVertices());
            for (AbstractDelaunayGraph.Triangle triangle : getCreepTriangles(pointIds)) {
                bindPointIds.addAll(triangle.getVertices());
            }
            return bindPointIds;
        }

        public Collection<Integer> getBindPointIds() {
            return getBindPointIds(id2point.keySet());
        }


        public Graph removeCreepTriangles(Collection<Integer> pointIds) {
            Graph g = new Graph();
            //System.out.println("rm " + pointIds + " " + triangles);
            for (AbstractDelaunayGraph.Triangle triangle : getCreepTriangles(pointIds)) {
                //System.out.println("creep: " + triangle);
                removeGraph(triangle.toGraph());
                triangles.remove(triangle);
                g.addGraph(triangle.toGraph());
            }
            return g;
        }

        public List<AbstractDelaunayGraphBuilder.Edge> getPointEdges() {
            List<AbstractDelaunayGraphBuilder.Edge> edges = new ArrayList();
            for (Edge edge : getEdges()) {
                edges.add(new AbstractDelaunayGraphBuilder.Edge(edge));
            }
            return edges;
        }

        public Rectangle getRectangle() {
            return new Rectangle(id2point.values());
        }

        @Override
        public String toString() {
            return "AbstractDelaunayGraph(" + size() + ")";
        }
    }

}

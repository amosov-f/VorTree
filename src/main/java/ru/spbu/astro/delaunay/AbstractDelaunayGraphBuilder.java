package ru.spbu.astro.delaunay;

import com.google.common.primitives.Ints;
import javafx.util.Pair;
import ru.spbu.astro.graphics.Framable;
import ru.spbu.astro.model.*;

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


    public abstract class AbstractDelaunayGraph extends Graph implements Framable {

        public class Simplex {
            private int[] vertices;
            private int level;

            public Simplex(int[] vertices, int level) {
                this.vertices = vertices;
                Arrays.sort(vertices);
                this.level = level;
            }

            public Simplex(int[] vertices) {
                this(vertices, 0);
            }

            public Simplex(Collection<Integer> vertices) {
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

            public Collection<Simplex> getSideTriangles() {
                Collection<Simplex> sideSimplexes = new ArrayList();

                for (int i = 0; i < vertices.length; ++i) {
                    List<Integer> sideVertices = new ArrayList(Ints.asList(vertices));
                    sideVertices.remove(i);
                    sideSimplexes.add(new Simplex(sideVertices));
                }

                return sideSimplexes;
            }


            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof Simplex)) {
                    return false;
                }

                Simplex simplex = (Simplex) o;

                return Arrays.equals(vertices, simplex.vertices);
            }

            @Override
            public int hashCode() {
                return vertices != null ? Arrays.hashCode(vertices) : 0;
            }

            @Override
            public String toString() {
                return "Simplex(" +
                        "vertices = " + Arrays.toString(vertices) +
                        ')';
            }
        }

        Collection<Integer> pointIds;
        Set<Simplex> simplexes = new HashSet();

        AbstractDelaunayGraph(final Collection<Integer> pointIds) {
            this.pointIds = pointIds;
            if (pointIds.size() <= DIM) {
                for (int u : pointIds) {
                    for (int v : pointIds) {
                        addEdge(u, v);
                    }
                }
            }
        }

        public Collection<Integer> getBindPointIds() {
            return getBindPointIds(id2point.keySet());
        }

        public Collection<Integer> getBindPointIds(Collection<Integer> pointIds) {
            Collection<Integer> bindPointIds = new HashSet(getBorderVertices());
            for (Simplex simplex : getCreepTriangles(pointIds)) {
                bindPointIds.addAll(simplex.getVertices());
            }
            return bindPointIds;
        }

        public Graph removeCreepTriangles(Collection<Integer> pointIds) {
            Graph g = new Graph();
            for (Simplex simplex : getCreepTriangles(pointIds)) {
                removeGraph(simplex.toGraph());
                simplexes.remove(simplex);
                g.addGraph(simplex.toGraph());
            }
            return g;
        }

        public Collection<Simplex> getCreepTriangles(Collection<Integer> pointIds) {
            Collection<Simplex> creepSimplexes = new ArrayList();
            for (Simplex t : getSimplexes()) {
                if (isCreep(t, pointIds)) {
                    creepSimplexes.add(t);
                }
            }
            return creepSimplexes;
        }

        public Collection<Integer> getBorderVertices() {
            Set<Integer> borderVertices = new HashSet();

            Map<Simplex, Integer> count = new HashMap();

            for (Simplex t : simplexes) {
                for (Simplex side : t.getSideTriangles()) {
                    if (!count.containsKey(side)) {
                        count.put(side, 0);
                    }
                    count.put(side, count.get(side) + 1);
                }
            }

            for (Map.Entry<Simplex, Integer> entry : count.entrySet()) {
                if (entry.getValue() == 1) {
                    borderVertices.addAll(entry.getKey().getVertices());
                }
            }

            return borderVertices;
        }

        public Collection<Simplex> getSimplexes() {
            /*Set<Simplex> simplexes = new HashSet();
            for (int u : neighbors.keySet()) {
                for (int v : neighbors.get(u)) {
                    for (int t : neighbors.get(u)) {
                        if (containsEdge(v, t)) {
                            Simplex simplex = new Simplex(new int[]{u, v, t});
                            if (!new AbstractDelaunayGraphBuilder.Simplex(simplex).containsPoint()) {
                                simplexes.add(simplex);
                            }
                        }
                    }
                }
            }     */
            return simplexes;
        }

        public Collection<ru.spbu.astro.model.Simplex> getPointTriangles() {
            Collection<ru.spbu.astro.model.Simplex> simplexes = new ArrayList();
            for (Simplex t : getSimplexes()) {
                simplexes.add(new ru.spbu.astro.model.Simplex(getPoints(t)));
            }
            return simplexes;
        }

        public Collection<Simplex> getCreepTriangles() {
            return getCreepTriangles(pointIds);
        }

        public Collection<ru.spbu.astro.model.Simplex> getCreepPointTriangles() {
            Collection<ru.spbu.astro.model.Simplex> creepSimplexes = new ArrayList();
            for (Simplex t : getCreepTriangles(id2point.keySet())) {
                creepSimplexes.add(new ru.spbu.astro.model.Simplex(getPoints(t)));
            }
            return creepSimplexes;
        }



        public List<Line> getPointEdges() {
            List<Line> edges = new ArrayList();
            for (Edge edge : getEdges()) {
                edges.add(new Line(id2point.get(edge.getFirst()), id2point.get(edge.getSecond())));
            }
            return edges;
        }

        public List<Point> getPoints() {
            return getPoints(pointIds);
        }

        public List<Point> getPoints(Collection<Integer> pointIds) {
            List<Point> points = new ArrayList();
            for (Integer pointId : pointIds) {
                points.add(id2point.get(pointId));
            }
            return points;
        }

        public List<Point> getPoints(Simplex t) {
            return getPoints(t.getVertices());
        }

        public boolean isCreep(Simplex t, Collection<Integer> pointIds) {
            Ball b = new Ball(getPoints(t));
            for (int pointId : pointIds) {
                if (b.contains(id2point.get(pointId))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Rectangle getFrameRectangle() {
            return new Rectangle(getPoints());
        }

        @Override
        public String toString() {
            return "AbstractDelaunayGraph(" + size() + ")";
        }
    }

}

package ru.spbu.astro.delaunay;

import javafx.util.Pair;
import ru.spbu.astro.graphics.Framable;
import ru.spbu.astro.model.*;

import java.util.*;

public abstract class AbstractDelaunayGraphBuilder {
    public final Map<Integer, Point> id2point;
    protected final int dim;

    public AbstractDelaunayGraphBuilder(Collection<Point> points) {
        List<Point> pointList = new ArrayList(points);
        dim = pointList.get(0).dim();
        id2point = new HashMap();
        for (int i = 0; i < pointList.size(); ++i) {
            id2point.put(i, pointList.get(i));
        }
    }

    public abstract AbstractDelaunayGraph build(Collection<Integer> pointIds, int level);

    public AbstractDelaunayGraph build(final Collection<Integer> pointIds) {
        return build(pointIds, 0);
    }

    public AbstractDelaunayGraph build() {
        return build(id2point.keySet());
    }


    public Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> split(final Collection<Integer> pointIds, int m, int level) {

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
                if (p.distance2to(id2point.get(pivotId)) < p.distance2to(id2point.get(pointId2pivotId.get(pointId)))) {
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

    public Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> split(int m) {
        return split(id2point.keySet(), m, 0);
    }

    public final Point getPoint(int pointId) {
        return id2point.get(pointId);
    }

    public final ArrayList<Point> getPoints(Iterable<Integer> pointIds) {
        ArrayList<Point> points = new ArrayList();
        for (Integer pointId : pointIds) {
            points.add(id2point.get(pointId));
        }
        return points;
    }


    public abstract class AbstractDelaunayGraph extends Triangulation implements Framable {

        public AbstractDelaunayGraph(final Collection<Integer> pointIds) {
            addVertices(pointIds);
            if (pointIds.size() <= dim) {
                addGraph(new Simplex(pointIds).toGraph());
            }
        }

        public boolean contains(int u, Point p) {
            //System.out.println(u);
            for (int v : neighbors.get(u)) {
                if (getPoint(v).distance2to(p) < getPoint(u).distance2to(p)) {
                    return false;
                }
            }
            return true;
        }

        public HashSet<Integer> getBindPointIds(AbstractDelaunayGraph g) {
            HashSet<Integer> bindPointIds = new HashSet(g.getBorderVertices());
            for (Simplex s : getCreepSimplexes(g)) {
                bindPointIds.addAll(s.getVertices());
            }
            return bindPointIds;
        }

        public Graph removeCreepSimplexes(AbstractDelaunayGraph g) {
            Graph removedGraph = new Graph();
            for (Simplex s : getCreepSimplexes(g)) {
                removedGraph.addGraph(g.removeSimplex(s));
            }
            return removedGraph;
        }

        public ArrayList<Simplex> getCreepSimplexes(AbstractDelaunayGraph g) {
            ArrayList<Simplex> creepSimplexes = new ArrayList();
            for (Simplex s : g.getSimplexes()) {
                if (isCreep(s)) {
                    creepSimplexes.add(s);
                }
            }
            return creepSimplexes;
        }

        public Collection<ru.spbu.astro.model.Simplex> getPointSimplexes() {
            Collection<ru.spbu.astro.model.Simplex> simplexes = new ArrayList();
            for (Simplex s : getSimplexes()) {
                simplexes.add(new ru.spbu.astro.model.Simplex(getPoints(s)));
            }
            return simplexes;
        }

        public Collection<ru.spbu.astro.model.Simplex> getCreepPointSimplexes(AbstractDelaunayGraph g) {
            Collection<ru.spbu.astro.model.Simplex> creepSimplexes = new ArrayList();
            for (Simplex s : getCreepSimplexes(g)) {
                creepSimplexes.add(new ru.spbu.astro.model.Simplex(g.getPoints(s)));
            }
            return creepSimplexes;
        }

        public List<Line> getPointEdges() {
            List<Line> edges = new ArrayList();
            for (Edge edge : getEdges()) {
                edges.add(new Line(getPoint(edge.getFirst()), getPoint(edge.getSecond())));
            }
            return edges;
        }

        public final ArrayList<Point> getPoints() {
            return AbstractDelaunayGraphBuilder.this.getPoints(getVertices());
        }

        public final ArrayList<Point> getPoints(Simplex s) {
            return AbstractDelaunayGraphBuilder.this.getPoints(s.getVertices());
        }

        public boolean isCreep(Simplex s) {
            Ball b = new Ball(getPoints(s));
            //System.out.println(getPoints().size());
            for (Point p : getPoints()) {
                if (b.contains(p)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Rectangle getFrameRectangle() {
            return new Rectangle(getPoints());
        }
    }

}

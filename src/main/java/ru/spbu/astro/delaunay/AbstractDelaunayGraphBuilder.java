package ru.spbu.astro.delaunay;

import com.google.common.collect.Iterables;
import ru.spbu.astro.graphics.Framable;
import ru.spbu.astro.model.*;

import java.util.*;

public abstract class AbstractDelaunayGraphBuilder {

    protected final Map<Integer, Point> id2point;
    private final int dim;

    protected AbstractDelaunayGraphBuilder(final Collection<Point> points) {
        final List<Point> pointList = new ArrayList<>(points);
        id2point = new HashMap<>();
        for (int i = 0; i < pointList.size(); ++i) {
            id2point.put(i, pointList.get(i));
        }
        dim = pointList.get(0).dim();
    }

    protected AbstractDelaunayGraphBuilder(final Map<Integer, Point> id2point) {
        this.id2point = id2point;
        dim = Iterables.get(id2point.values(), 0).dim();
    }

    public abstract AbstractDelaunayGraph build(final Collection<Integer> pointIds);

    public AbstractDelaunayGraph build() {
        return build(id2point.keySet());
    }

    public List<Point> get(final Iterable<Integer> pointIds) {
        final List<Point> points = new ArrayList<>();
        for (int pointId : pointIds) {
            points.add(id2point.get(pointId));
        }
        return points;
    }

    public class AbstractDelaunayGraph extends Triangulation implements Framable {

        protected AbstractDelaunayGraph() {
        }

        protected AbstractDelaunayGraph(final Collection<Integer> pointIds) {
            addVertices(pointIds);
            if (pointIds.size() <= dim()) {
                addGraph(new Simplex(pointIds).toGraph());
            }
        }

        protected AbstractDelaunayGraph(final AbstractDelaunayGraph g) {
            super(g);
        }

        protected AbstractDelaunayGraph(final Map<Integer, Set<Integer>> neighbors, final Set<Simplex> simplexes) {
            super(neighbors, simplexes);
        }

        public final boolean contains(int u, final Point p) {
            for (int v : getNeighbors(u)) {
                if (id2point.get(v).distance2to(p) < id2point.get(u).distance2to(p)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isCreep(final Simplex s) {
            final Ball b = new Ball(get(s));
            for (final Point p : getPoints()) {
                if (b.contains(p)) {
                    return true;
                }
            }
            return false;
        }

        public final Set<Integer> getBindPointIds(final AbstractDelaunayGraph g) {
            final Set<Integer> bindPointIds = new HashSet<>(g.getBorderVertices());
            for (final Simplex s : getCreepSimplexes(g)) {
                bindPointIds.addAll(s.getVertices());
            }
            return bindPointIds;
        }

        public Graph removeCreepSimplexes(final AbstractDelaunayGraph g) {
            final Graph removedGraph = new Graph();
            for (final Simplex s : getCreepSimplexes(g)) {
                removedGraph.addGraph(g.removeSimplex(s));
            }
            return removedGraph;
        }

        public final List<ru.spbu.astro.model.Simplex> getPointSimplexes() {
            final List<ru.spbu.astro.model.Simplex> simplexes = new ArrayList<>();
            for (Simplex s : getSimplexes()) {
                simplexes.add(new ru.spbu.astro.model.Simplex(get(s)));
            }
            return simplexes;
        }

        public final List<ru.spbu.astro.model.Simplex> getCreepPointSimplexes(final AbstractDelaunayGraph g) {
            final List<ru.spbu.astro.model.Simplex> creepSimplexes = new ArrayList<>();
            for (Simplex s : getCreepSimplexes(g)) {
                creepSimplexes.add(new ru.spbu.astro.model.Simplex(get(s)));
            }
            return creepSimplexes;
        }

        public List<Line> getPointEdges() {
            List<Line> edges = new ArrayList<>();
            for (Edge edge : getEdges()) {
                edges.add(new Line(id2point.get(edge.getFirst()), id2point.get(edge.getSecond())));
            }
            return edges;
        }

        public final Collection<Point> getPoints() {
            return get(getVertices());
        }

        public int dim() {
            return dim;
        }

        @Override
        public Rectangle getFrameRectangle() {
            return new Rectangle(getPoints());
        }

        private List<Simplex> getCreepSimplexes(final AbstractDelaunayGraph g) {
            final List<Simplex> creepSimplexes = new ArrayList<>();
            for (Simplex s : g.getSimplexes()) {
                if (isCreep(s)) {
                    creepSimplexes.add(s);
                }
            }
            return creepSimplexes;
        }

    }

}

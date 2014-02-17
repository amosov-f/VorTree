package ru.spbu.astro.delaunay;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import ru.spbu.astro.Message;
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

    public AbstractDelaunayGraph build(final int... pointIds) {
        return build(Ints.asList(pointIds));
    }

    public List<Point> get(final Iterable<Integer> pointIds) {
        final List<Point> points = new ArrayList<>();
        for (int pointId : pointIds) {
            points.add(get(pointId));
        }
        return points;
    }

    public Point get(int pointId) {
        return id2point.get(pointId);
    }

    public boolean isCreep(final Triangulation.Simplex s) {
        final Ball b = new Ball(get(s));
        for (final Point p : get(id2point.keySet())) {
            if (b.contains(p)) {
                return true;
            }
        }
        return false;
    }

    public final List<ru.spbu.astro.model.Simplex> getCreepPointSimplexes(final AbstractDelaunayGraph g) {
        final List<Simplex> creepSimplexes = new ArrayList<>();
        for (final Triangulation.Simplex s : g.getSimplexes()) {
            if (isCreep(s)) {
                creepSimplexes.add(new Simplex(get(s)));
            }
        }
        return creepSimplexes;
    }

    public final List<Simplex> getNormalPointSimplexes(final AbstractDelaunayGraph g) {
        final List<Simplex> normalSimplexes = new ArrayList<>();
        for (final Triangulation.Simplex s : g.getSimplexes()) {
            if (!isCreep(s)) {
                normalSimplexes.add(new Simplex(get(s)));
            }
        }
        return normalSimplexes;
    }

    public class AbstractDelaunayGraph extends Triangulation implements Framable {

        protected AbstractDelaunayGraph(final Collection<Integer> pointIds) {
            addVertices(pointIds);
            if (pointIds.size() <= dim()) {
                addGraph(new Simplex(pointIds).toGraph());
            }
        }

        protected AbstractDelaunayGraph(final AbstractDelaunayGraph g) {
            super(g);
        }

        protected AbstractDelaunayGraph(final Message.AbstractDelaunayGraph message) {
            super(message.getTriangulation());
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

        public AbstractDelaunayGraphBuilder getBuilder() {
            return AbstractDelaunayGraphBuilder.this;
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
            for (final Simplex s : getSimplexes()) {
                simplexes.add(new ru.spbu.astro.model.Simplex(get(s)));
            }
            return simplexes;
        }

        public final List<ru.spbu.astro.model.Simplex> getCreepPointSimplexes(final AbstractDelaunayGraph g) {
            final List<ru.spbu.astro.model.Simplex> creepSimplexes = new ArrayList<>();
            for (final Simplex s : getCreepSimplexes(g)) {
                creepSimplexes.add(new ru.spbu.astro.model.Simplex(get(s)));
            }
            return creepSimplexes;
        }

        public List<Line> getPointEdges() {
            List<Line> edges = new ArrayList<>();
            for (Edge e : getSimplexGraph()) {
                edges.add(new Line(id2point.get(e.getFirst()), id2point.get(e.getSecond())));
            }
            return edges;
        }

        public List<Point> getBorderPoints() {
            return get(getBorderVertices());
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
            for (final Simplex s : g.getSimplexes()) {
                if (isCreep(s)) {
                    creepSimplexes.add(s);
                }
            }
            return creepSimplexes;
        }

        public List<Simplex> getCreepSimplexes() {
            final List<Simplex> creepSimplexes = new ArrayList<>();
            for (final Simplex s : getSimplexes()) {
                if (isCreep(s)) {
                    creepSimplexes.add(s);
                }
            }
            return creepSimplexes;
        }

        public List<Point> getIsolatedPoints() {
            return get(getIsolatedVertices());
        }

        public Message.AbstractDelaunayGraph toAbstractDelaunayGraphMessage() {
            final Message.AbstractDelaunayGraph.Builder builder = Message.AbstractDelaunayGraph.newBuilder();
            builder.setTriangulation(super.toTriangulationMessage());
            return builder.build();
        }

    }

}

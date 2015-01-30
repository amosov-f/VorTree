package ru.spbu.astro.delaunay;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.Message;
import ru.spbu.astro.graphics.Framable;
import ru.spbu.astro.model.*;

import java.util.*;

@SuppressWarnings("unused")
public abstract class AbstractDelaunayGraphBuilder {
    @NotNull
    protected final Map<Integer, Point> id2point;
    private final int dim;

    protected AbstractDelaunayGraphBuilder(@NotNull final Collection<Point> points) {
        final List<Point> pointList = new ArrayList<>(points);
        id2point = new HashMap<>();
        for (int i = 0; i < pointList.size(); ++i) {
            id2point.put(i, pointList.get(i));
        }
        dim = pointList.get(0).dim();
    }

    protected AbstractDelaunayGraphBuilder(@NotNull final Map<Integer, Point> id2point) {
        this.id2point = id2point;
        dim = Iterables.get(id2point.values(), 0).dim();
    }

    @NotNull
    public abstract AbstractDelaunayGraph build(@NotNull final Collection<Integer> pointIds);

    @NotNull
    public AbstractDelaunayGraph build() {
        return build(id2point.keySet());
    }

    @NotNull
    public AbstractDelaunayGraph build(@NotNull final int... pointIds) {
        return build(Ints.asList(pointIds));
    }

    @NotNull
    public List<Point> get(@NotNull final Iterable<Integer> pointIds) {
        final List<Point> points = new ArrayList<>();
        for (final int pointId : pointIds) {
            points.add(get(pointId));
        }
        return points;
    }

    @NotNull
    public Point get(final int pointId) {
        return id2point.get(pointId);
    }

    public boolean isCreep(@NotNull final Triangulation.Simplex s) {
        final Ball b = new Ball(get(s));
        for (final Point p : get(id2point.keySet())) {
            if (b.contains(p)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public final List<ru.spbu.astro.model.Simplex> getCreepPointSimplexes(@NotNull final AbstractDelaunayGraph g) {
        final List<Simplex> creepSimplexes = new ArrayList<>();
        for (final Triangulation.Simplex s : g.getSimplexes()) {
            if (isCreep(s)) {
                creepSimplexes.add(new Simplex(get(s)));
            }
        }
        return creepSimplexes;
    }

    @NotNull
    public final List<Simplex> getNormalPointSimplexes(@NotNull final AbstractDelaunayGraph g) {
        final List<Simplex> normalSimplexes = new ArrayList<>();
        for (final Triangulation.Simplex s : g.getSimplexes()) {
            if (!isCreep(s)) {
                normalSimplexes.add(new Simplex(get(s)));
            }
        }
        return normalSimplexes;
    }

    public class AbstractDelaunayGraph extends Triangulation implements Framable {
        protected AbstractDelaunayGraph(@NotNull final Collection<Integer> pointIds) {
            addVertices(pointIds);
            if (pointIds.size() <= dim()) {
                addGraph(new Simplex(pointIds).toGraph());
            }
        }

        protected AbstractDelaunayGraph(@NotNull final AbstractDelaunayGraph g) {
            super(g);
        }

        protected AbstractDelaunayGraph(@NotNull final Message.AbstractDelaunayGraph message) {
            super(message.getTriangulation());
        }

        public final boolean contains(final int u, @NotNull final Point p) {
            for (final int v : getNeighbors(u)) {
                if (id2point.get(v).distance2to(p) < id2point.get(u).distance2to(p)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isCreep(@NotNull final Simplex s) {
            final Ball b = new Ball(get(s));
            for (final Point p : getPoints()) {
                if (b.contains(p)) {
                    return true;
                }
            }
            return false;
        }

        @NotNull
        public final AbstractDelaunayGraphBuilder getBuilder() {
            return AbstractDelaunayGraphBuilder.this;
        }

        @NotNull
        public final Set<Integer> getBindPointIds(@NotNull final AbstractDelaunayGraph g) {
            final Set<Integer> bindPointIds = new HashSet<>(g.getBorderVertices());
            for (final Simplex s : getCreepSimplexes(g)) {
                bindPointIds.addAll(s.getVertices());
            }
            return bindPointIds;
        }

        @NotNull
        public Graph removeCreepSimplexes(@NotNull final AbstractDelaunayGraph g) {
            final Graph removedGraph = new Graph();
            for (final Simplex s : getCreepSimplexes(g)) {
                removedGraph.addGraph(g.removeSimplex(s));
            }
            return removedGraph;
        }

        @NotNull
        public final List<ru.spbu.astro.model.Simplex> getPointSimplexes() {
            final List<ru.spbu.astro.model.Simplex> simplexes = new ArrayList<>();
            for (final Simplex s : getSimplexes()) {
                simplexes.add(new ru.spbu.astro.model.Simplex(get(s)));
            }
            return simplexes;
        }

        @NotNull
        public final List<ru.spbu.astro.model.Simplex> getCreepPointSimplexes(@NotNull final AbstractDelaunayGraph g) {
            final List<ru.spbu.astro.model.Simplex> creepSimplexes = new ArrayList<>();
            for (final Simplex s : getCreepSimplexes(g)) {
                creepSimplexes.add(new ru.spbu.astro.model.Simplex(get(s)));
            }
            return creepSimplexes;
        }

        @NotNull
        public final List<Line> getPointEdges() {
            final List<Line> edges = new ArrayList<>();
            for (final Edge e : getSimplexGraph()) {
                edges.add(new Line(id2point.get(e.getFirst()), id2point.get(e.getSecond())));
            }
            return edges;
        }

        @NotNull
        public final List<Point> getBorderPoints() {
            return get(getBorderVertices());
        }

        @NotNull
        public final Collection<Point> getPoints() {
            return get(getVertices());
        }

        public final int dim() {
            return dim;
        }

        @NotNull
        @Override
        public Rectangle getFrameRectangle() {
            return new Rectangle(getPoints());
        }

        @NotNull
        private List<Simplex> getCreepSimplexes(@NotNull final AbstractDelaunayGraph g) {
            final List<Simplex> creepSimplexes = new ArrayList<>();
            for (final Simplex s : g.getSimplexes()) {
                if (isCreep(s)) {
                    creepSimplexes.add(s);
                }
            }
            return creepSimplexes;
        }

        @NotNull
        public List<Simplex> getCreepSimplexes() {
            final List<Simplex> creepSimplexes = new ArrayList<>();
            for (final Simplex s : getSimplexes()) {
                if (isCreep(s)) {
                    creepSimplexes.add(s);
                }
            }
            return creepSimplexes;
        }

        @NotNull
        public List<Point> getIsolatedPoints() {
            return get(getIsolatedVertices());
        }

        @NotNull
        public Message.AbstractDelaunayGraph toAbstractDelaunayGraphMessage() {
            final Message.AbstractDelaunayGraph.Builder builder = Message.AbstractDelaunayGraph.newBuilder();
            builder.setTriangulation(super.toTriangulationMessage());
            return builder.build();
        }
    }
}
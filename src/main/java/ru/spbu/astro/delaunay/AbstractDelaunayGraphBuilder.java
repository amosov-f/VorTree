package ru.spbu.astro.delaunay;

import com.google.common.collect.Iterables;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.spbu.astro.db.PointDepot;
import ru.spbu.astro.graphics.Framable;
import ru.spbu.astro.model.*;

import java.util.*;

public abstract class AbstractDelaunayGraphBuilder {

    protected final PointDepot id2point =
            (PointDepot) new ClassPathXmlApplicationContext("application-context.xml").getBean("mapPointDepot");
    private final Collection<Integer> pointIds;

    protected AbstractDelaunayGraphBuilder(final Collection<Integer> pointIds) {
        this.pointIds = new ArrayList<>(pointIds);
    }

    protected AbstractDelaunayGraphBuilder(final Iterable<Point> points) {
        id2point.clear();
        pointIds = id2point.add(points);
    }

    public abstract AbstractDelaunayGraph build(final Collection<Integer> pointIds);

    public AbstractDelaunayGraph build() {
        return build(pointIds);
    }


    public abstract class AbstractDelaunayGraph extends Triangulation implements Framable {

        public AbstractDelaunayGraph(final Collection<Integer> pointIds) {
            addVertices(pointIds);
            if (pointIds.size() <= dim()) {
                addGraph(new Simplex(pointIds).toGraph());
            }
        }

        public AbstractDelaunayGraph(final AbstractDelaunayGraph g) {
            super(g);
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
            final Ball b = new Ball(getPoints(s));
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
                simplexes.add(new ru.spbu.astro.model.Simplex(getPoints(s)));
            }
            return simplexes;
        }

        public final List<ru.spbu.astro.model.Simplex> getCreepPointSimplexes(final AbstractDelaunayGraph g) {
            final List<ru.spbu.astro.model.Simplex> creepSimplexes = new ArrayList<>();
            for (Simplex s : getCreepSimplexes(g)) {
                creepSimplexes.add(new ru.spbu.astro.model.Simplex(g.getPoints(s)));
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
            return id2point.get(getVertices()).values();
        }

        public final Collection<Point> getPoints(final Simplex s) {
            return id2point.get(s.getVertices()).values();
        }

        public int dim() {
            return id2point.get(Iterables.get(pointIds, 0)).dim();
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

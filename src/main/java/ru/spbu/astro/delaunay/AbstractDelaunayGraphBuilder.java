package ru.spbu.astro.delaunay;

import com.google.common.collect.Iterables;
import javafx.util.Pair;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.spbu.astro.db.MapPointDepot;
import ru.spbu.astro.db.PointDepot;
import ru.spbu.astro.db.SQLPointDepot;
import ru.spbu.astro.graphics.Framable;
import ru.spbu.astro.model.*;

import java.util.*;

public abstract class AbstractDelaunayGraphBuilder {
    protected final PointDepot id2point =
            (PointDepot) new ClassPathXmlApplicationContext("application-context.xml").getBean("pointDepot");
    private final Collection<Integer> pointIds;
    protected final int dim;

    protected AbstractDelaunayGraphBuilder(Collection<Integer> pointIds) {
        dim = id2point.get(Iterables.get(pointIds, 0)).dim();
        this.pointIds = new ArrayList(pointIds);
    }

    protected AbstractDelaunayGraphBuilder(Iterable<Point> points) {
        dim = Iterables.get(points, 0).dim();
        id2point.clear();
        pointIds = id2point.add(points);
    }

    public abstract AbstractDelaunayGraph build(Collection<Integer> pointIds);

    public AbstractDelaunayGraph build() {
        return build(pointIds);
    }


    public abstract class AbstractDelaunayGraph extends Triangulation implements Framable {

        public AbstractDelaunayGraph() {

        }

        public AbstractDelaunayGraph(Collection<Integer> pointIds) {
            addVertices(pointIds);
            if (pointIds.size() <= dim) {
                addGraph(new Simplex(pointIds).toGraph());
            }
        }

        public AbstractDelaunayGraph(AbstractDelaunayGraph g) {
            super(g);
        }

        public boolean contains(int u, Point p) {
            //System.out.println(u);
            for (int v : neighbors.get(u)) {
                if (id2point.get(v).distance2to(p) < id2point.get(u).distance2to(p)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isCreep(Simplex s) {
            Ball b = new Ball(getPoints(s));
            for (Point p : getPoints()) {
                if (b.contains(p)) {
                    return true;
                }
            }
            return false;
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
                edges.add(new Line(id2point.get(edge.getFirst()), id2point.get(edge.getSecond())));
            }
            return edges;
        }

        public final Collection<Point> getPoints() {
            return id2point.get(getVertices()).values();
        }

        public final Collection<Point> getPoints(Simplex s) {
            return id2point.get(s.getVertices()).values();
        }

        @Override
        public Rectangle getFrameRectangle() {
            return new Rectangle(getPoints());
        }
    }

}

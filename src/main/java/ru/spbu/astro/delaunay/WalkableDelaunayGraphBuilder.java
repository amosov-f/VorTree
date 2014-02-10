package ru.spbu.astro.delaunay;

import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;

import java.util.*;

public abstract class WalkableDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {
    protected WalkableDelaunayGraphBuilder(final Iterable<Point> points) {
        super(points);
    }

    protected WalkableDelaunayGraphBuilder(final Collection<Integer> pointIds) {
        super(pointIds);
    }

    public class WalkableDelaunayGraph extends AbstractDelaunayGraph {
        protected final List<Integer> borderVertices;
        protected final Map<Simplex, Collection<Simplex> > side2simplexes;

        protected WalkableDelaunayGraph(final Collection<Integer> pointIds) {
            super(pointIds);
            borderVertices = new ArrayList<>();
            side2simplexes = new HashMap<>();

            if (pointIds.size() <= dim()) {
                borderVertices.addAll(pointIds);
            }
        }

        protected WalkableDelaunayGraph(final WalkableDelaunayGraph g) {
            super(g);
            borderVertices = new ArrayList<>(g.borderVertices);
            side2simplexes = new HashMap<>(g.side2simplexes);
        }

        @Override
        public Graph removeCreepSimplexes(final AbstractDelaunayGraph delaunayGraph) {
            final WalkableDelaunayGraph g = (WalkableDelaunayGraph) delaunayGraph;

            final Set<Simplex> visitedSimplexes = new HashSet<>();
            final Graph removedGraph = new Graph();

            for (final Simplex s : g.getBorderSimplexes()) {
                removedGraph.addGraph(dfs(g, s, visitedSimplexes));
            }

            return removedGraph;
        }

        private Graph dfs(final WalkableDelaunayGraph g, final Simplex u, final Set<Simplex> visitedSimplexes) {
            if (visitedSimplexes.contains(u)) {
                return new Graph();
            }
            visitedSimplexes.add(u);

            if (!isCreep(u)) {
                return new Graph();
            }

            final Graph removedGraph = new Graph();
            for (final Simplex v : g.getNeighborSimplexes(u)) {
                removedGraph.addGraph(dfs(g, v, visitedSimplexes));
            }

            removedGraph.addGraph(g.removeSimplex(u));

            return removedGraph;
        }

        public Set<Simplex> getBorderSimplexes() {
            final Set<Simplex> borderSimplexes = new HashSet<>();
            for (final Simplex side : side2simplexes.keySet()) {
                if (side2simplexes.get(side).size() == 1) {
                    borderSimplexes.addAll(side2simplexes.get(side));
                }
            }
            return borderSimplexes;
        }

        @Override
        public void addSimplex(final Simplex s) {
            super.addSimplex(s);
            for (final Simplex side : s.getSides()) {
                if (!side2simplexes.containsKey(side)) {
                    side2simplexes.put(side, new HashSet<Simplex>());
                }
                side2simplexes.get(side).add(s);
            }
        }

        @Override
        public Graph removeSimplex(final Simplex s) {
            for (final Simplex side : s.getSides()) {
                side2simplexes.get(side).remove(s);
            }
            return super.removeSimplex(s);
        }

        public Set<Simplex> getNeighborSimplexes(final Simplex u) {
            final Set<Simplex> neighborSimplexes = new HashSet<>();
            for (final Simplex side : u.getSides()) {
                for (final Simplex v : side2simplexes.get(side)) {
                    if (!u.equals(v)) {
                        neighborSimplexes.add(v);
                    }
                }
            }
            return neighborSimplexes;
        }

        @Override
        public Collection<Integer> getBorderVertices() {
            return borderVertices;
        }
    }
}

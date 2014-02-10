package ru.spbu.astro.delaunay;

import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;

import java.util.*;

public abstract class WalkableDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {
    protected WalkableDelaunayGraphBuilder(Iterable<Point> points) {
        super(points);
    }

    protected WalkableDelaunayGraphBuilder(Collection<Integer> pointIds) {
        super(pointIds);
    }

    public class WalkableDelaunayGraph extends AbstractDelaunayGraph {
        protected ArrayList<Integer> borderVertices = new ArrayList();
        protected HashMap<Simplex, Collection<Simplex> > side2simplexes = new HashMap();

        protected WalkableDelaunayGraph() {
        }

        protected WalkableDelaunayGraph(Collection<Integer> pointIds) {
            super(pointIds);
        }

        protected WalkableDelaunayGraph(WalkableDelaunayGraph g) {
            super(g);
            borderVertices = new ArrayList(g.borderVertices);
        }

        @Override
        public Graph removeCreepSimplexes(AbstractDelaunayGraph delaunayGraph) {
            WalkableDelaunayGraph g = (WalkableDelaunayGraph) delaunayGraph;

            HashSet<Simplex> visitedSimplexes = new HashSet();
            Graph removedGraph = new Graph();

            for (Simplex s : g.getBorderSimplexes()) {
                removedGraph.addGraph(dfs(g, s, visitedSimplexes));
            }

            return removedGraph;
        }

        private Graph dfs(WalkableDelaunayGraph g, Simplex u, HashSet<Simplex> visitedSimplexes) {
            if (visitedSimplexes.contains(u)) {
                return new Graph();
            }
            visitedSimplexes.add(u);

            if (!isCreep(u)) {
                return new Graph();
            }

            Graph removedGraph = new Graph();
            for (Simplex v : g.getNeighborSimplexes(u)) {
                removedGraph.addGraph(dfs(g, v, visitedSimplexes));
            }

            removedGraph.addGraph(g.removeSimplex(u));

            return removedGraph;
        }

        public HashSet<Simplex> getBorderSimplexes() {
            HashSet<Simplex> borderSimplexes = new HashSet();
            for (Simplex side : side2simplexes.keySet()) {
                if (side2simplexes.get(side).size() == 1) {
                    borderSimplexes.addAll(side2simplexes.get(side));
                }
            }
            return borderSimplexes;
        }

        @Override
        public void addSimplex(Simplex s) {
            super.addSimplex(s);
            for (Simplex side : s.getSides()) {
                if (!side2simplexes.containsKey(side)) {
                    side2simplexes.put(side, new HashSet());
                }
                side2simplexes.get(side).add(s);
            }
        }

        @Override
        public Graph removeSimplex(Simplex s) {
            for (Simplex side : s.getSides()) {
                side2simplexes.get(side).remove(s);
            }
            return super.removeSimplex(s);
        }

        public Collection<Simplex> getNeighborSimplexes(Simplex u) {
            Set<Simplex> neighborSimplexes = new HashSet();
            for (Simplex side : u.getSides()) {
                for (Simplex v : side2simplexes.get(side)) {
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

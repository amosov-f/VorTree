package ru.spbu.astro.delaunay;

import ru.spbu.astro.Message;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;

import java.util.*;

public abstract class WalkableDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {

    protected WalkableDelaunayGraphBuilder(final Collection<Point> points) {
        super(points);
    }

    protected WalkableDelaunayGraphBuilder(final Map<Integer, Point> id2point) {
        super(id2point);
    }

    public class WalkableDelaunayGraph extends AbstractDelaunayGraph {

        private final Map<Simplex, Set<Simplex>> side2simplexes;

        protected WalkableDelaunayGraph(final Collection<Integer> pointIds) {
            super(pointIds);
            side2simplexes = new HashMap<>();
        }

        protected WalkableDelaunayGraph(final WalkableDelaunayGraph g) {
            super(g);
            side2simplexes = new HashMap<>(g.side2simplexes);
        }

        protected WalkableDelaunayGraph(final Message.WalkableDelaunayGraph message) {
            super(message.getAbstractDelaunayGraph());

            side2simplexes = new HashMap<>();
            for (Message.WalkableDelaunayGraph.Side2SimplexesEntry side2simplexesEntryMessage : message.getSide2SimplexesList()) {
                Set<Simplex> simplexSet = new HashSet<>();
                for (Message.Simplex simplexMessage : side2simplexesEntryMessage.getSimplexesList()) {
                    simplexSet.add(new Simplex(simplexMessage));
                }
                side2simplexes.put(new Simplex(side2simplexesEntryMessage.getSide()), simplexSet);
            }
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
        public Set<Integer> getBorderVertices() {
            if (getSimplexes().isEmpty()) {
                return getVertices();
            }
            final Set<Integer> borderVertices = new HashSet<>();
            for (final Simplex side : side2simplexes.keySet()) {
                if (side2simplexes.get(side).size() == 1) {
                    borderVertices.addAll(side.getVertices());
                }
            }
            return borderVertices;
        }

        public Message.WalkableDelaunayGraph toWalkableDelaunayGraphMessage() {
            final Message.WalkableDelaunayGraph.Builder builder = Message.WalkableDelaunayGraph.newBuilder();
            builder.setAbstractDelaunayGraph(super.toAbstractDelaunayGraphMessage());

            for (final Simplex side : side2simplexes.keySet()) {
                final Message.WalkableDelaunayGraph.Side2SimplexesEntry.Builder side2simplexesEntryBuilder
                        = Message.WalkableDelaunayGraph.Side2SimplexesEntry.newBuilder();
                side2simplexesEntryBuilder.setSide(side.toMessage());
                for (final Simplex s : side2simplexes.get(side)) {
                    side2simplexesEntryBuilder.addSimplexes(s.toMessage());
                }
                builder.addSide2Simplexes(side2simplexesEntryBuilder);
            }

            return builder.build();
        }

    }

}

package ru.spbu.astro.delaunay;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.Message;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Line;
import ru.spbu.astro.model.Point;

import java.util.*;

public abstract class WalkableDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {
    protected WalkableDelaunayGraphBuilder(@NotNull final Collection<Point> points) {
        super(points);
    }

    protected WalkableDelaunayGraphBuilder(@NotNull final Map<Integer, Point> id2point) {
        super(id2point);
    }

    public class WalkableDelaunayGraph extends AbstractDelaunayGraph {
        @NotNull
        private final Map<Simplex, Set<Simplex>> side2simplexes;

        protected WalkableDelaunayGraph(@NotNull final Collection<Integer> pointIds) {
            super(pointIds);
            side2simplexes = new HashMap<>();
        }

        protected WalkableDelaunayGraph(@NotNull final WalkableDelaunayGraph g) {
            super(g);
            side2simplexes = new HashMap<>(g.side2simplexes);
        }

        protected WalkableDelaunayGraph(@NotNull final Message.WalkableDelaunayGraph message) {
            super(message.getAbstractDelaunayGraph());

            side2simplexes = new HashMap<>();
            for (final Message.WalkableDelaunayGraph.Side2SimplexesEntry side2simplexesEntryMessage : message.getSide2SimplexesList()) {
                final Set<Simplex> simplexSet = new HashSet<>();
                for (final Message.Simplex simplexMessage : side2simplexesEntryMessage.getSimplexesList()) {
                    simplexSet.add(new Simplex(simplexMessage));
                }
                side2simplexes.put(new Simplex(side2simplexesEntryMessage.getSide()), simplexSet);
            }
        }

        @NotNull
        @Override
        public Graph removeCreepSimplexes(@NotNull final AbstractDelaunayGraph delaunayGraph) {
            final WalkableDelaunayGraph g = (WalkableDelaunayGraph) delaunayGraph;

            final Set<Simplex> visitedSimplexes = new HashSet<>();
            final Graph removedGraph = new Graph();

            for (final Simplex s : g.getBorderSimplexes()) {
                removedGraph.addGraph(dfs(g, s, visitedSimplexes));
            }

            return removedGraph;
        }

        @NotNull
        private Graph dfs(@NotNull final WalkableDelaunayGraph g,
                          @NotNull final Simplex u,
                          @NotNull final Set<Simplex> visitedSimplexes) {
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

        @NotNull
        public final Set<Simplex> getBorderSimplexes() {
            final Set<Simplex> borderSimplexes = new HashSet<>();
            for (final Simplex side : side2simplexes.keySet()) {
                if (side2simplexes.get(side).size() == 1) {
                    borderSimplexes.addAll(side2simplexes.get(side));
                }
            }
            return borderSimplexes;
        }

        @NotNull
        public final List<Simplex> getBorderSides() {
            final List<Simplex> borderSides = new ArrayList<>();
            for (final Simplex side : side2simplexes.keySet()) {
                if (side2simplexes.get(side).size() == 1) {
                    borderSides.add(side);
                }
            }
            return borderSides;
        }

        @NotNull
        public List<Line> getBorderEdges() {
            final List<Line> borderEdges = new ArrayList<>();
            for (final Simplex s : getBorderSides()) {
                borderEdges.add(new Line(get(s.getVertices().get(0)), get(s.getVertices().get(1))));
            }
            return borderEdges;
        }

        @Override
        public void addSimplex(@NotNull final Simplex s) {
            super.addSimplex(s);
            for (final Simplex side : s.getSides()) {
                if (!side2simplexes.containsKey(side)) {
                    side2simplexes.put(side, new HashSet<Simplex>());
                }
                side2simplexes.get(side).add(s);
            }
        }

        @NotNull
        @Override
        public Graph removeSimplex(@NotNull final Simplex s) {
            for (final Simplex side : s.getSides()) {
                side2simplexes.get(side).remove(s);
            }
            return super.removeSimplex(s);
        }

        @NotNull
        public Set<Simplex> getNeighborSimplexes(@NotNull final Simplex u) {
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

        @NotNull
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

        @NotNull
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
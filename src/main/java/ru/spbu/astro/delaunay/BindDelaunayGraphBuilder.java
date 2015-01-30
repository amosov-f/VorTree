package ru.spbu.astro.delaunay;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;

import java.util.*;

@Deprecated
public final class BindDelaunayGraphBuilder extends WalkableDelaunayGraphBuilder {
    private final int division;
    @NotNull
    private final NativeDelaunayGraphBuilder nativeDelaunayGraphBuilder;

    public BindDelaunayGraphBuilder(@NotNull final Collection<Point> points, final int division) {
        super(points);
        this.division = division;
        nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(id2point);
    }

    public BindDelaunayGraphBuilder(@NotNull final Map<Integer, Point> id2point, final int division) {
        super(id2point);
        this.division = division;
        nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(id2point);
    }

    @NotNull
    @Override
    public BindDelaunayGraph build(@NotNull final Collection<Integer> pointIds) {
        return new BindDelaunayGraph(pointIds);
    }

    public Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> split(@NotNull final Collection<Integer> pointIds,
                                                                                final int m,
                                                                                final int level) {
        final List<Integer> perm = new ArrayList<>();
        for (final Integer pointId : pointIds) {
            perm.add(pointId);
        }
        Collections.shuffle(perm);

        final List<Integer> pivotIds = new ArrayList<>();
        for (int i = 0; i < Math.min(m, perm.size()); ++i) {
            pivotIds.add(perm.get(i));
        }

        final Map<Integer, Integer> pointId2pivotId = new HashMap<>();
        for (final Integer pointId : pointIds) {
            pointId2pivotId.put(pointId, pivotIds.get(0));
            final Point p = id2point.get(pointId);
            for (final Integer pivotId : pivotIds) {
                if (p.distance2to(id2point.get(pivotId)) < p.distance2to(id2point.get(pointId2pivotId.get(pointId)))) {
                    pointId2pivotId.put(pointId, pivotId);
                }
            }
        }

        final Map<Integer, Collection<Integer>> pivotId2cell = new HashMap<>();

        for (final Map.Entry<Integer, Integer> entry : pointId2pivotId.entrySet()) {
            final int pointId = entry.getKey();
            final int pivotId = entry.getValue();
            if (!pivotId2cell.containsKey(pivotId)) {
                pivotId2cell.put(pivotId, new HashSet<Integer>());
            }
            pivotId2cell.get(pivotId).add(pointId);
        }

        final Collection<AbstractDelaunayGraph> delaunayGraphs = new ArrayList<>();

        for (final Collection<Integer> cell : pivotId2cell.values()) {
            delaunayGraphs.add(build(cell));
        }

        return new ImmutablePair<>(delaunayGraphs, pointId2pivotId);
    }

    public class BindDelaunayGraph extends WalkableDelaunayGraph {
        public BindDelaunayGraph(@NotNull final Collection<Integer> pointIds) {
            this(pointIds, 0);
        }

        public BindDelaunayGraph(@NotNull final Collection<Integer> pointIds, final int level) {
            super(pointIds);

            if (pointIds.size() <= dim()) {
                return;
            }

            final Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> pair = split(pointIds, division, level);
            final Map<Integer, Integer> pointId2pivotId = pair.getValue();

            final Collection<Integer> bindPointIds = new HashSet<>();
            final Graph removedGraph = new Graph();
            for (final AbstractDelaunayGraph delaunayGraph : pair.getKey()) {
                //Collection<Integer> outsidePointIds = new ArrayList(pointIds);
                //outsidePointIds.removeAll(delaunayGraph.pointIds);

                bindPointIds.addAll(delaunayGraph.getBorderVertices());
                removedGraph.addGraph(removeCreepSimplexes(delaunayGraph));
                addTriangulation(delaunayGraph);
            }

            bindPointIds.addAll(removedGraph.getVertices());

            final AbstractDelaunayGraph bindDelanayGraph;
            if (bindPointIds.size() != pointIds.size()) {
                bindDelanayGraph = build(bindPointIds);
            } else {
                bindDelanayGraph = nativeDelaunayGraphBuilder.build(bindPointIds);
            }

            final Graph newEdges = new Graph();
            for (final Edge edge : bindDelanayGraph) {
                final int u = edge.getFirst();
                final int v = edge.getSecond();
                if (!pointId2pivotId.get(u).equals(pointId2pivotId.get(v))) {
                    addEdge(u, v);
                    newEdges.addEdge(u, v);
                } else if (removedGraph.containsEdge(u, v)) {
                    addEdge(u, v);
                }
            }

            for (final Simplex simplex : bindDelanayGraph.getSimplexes()) {
                if (containsGraph(simplex.toGraph())) {
                    for (final Edge edge : newEdges) {
                        if (simplex.toGraph().containsEdge(edge)) {
                            addSimplex(simplex);
                            break;
                        }
                    }
                }
            }
        }
    }
}

package ru.spbu.astro.delaunay;

import javafx.util.Pair;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Triangulation;

import java.util.*;

@Deprecated
public class BindDelaunayGraphBuilder extends WalkableDelaunayGraphBuilder {

    private NativeDelaunayGraphBuilder nativeDelaunayGraphBuilder;
    private int m;

    public BindDelaunayGraphBuilder(final Collection<Point> points, int m) {
        super(points);
        this.m = m;
        nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(points);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds, int level) {
        return new BindDelaunayGraph(pointIds, level);
    }

    public class BindDelaunayGraph extends WalkableDelaunayGraph {

        public BindDelaunayGraph(Collection<Integer> pointIds) {
            this(pointIds, 0);
        }

        public BindDelaunayGraph(Collection<Integer> pointIds, int level) {
            super(pointIds);

            if (pointIds.size() <= dim) {
                borderVertices.addAll(pointIds);
                return;
            }

            Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> pair = split(pointIds, m, level);
            Map<Integer, Integer> pointId2pivotId = pair.getValue();

            Collection<Integer> bindPointIds = new HashSet();
            Graph removedGraph = new Graph();
            for (AbstractDelaunayGraph delaunayGraph : pair.getKey()) {
                //Collection<Integer> outsidePointIds = new ArrayList(pointIds);
                //outsidePointIds.removeAll(delaunayGraph.pointIds);

                bindPointIds.addAll(delaunayGraph.getBorderVertices());
                removedGraph.addGraph(removeCreepSimplexes(delaunayGraph));
                addTriangulation(delaunayGraph);
            }

            bindPointIds.addAll(removedGraph.getVertices());

            AbstractDelaunayGraph bindDelanayGraph;
            if (bindPointIds.size() != pointIds.size()) {
                bindDelanayGraph = build(bindPointIds);
            } else {
                bindDelanayGraph = nativeDelaunayGraphBuilder.build(bindPointIds);
            }

            borderVertices = bindDelanayGraph.getBorderVertices();

            Graph newEdges = new Graph();
            for (Edge edge : bindDelanayGraph) {
                int u = edge.getFirst();
                int v = edge.getSecond();
                if (!pointId2pivotId.get(u).equals(pointId2pivotId.get(v))) {
                    addEdge(u, v);
                    newEdges.addEdge(u, v);
                } else if (removedGraph.containsEdge(u, v)) {
                    addEdge(u, v);
                }
            }

            for (Simplex simplex : bindDelanayGraph.getSimplexes()) {
                if (containsGraph(simplex.toGraph())) {
                    for (Edge edge : newEdges) {
                        if (simplex.toGraph().containsEdge(edge)) {
                            simplex.setLevel(level);
                            addSimplex(simplex);
                            break;
                        }
                    }
                }
            }
        }
    }
}

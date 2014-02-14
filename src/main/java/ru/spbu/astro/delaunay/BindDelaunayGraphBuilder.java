package ru.spbu.astro.delaunay;

import javafx.util.Pair;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;

import java.util.*;

@Deprecated
public final class BindDelaunayGraphBuilder extends WalkableDelaunayGraphBuilder {

    private final int division;
    private final NativeDelaunayGraphBuilder nativeDelaunayGraphBuilder;

    public BindDelaunayGraphBuilder(final Collection<Point> points, int division) {
        super(points);
        this.division = division;
        nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(id2point);
    }

    public BindDelaunayGraphBuilder(final Map<Integer, Point> id2point, int division) {
        super(id2point);
        this.division = division;
        nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(id2point);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds) {
        return new BindDelaunayGraph(pointIds);
    }

    public Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> split(final Collection<Integer> pointIds, int m, int level) {

        List<Integer> perm = new ArrayList<>();
        for (Integer pointId : pointIds)  {
            perm.add(pointId);
        }
        Collections.shuffle(perm);


        List<Integer> pivotIds = new ArrayList<>();
        for (int i = 0; i < Math.min(m, perm.size()); ++i) {
            pivotIds.add(perm.get(i));
        }

        Map<Integer, Integer> pointId2pivotId = new HashMap();
        for (Integer pointId : pointIds) {
            pointId2pivotId.put(pointId, pivotIds.get(0));
            Point p = id2point.get(pointId);
            for (Integer pivotId : pivotIds) {
                if (p.distance2to(id2point.get(pivotId)) < p.distance2to(id2point.get(pointId2pivotId.get(pointId)))) {
                    pointId2pivotId.put(pointId, pivotId);
                }
            }
        }

        Map<Integer, Collection<Integer>> pivotId2cell = new HashMap();

        for (Map.Entry<Integer, Integer> entry : pointId2pivotId.entrySet()) {
            int pointId = entry.getKey();
            int pivotId = entry.getValue();
            if (!pivotId2cell.containsKey(pivotId)) {
                pivotId2cell.put(pivotId, new HashSet());
            }
            pivotId2cell.get(pivotId).add(pointId);
        }

        Collection<AbstractDelaunayGraph> delaunayGraphs = new ArrayList();

        for (Collection<Integer> cell : pivotId2cell.values()) {
            delaunayGraphs.add(build(cell));
        }

        return new Pair(delaunayGraphs, pointId2pivotId);
    }

    public class BindDelaunayGraph extends WalkableDelaunayGraph {

        public BindDelaunayGraph(Collection<Integer> pointIds) {
            this(pointIds, 0);
        }

        public BindDelaunayGraph(Collection<Integer> pointIds, int level) {
            super(pointIds);

            if (pointIds.size() <= dim()) {
                return;
            }

            Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> pair = split(pointIds, division, level);
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

            borderVertices.addAll(bindDelanayGraph.getBorderVertices());

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
                            addSimplex(simplex);
                            break;
                        }
                    }
                }
            }
        }

    }

}

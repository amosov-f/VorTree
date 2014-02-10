package ru.spbu.astro.search;

import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.Schema.msg;


import java.util.*;

public final class VorTreeBuilder extends AbstractVorTreeBuilder {
    public VorTreeBuilder(Iterable<Point> points, int division) {
        super(points, division);
    }

    public VorTreeBuilder(Collection<Integer> pointIds, int division) {
        super(pointIds, division);
    }

    @Override
    public AbstractDelaunayGraph build(final Collection<Integer> pointIds) {
        return new VorTree(pointIds);
    }

    public class VorTree extends AbstractVorTree {

        public static VorTree fromMessage(final msg message){

        }

        public msg toMessage(){
            final msg.Builder builder = msg.newBuilder();
            toMessage(builder);
            return builder.build();
        }

        public void toMessage(msg.Builder builder){
            super.toMessage(builder);
        }
        
        public VorTree(final Collection<Integer> pointIds) {
            super(pointIds);

            if (pointIds.size() <= dim()) {
                return;
            }

            final Map<Integer, Integer> pointId2pivotId = new HashMap<>();
            if (pointIds.size() > division) {
                final List<Integer> pointIdList = new ArrayList<>(pointIds);
                Collections.shuffle(pointIdList);
                final List<Integer> pivotIds = pointIdList.subList(0, Math.min(division, pointIdList.size()));

                final VorTree pivotVorTree = (VorTree) build(pivotIds);
                for (int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pivotVorTree.getNearestNeighbor(id2point.get(pointId)));
                }
            } else {
                for (int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pointId);
                }
            }

            final Map<Integer, List<Integer>> pivotId2pointIds = new HashMap<>();
            for (int pointId : pointId2pivotId.keySet()) {
                int pivotId = pointId2pivotId.get(pointId);
                if (!pivotId2pointIds.containsKey(pivotId)) {
                    pivotId2pointIds.put(pivotId, new ArrayList<Integer>());
                }
                pivotId2pointIds.get(pivotId).add(pointId);
            }

            final Collection<List<Integer>> cells = pivotId2pointIds.values();

            final List<VorTree> sons = new ArrayList<>();
            for (final List<Integer> cell : cells) {
                final VorTree t = (VorTree) build(cell);
                sons.add(t);
                rTree.sons.add(t.rTree);
            }

            final Set<Integer> bindPointIds = new HashSet<>();
            final Graph removedGraph = new Graph();
            for (final VorTree t : sons) {
                bindPointIds.addAll(t.getBorderVertices());
                removedGraph.addGraph(removeCreepSimplexes(t));
                addTriangulation(t);
            }
            bindPointIds.addAll(removedGraph.getVertices());

            final AbstractDelaunayGraph bindDelanayGraph;
            if (bindPointIds.size() != pointIds.size()) {
                bindDelanayGraph = build(bindPointIds);
            } else {
                bindDelanayGraph = binder.build(bindPointIds);
            }

            borderVertices.addAll(bindDelanayGraph.getBorderVertices());

            final Graph newEdges = new Graph();
            for (final Edge edge : bindDelanayGraph) {
                int u = edge.getFirst();
                int v = edge.getSecond();
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

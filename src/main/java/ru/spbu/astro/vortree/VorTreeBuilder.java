package ru.spbu.astro.vortree;

import com.google.common.collect.Iterables;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VisadDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.WalkableDelaunayGraphBuilder;
import ru.spbu.astro.model.*;

import java.util.*;

public class VorTreeBuilder extends WalkableDelaunayGraphBuilder {
    private int m;

    AbstractDelaunayGraphBuilder nativeDelaunayGraphBuilder;


    public VorTreeBuilder(final Collection<Point> points, int m) {
        super(points);
        this.m = m;
        nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(points);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds, int level) {
        return new VorTree(pointIds, m);
    }

    public class VorTree extends WalkableDelaunayGraph implements Index {
        private RTree rTree;

        private ArrayList<VorTree> sons = new ArrayList();
        private VorTree pivotVorTree;

        public VorTree(Collection<Integer> pointIds, int level) {
            super(pointIds);

            rTree = new RTree(pointIds);

            if (pointIds.size() <= dim) {
                borderVertices.addAll(pointIds);
                return;
            }

            Map<Integer, Integer> pointId2pivotId = new HashMap();
            if (pointIds.size() > m) {
                ArrayList<Integer> pointIdList = new ArrayList(pointIds);
                Collections.shuffle(pointIdList);
                List<Integer> pivotIds = pointIdList.subList(0, Math.min(m, pointIdList.size()));

                pivotVorTree = (VorTree) build(pivotIds);
                for (int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pivotVorTree.getNearestNeighbor(getPoint(pointId)));
                }
            } else {
                for (int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pointId);
                }
            }

            HashMap<Integer, ArrayList<Integer>> pivotId2pointIds = new HashMap();
            for (int pointId : pointId2pivotId.keySet()) {
                int pivotId = pointId2pivotId.get(pointId);
                if (!pivotId2pointIds.containsKey(pivotId)) {
                    pivotId2pointIds.put(pivotId, new ArrayList());
                }
                pivotId2pointIds.get(pivotId).add(pointId);
            }

            Collection<ArrayList<Integer>> cells = pivotId2pointIds.values();

            for (List<Integer> cell : cells) {
                VorTree vorTree = (VorTree) build(cell);
                sons.add(vorTree);
                rTree.sons.add(vorTree.rTree);
            }

            Collection<Integer> bindPointIds = new HashSet();
            Graph removedGraph = new Graph();
            for (VorTree vorTree : sons) {
                bindPointIds.addAll(vorTree.getBorderVertices());
                removedGraph.addGraph(removeCreepSimplexes(vorTree));
                addTriangulation(vorTree);
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

        @Override
        public boolean isCreep(Simplex s) {
            Ball b = new Ball(getPoints(s));
            Point center = b.getCenter();
            //return b.contains(getPoint(getNearestNeighbor(center)));

            for (VorTree vorTree : sons) {
                int curNN = vorTree.getNearestNeighbor(center);
                if (b.contains(getPoint(curNN))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getNearestNeighbor(final Point p) {
            PriorityQueue<RTree> heap = new PriorityQueue<RTree>(rTree.sons.size() + 1, new Comparator<RTree>() {
                @Override
                public int compare(RTree v1, RTree v2) {
                    return Long.compare(v1.cover.distance2to(p), v2.cover.distance2to(p));
                }
            });
            heap.add(rTree);
            long bestDist2 = Long.MAX_VALUE;
            int bestNN = -1;
            while (!heap.isEmpty()) {
                RTree u = heap.poll();
                if (u.sons.isEmpty()) {
                    for (int pointId : u.pointIds) {
                        if (getPoint(pointId).distance2to(p) < bestDist2) {
                            bestNN = pointId;
                            bestDist2 = getPoint(pointId).distance2to(p);
                        }
                    }
                    if (contains(bestNN, p)) {
                        return bestNN;
                    }
                } else {
                    for (RTree v : u.sons) {
                        heap.add(v);
                    }
                }
            }
            return -1;
        }

        private class RTree {
            private Rectangle cover;
            private ArrayList<RTree> sons = new ArrayList();
            private ArrayList<Integer> pointIds;

            private RTree(Collection<Integer> pointIds) {
                this.cover = new Rectangle(VorTreeBuilder.this.getPoints(pointIds));
                this.pointIds = new ArrayList(pointIds);
            }
        }
    }
}

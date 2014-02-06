package ru.spbu.astro.vortree;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.model.*;

import java.util.*;

public class VorTreeBuilder extends AbstractDelaunayGraphBuilder {
    private Map<Integer, Point> id2point = new HashMap();
    private int m;

    NativeDelaunayGraphBuilder nativeDelaunayGraphBuilder;


    public VorTreeBuilder(final Collection<Point> points, int m) {
        super(points);
        this.m = m;
        nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(points);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds, int level) {
        return new VorTree(pointIds, m);
    }

    public List<Point> getPoints(Collection<Integer> pointIds) {
        List<Point> points = new ArrayList();
        for (int pointId : pointIds) {
            points.add(id2point.get(pointId));
        }
        return points;
    }

    public Point getPoint(int pointId) {
        return id2point.get(pointId);
    }

    public class VorTree extends AbstractDelaunayGraph implements Index {
        private List<Integer> pointIds;

        RTree rTree;


        public VorTree(Collection<Integer> pointIds, int level) {
            super(pointIds);

            rTree = new RTree(pointIds);

            if (pointIds.size() <= dim) {
                return;
            }

            this.pointIds = new ArrayList(pointIds);

            Collections.shuffle(this.pointIds);
            List<Integer> pivotIds = this.pointIds.subList(0, m);

            VorTree pivotVorTree = (VorTree) build(pivotIds);
            Map<Integer, Integer> pointId2pivotId = new HashMap();
            for (int pointId : pointIds) {
                pointId2pivotId.put(pointId, pivotVorTree.getNearestNeighbor(getPoint(pointId)));
            }

            Map<Integer, List<Integer>> pivotId2pointIds = new HashMap();
            for (int pointId : pointId2pivotId.keySet()) {
                pivotId2pointIds.get(pointId2pivotId.get(pointId));
            }

            Collection<List<Integer>> cells = pivotId2pointIds.values();

            ArrayList<VorTree> vorTrees = new ArrayList();
            for (List<Integer> cell : cells) {
                vorTrees.add((VorTree) build(cell));

            }

            Collection<Integer> bindPointIds = new HashSet();
            Graph removedGraph = new Graph();

            for (VorTree vorTree : vorTrees) {
                //ArrayList outsidePointIds = new ArrayList(pointIds);
                //outsidePointIds.removeAll(t.pointIds);
                AbstractDelaunayGraph g = (AbstractDelaunayGraph) vorTree.clone();
                bindPointIds.addAll(g.getBorderVertices());
                removedGraph.addGraph(g.removeCreepSimplexes(vorTree.pointIds));
                addTriangulation(g);
            }

            bindPointIds.addAll(removedGraph.getVertices());

            AbstractDelaunayGraph bindDelanayGraph;
            if (bindPointIds.size() != pointIds.size()) {
                bindDelanayGraph = build(bindPointIds);
            } else {
                bindDelanayGraph = nativeDelaunayGraphBuilder.build(bindPointIds);
            }

            Graph newEdges = new Graph();
            for (Graph.Edge edge : bindDelanayGraph) {
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
                    int count = 0;
                    for (Graph.Edge edge : newEdges) {
                        if (simplex.toGraph().containsEdge(edge)) {
                            count++;
                        }
                    }
                    if (count >= 1) {
                        simplex.setLevel(level);
                        addSimplex(simplex);
                    }

                }
            }

        }

        @Override
        public int getNearestNeighbor(final Point p) {
            PriorityQueue<RTree> heap = new PriorityQueue<RTree>(rTree.sons.size(), new Comparator<RTree>() {
                @Override
                public int compare(RTree v1, RTree v2) {
                    return Long.compare(v1.cover.distance2to(p), v2.cover.distance2to(p));
                }
            });
            heap.add(rTree);
            long bestDist = Long.MAX_VALUE;
            int bestNN = -1;
            while (!heap.isEmpty()) {
                RTree u = heap.poll();
                if (u.sons.isEmpty()) {
                    for (int pointId : u.pointIds) {
                        if (getPoint(pointId).distance2to(p) < bestDist) {
                            bestNN = pointId;
                            bestDist = getPoint(pointId).distance2to(p);
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
    }

    private class RTree {
        private Rectangle cover;
        private ArrayList<RTree> sons = new ArrayList();
        private Collection<Integer> pointIds;

        private RTree(Collection<Integer> pointIds) {
            this.cover = new Rectangle(getPoints(pointIds));
            this.pointIds = pointIds;
        }
    }
}

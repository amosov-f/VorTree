package ru.spbu.astro.vortree;

import com.google.common.collect.Iterables;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.model.*;

import java.util.*;

public class VorTreeBuilder extends AbstractDelaunayGraphBuilder {
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

    public class VorTree extends AbstractDelaunayGraph implements Index {
        private RTree rTree;

        private Collection<Integer> borderVertices = new ArrayList();
        private HashMap<Simplex, Collection<Simplex> > side2simplexes = new HashMap();

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
        public Graph removeCreepSimplexes(AbstractDelaunayGraph delaunayGraph) {
            VorTree t = (VorTree) delaunayGraph;

            HashSet<Simplex> visitedSimplexes = new HashSet();
            Graph removedGraph = new Graph();

            for (Simplex s : t.getBorderSimplexes()) {
                removedGraph.addGraph(dfs(t, s, visitedSimplexes));
            }

            return removedGraph;
        }

        private Graph dfs(VorTree t, Simplex u, HashSet<Simplex> visitedSimplexes) {
            if (visitedSimplexes.contains(u)) {
                return new Graph();
            }
            visitedSimplexes.add(u);

            if (!isCreep(u)) {
                return new Graph();
            }

            Graph removedGraph = new Graph();
            for (Simplex v : t.getNeighborSimplexes(u)) {
                removedGraph.addGraph(dfs(t, v, visitedSimplexes));
            }

            removedGraph.addGraph(t.removeSimplex(u));

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

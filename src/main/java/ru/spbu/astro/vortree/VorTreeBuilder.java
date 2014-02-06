package ru.spbu.astro.vortree;

import javafx.util.Pair;
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
        RTree rTree;

        Collection<Integer> borderVertices = new ArrayList();
        HashMap<Simplex, Collection<Simplex> > side2simplexes = new HashMap();

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

                VorTree pivotVorTree = (VorTree) build(pivotIds);
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

            ArrayList<VorTree> vorTrees = new ArrayList();
            for (List<Integer> cell : cells) {
                VorTree vorTree = (VorTree) build(cell);
                vorTrees.add(vorTree);
                rTree.sons.add(vorTree.rTree);
            }

            Collection<Integer> bindPointIds = new HashSet();
            Graph removedGraph = new Graph();
            for (VorTree vorTree : vorTrees) {
                bindPointIds.addAll(vorTree.getBorderVertices());
                removedGraph.addGraph(vorTree.removeCreepSimplexes(pointIds));
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
        public int getNearestNeighbor(final Point p) {
            PriorityQueue<RTree> heap = new PriorityQueue<RTree>(rTree.sons.size() + 1, new Comparator<RTree>() {
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

        @Override
        public Graph removeCreepSimplexes(Iterable<Integer> pointIds) {
            HashSet<Simplex> visitedSimplexes = new HashSet();
            Graph deletedGraph = new Graph();

            for (Simplex t : getBorderSimplexes()) {
                deletedGraph.addGraph(dfs(t, visitedSimplexes, pointIds));
            }

            return deletedGraph;
        }

        private Graph dfs(Simplex ut, HashSet<Simplex> visitedSimplexes, Iterable<Integer> pointIds) {
            if (visitedSimplexes.contains(ut)) {
                return new Graph();
            }
            visitedSimplexes.add(ut);

            if (!isCreep(ut, pointIds)) {
                return new Graph();
            }

            Graph g = new Graph();
            for (Simplex vt : getNeighborSimplexes(ut)) {
                g.addGraph(dfs(vt, visitedSimplexes, pointIds));
            }

            for (Simplex side : ut.getSides()) {
                side2simplexes.get(side).remove(ut);
            }

            removeGraph(ut.toGraph());
            simplexes.remove(ut);
            g.addGraph(ut.toGraph());

            return g;
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
        public void addSimplex(Simplex t) {
            super.addSimplex(t);
            for (Simplex side : t.getSides()) {
                if (!side2simplexes.containsKey(side)) {
                    side2simplexes.put(side, new HashSet());
                }
                side2simplexes.get(side).add(t);
            }
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
}

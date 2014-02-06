package ru.spbu.astro.delaunay;

import javafx.util.Pair;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;

import java.util.*;

@Deprecated
public class VorDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {

    private NativeDelaunayGraphBuilder nativeDelaunayGraphBuilder;
    private int m;

    public VorDelaunayGraphBuilder(final Collection<Point> points, int m) {
        super(points);
        this.m = m;
        nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(points);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds, int level) {
        return new VorDelaunayGraph(pointIds, level);
    }

    public class VorDelaunayGraph extends AbstractDelaunayGraph {
        Collection<Integer> borderVertices = new ArrayList();
        HashMap<Simplex, Collection<Simplex> > side2simplexes = new HashMap();

        public VorDelaunayGraph(Collection<Integer> pointIds) {
            this(pointIds, 0);
        }

        public VorDelaunayGraph(Collection<Integer> pointIds, int level) {
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
                Collection<Integer> outsidePointIds = new ArrayList(pointIds);
                outsidePointIds.removeAll(delaunayGraph.pointIds);

                bindPointIds.addAll(delaunayGraph.getBorderVertices());

                removedGraph.addGraph(delaunayGraph.removeCreepSimplexes(pointIds));

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
                    int count = 0;
                    for (Edge edge : newEdges) {
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
    }
}

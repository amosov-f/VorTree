package ru.spbu.astro.delaunay;

import javafx.util.Pair;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Triangulation;

import java.util.*;

@Deprecated
public class BindDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {

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

    public class BindDelaunayGraph extends AbstractDelaunayGraph {
        Collection<Integer> borderVertices = new ArrayList();
        HashMap<Simplex, Collection<Simplex> > side2simplexes = new HashMap();

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

        @Override
        public Graph removeCreepSimplexes(AbstractDelaunayGraph delaunayGraph) {
            BindDelaunayGraph g = (BindDelaunayGraph) delaunayGraph;

            HashSet<Simplex> visitedSimplexes = new HashSet();
            Graph removedGraph = new Graph();

            for (Simplex s : g.getBorderSimplexes()) {
                removedGraph.addGraph(dfs(g, s, visitedSimplexes));
            }

            return removedGraph;
        }

        private Graph dfs(BindDelaunayGraph g, Simplex u, HashSet<Simplex> visitedSimplexes) {
            if (visitedSimplexes.contains(u)) {
                return new Graph();
            }
            visitedSimplexes.add(u);

            if (!isCreep(u)) {
                return new Graph();
            }

            Graph removedGraph = new Graph();
            for (Simplex v : g.getNeighborSimplexes(u)) {
                removedGraph.addGraph(dfs(g, v, visitedSimplexes));
            }

            removedGraph.addGraph(g.removeSimplex(u));

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
    }
}

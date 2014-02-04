package ru.spbu.astro.delaunay;

import javafx.util.Pair;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;

import java.util.*;

public class VorDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {

    private NativeDelaunayGraphBuilder nativeDelaunayGraphBuilder;

    public VorDelaunayGraphBuilder(final Collection<Point> points, int m) {
        super(points, m);

        nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(points, m);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds, int level) {
        return new VorDelaunayGraph(pointIds, level);
    }


    public class VorDelaunayGraph extends AbstractDelaunayGraph {
        Collection<Integer> borderVertices = new ArrayList();
        Set<Simplex> visitedSimplexes = new HashSet();
        HashMap<Simplex, Collection<Simplex> > side2triangles = new HashMap();


        public VorDelaunayGraph(Collection<Integer> pointIds) {
            this(pointIds, 0);
        }

        public VorDelaunayGraph(Collection<Integer> pointIds, int level) {
            super(pointIds);

            if (pointIds.size() <= DIM) {
                borderVertices.addAll(pointIds);
                return;
            }

            Pair<Collection<AbstractDelaunayGraph>, Map<Integer, Integer>> pair = split(pointIds, level);
            Map<Integer, Integer> pointId2pivotId = pair.getValue();


            Collection<Integer> bindPointIds = new HashSet();
            Graph removedGraph = new Graph();
            for (AbstractDelaunayGraph delaunayGraph : pair.getKey()) {
                Collection<Integer> outsidePointIds = new ArrayList(pointIds);
                outsidePointIds.removeAll(delaunayGraph.pointIds);

                //bindPointIds.addAll(delaunayGraph.getBindPointIds(outsidePointIds));
                bindPointIds.addAll(delaunayGraph.getBorderVertices());

                removedGraph.addGraph(delaunayGraph.removeCreepTriangles(outsidePointIds));

                addGraph(delaunayGraph);
                addTriangles(delaunayGraph.getSimplexes());
            }


            bindPointIds.addAll(removedGraph.getVertices());


            //System.out.println(removedGraph.getVertices().size() + " " + bindPointIds.size());

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
                        addTriangle(simplex);
                    }

                }
            }
        }

        @Override
        public Graph removeCreepTriangles(Collection<Integer> pointIds) {
            visitedSimplexes = new HashSet();

            Graph deletedGraph = new Graph();

            for (Simplex t : getBorderTriangles()) {
                deletedGraph.addGraph(dfs(t, pointIds));
            }

            return deletedGraph;
        }

        private Graph dfs(Simplex ut, Collection<Integer> pointIds) {
            if (visitedSimplexes.contains(ut)) {
                //System.out.println(visitedSimplexes.size());
                return new Graph();
            }
            visitedSimplexes.add(ut);

            if (!isCreep(ut, pointIds)) {
                return new Graph();
            }

            Graph g = new Graph();
            for (Simplex vt : getNeighborTriangles(ut)) {
                g.addGraph(dfs(vt, pointIds));
            }

            for (Simplex side : ut.getSideTriangles()) {
                side2triangles.get(side).remove(ut);
            }

            removeGraph(ut.toGraph());
            simplexes.remove(ut);
            g.addGraph(ut.toGraph());

            return g;
        }

        protected Collection<Simplex> getBorderTriangles() {
            Set<Simplex> borderSimplexes = new HashSet();
            for (Simplex side : side2triangles.keySet()) {
                if (side2triangles.get(side).size() == 1) {
                    borderSimplexes.addAll(side2triangles.get(side));
                }
            }
            return borderSimplexes;
        }

        public void addTriangle(Simplex t) {
            simplexes.add(t);
            for (Simplex side : t.getSideTriangles()) {
                if (!side2triangles.containsKey(side)) {
                    side2triangles.put(side, new HashSet());
                }
                side2triangles.get(side).add(t);
            }
        }

        public void addTriangles(Collection<Simplex> simplexes) {
            for (Simplex t : simplexes) {
                addTriangle(t);
            }
        }

        public Collection<Simplex> getNeighborTriangles(Simplex u) {
            Set<Simplex> neighborSimplexes = new HashSet();
            for (Simplex side : u.getSideTriangles()) {
                for (Simplex v : side2triangles.get(side)) {
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

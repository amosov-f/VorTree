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
        Set<Triangle> visitedTriangles = new HashSet();
        HashMap<Triangle, Collection<Triangle> > side2triangles = new HashMap();


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
                bindPointIds.addAll(delaunayGraph.getBindPointIds(pointIds));
                removedGraph.addGraph(delaunayGraph.removeCreepTriangles(pointIds));
                addGraph(delaunayGraph);
                addTriangles(delaunayGraph.getTriangles());
            }
            //System.out.println("bind: " + bindPointIds);

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

            for (Triangle triangle : bindDelanayGraph.getTriangles()) {
                if (containsGraph(triangle.toGraph())) {
                    int count = 0;
                    for (Edge edge : newEdges) {
                        if (triangle.toGraph().containsEdge(edge)) {
                            count++;
                        }
                    }
                    if (count >= 1) {
                        triangle.setLevel(level);
                        addTriangle(triangle);
                    }

                }
            }
        }

        @Override
        public Graph removeCreepTriangles(Collection<Integer> pointIds) {
            visitedTriangles = new HashSet();

            Graph deletedGraph = new Graph();

            for (Triangle t : getBorderTriangles()) {
                deletedGraph.addGraph(dfs(t, pointIds));
            }

            return deletedGraph;
        }

        private Graph dfs(Triangle ut, Collection<Integer> pointIds) {
            if (visitedTriangles.contains(ut)) {
                //System.out.println(visitedTriangles.size());
                return new Graph();
            }
            visitedTriangles.add(ut);

            if (!new AbstractDelaunayGraphBuilder.Triangle(ut).isCreep(pointIds)) {
                return new Graph();
            }

            Graph g = new Graph();
            for (Triangle vt : getNeighborTriangles(ut)) {
                g.addGraph(dfs(vt, pointIds));
            }

            for (Triangle side : ut.getSideTriangles()) {
                side2triangles.get(side).remove(ut);
            }

            removeGraph(ut.toGraph());
            triangles.remove(ut);
            g.addGraph(ut.toGraph());

            return g;
        }

        protected Collection<Triangle> getBorderTriangles() {
            Set<Triangle> borderTriangles = new HashSet();
            for (Triangle side : side2triangles.keySet()) {
                if (side2triangles.get(side).size() == 1) {
                    borderTriangles.addAll(side2triangles.get(side));
                }
            }
            return borderTriangles;
        }

        public void addTriangle(Triangle t) {
            triangles.add(t);
            for (Triangle side : t.getSideTriangles()) {
                if (!side2triangles.containsKey(side)) {
                    side2triangles.put(side, new HashSet());
                }
                side2triangles.get(side).add(t);
            }
        }

        public void addTriangles(Collection<Triangle> triangles) {
            for (Triangle t : triangles) {
                addTriangle(t);
            }
        }

        public Collection<Triangle> getNeighborTriangles(Triangle u) {
            Set<Triangle> neighborTriangles = new HashSet();
            for (Triangle side : u.getSideTriangles()) {
                for (Triangle v : side2triangles.get(side)) {
                    if (!u.equals(v)) {
                        neighborTriangles.add(v);
                    }
                }
            }
            return neighborTriangles;
        }

        @Override
        public Collection<Integer> getBorderVertices() {
            return borderVertices;
        }

    }



}

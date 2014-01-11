package ru.spbu.astro.delaunay;

import ru.spbu.astro.utility.PointGenerator;

import java.util.*;

public class DelaunayBinder {

    private AbstractDelaunayGraphBuilder delaunayGraphBuilder;
    private Collection<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> delaunayGraphs;


    public DelaunayBinder(int n, int m) {


        delaunayGraphBuilder = new VisadDelaunayGraphBuilder(PointGenerator.nextUniforms(n), m);

        delaunayGraphs = delaunayGraphBuilder.split().getKey();
    }


    public Collection<Integer> getBindPointIds() {

        Collection<Integer> border = new HashSet();

        for (AbstractDelaunayGraphBuilder.AbstractDelaunayGraph delaunayGraph : delaunayGraphs) {
            border.addAll(delaunayGraph.getBindPointIds());
            for (AbstractDelaunayGraphBuilder.AbstractDelaunayGraph.Triangle triangle : delaunayGraph.getCreepTriangles()) {
                border.addAll(triangle.getVertices());
            }
        }

        return border;
    }

    public List<AbstractDelaunayGraphBuilder.Triangle> getCreepTriangles() {
        List<AbstractDelaunayGraphBuilder.Triangle> creepTriangles = new ArrayList();
        for (AbstractDelaunayGraphBuilder.AbstractDelaunayGraph delaunayGraph : delaunayGraphs) {
            creepTriangles.addAll(delaunayGraph.getCreepPointTriangles());
        }
        return creepTriangles;
    }

    public AbstractDelaunayGraphBuilder.AbstractDelaunayGraph getDelaunayGraph() {
        return delaunayGraphBuilder.build();
    }

    public Collection<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> getDelaunayGraphs() {
        return delaunayGraphs;
    }

    public AbstractDelaunayGraphBuilder.AbstractDelaunayGraph getBindGraph() {
        return delaunayGraphBuilder.build(getBindPointIds());
    }

    public double getRate() {
        return 1.0 * getBindPointIds().size() / delaunayGraphBuilder.id2point.size();
    }
}

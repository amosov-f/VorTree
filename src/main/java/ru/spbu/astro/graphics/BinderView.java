package ru.spbu.astro.graphics;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VisadDelaunayGraphBuilder;
import ru.spbu.astro.model.Simplex;
import ru.spbu.astro.model.Rectangle;
import ru.spbu.astro.utility.PointGenerator;

import java.awt.*;
import java.util.*;
import java.util.List;

@Deprecated
public class BinderView extends DelaunayGraphPainter {
    private AbstractDelaunayGraphBuilder delaunayGraphBuilder;
    private Collection<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> delaunayGraphs;

    private int n;
    private int m;

    public BinderView(int n, int m) {
        super();
        this.n = n;
        this.m = m;
        setSize(1320, 660);
        build();
    }

    public void build() {
        delaunayGraphBuilder = new VisadDelaunayGraphBuilder(
                PointGenerator.nextUniforms(n, 1000 * getWidth(), 1000 * getHeight()),
                m
        );

        delaunayGraphs = delaunayGraphBuilder.split().getKey();
    }

    @Override
    public void paint(Graphics g) {


        AbstractDelaunayGraphBuilder.AbstractDelaunayGraph delaunayGraph = delaunayGraphBuilder.build();
        Rectangle rect = delaunayGraph.getFrameRectangle();


        for (Simplex triangle : getCreepTriangles()) {
            paintTriangle(triangle, rect, g, new Color(150 + new Random().nextInt(106), 0, 0));
        }


        paintDelaunayGraph(delaunayGraph, g, new Color(0, 0, 0), 4);


        for (AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph : delaunayGraphs) {
            Color color = new Color(
                    new Random().nextInt(156) + 100,
                    new Random().nextInt(156) + 100,
                    new Random().nextInt(156) + 100
            );
            paintDelaunayGraph(graph, g, color, 4);
        }

        paintDelaunayGraph(delaunayGraph, g, new Color(100, 100, 100), 1);
    }

    private List<Simplex> getCreepTriangles() {
        List<Simplex> creepTriangles = new ArrayList();
        for (AbstractDelaunayGraphBuilder.AbstractDelaunayGraph delaunayGraph : delaunayGraphs) {
            creepTriangles.addAll(delaunayGraph.getCreepPointTriangles());
        }
        return creepTriangles;
    }
}

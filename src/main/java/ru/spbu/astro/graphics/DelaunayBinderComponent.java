package ru.spbu.astro.graphics;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.DelaunayBinder;
import ru.spbu.astro.graphics.DelaunayGraphComponent;

import java.awt.*;
import java.util.*;


public class DelaunayBinderComponent extends DelaunayGraphComponent {
    private DelaunayBinder delaunayBinder;

    private int n;
    private int m;

    public DelaunayBinderComponent(int n, int m) {
        this.n = n;
        this.m = m;
    }

    @Override
    public void paint(Graphics g) {
        delaunayBinder = new DelaunayBinder(n, m);

        for (AbstractDelaunayGraphBuilder.Triangle triangle : delaunayBinder.getCreepTriangles()) {
            paintTriangle(triangle, delaunayBinder.getDelaunayGraph(), g, new Color(150 + new Random().nextInt(106), 0, 0));
        }

        paintDelaunayGraph(delaunayBinder.getDelaunayGraph(), g, new Color(0, 0, 0), 4);

        for (AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph : delaunayBinder.getDelaunayGraphs()) {
            Color color = new Color(
                    new Random().nextInt(156) + 100,
                    new Random().nextInt(156) + 100,
                    new Random().nextInt(156) + 100
            );
            paintDelaunayGraph(graph, g, color, 4);
        }

        paintDelaunayGraph(delaunayBinder.getBindGraph(), g, new Color(100, 100, 100), 1);

    }



}

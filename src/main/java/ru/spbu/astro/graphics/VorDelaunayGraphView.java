package ru.spbu.astro.graphics;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.BindDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VisadDelaunayGraphBuilder;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Simplex;
import ru.spbu.astro.model.Rectangle;
import ru.spbu.astro.utility.PointGenerator;

import java.awt.*;
import java.util.*;

@Deprecated
public class VorDelaunayGraphView extends DelaunayGraphPainter {

    private int n;
    private int m;

    AbstractDelaunayGraphBuilder.AbstractDelaunayGraph vorDelaunayGraph;
    AbstractDelaunayGraphBuilder.AbstractDelaunayGraph visadDelaunayGraph;

    public VorDelaunayGraphView(int n, int m) {
        super();
        this.n = n;
        this.m = m;
        setSize(1320, 660);
        build();
    }

    public void build() {
        Collection<Point> points = PointGenerator.nextUniforms(
                n,
                new Point(new long[]{1000 * getWidth(), 1000 * getHeight()})
        );

        vorDelaunayGraph = new BindDelaunayGraphBuilder(points, m).build();
        visadDelaunayGraph = new VisadDelaunayGraphBuilder(points).build();
    }

    @Override
    public void paint(Graphics g) {

        AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph = vorDelaunayGraph;
        Rectangle rect = graph.getFrameRectangle();

        for (Simplex triangle : graph.getPointSimplexes()) {
            paintTriangle(triangle, rect, g);
        }

        //for (AbstractDelaunayGraphBuilder.Simplex triangle : graph.getCreepPointSimplexes()) {
        //    paintTriangle(triangle, rect, g, new Color(150 + new Random().nextInt(106), 0, 0));
        //}

        paintDelaunayGraph(visadDelaunayGraph, g, new Color(0, 100, 0), 4);

        paintDelaunayGraph(graph, g, new Color(130, 100, 130), 1);

        /*g.setColor(new Color(0, 100, 200));
        for (Map.Entry<Integer, Point> entry : vorDelaunayGraphBuilder.id2point.entrySet()) {
            g.drawString(entry.getKey().toString(), (int)entry.getValue().getX(), (int)entry.getValue().getY());
        }
          */


    }

}

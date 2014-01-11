package ru.spbu.astro.graphics;


import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VisadDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VorDelaunayGraphBuilder;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.utility.PointGenerator;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

public class VorDelaunayGraphComponent extends DelaunayGraphComponent {

    private int n;
    private int m;

    AbstractDelaunayGraphBuilder.AbstractDelaunayGraph vorDelaunayGraph;
    AbstractDelaunayGraphBuilder.AbstractDelaunayGraph visadDelaunayGraph;

    public VorDelaunayGraphComponent(int n, int m) {
        this.n = n;
        this.m = m;

        build();

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clear(getGraphics());

                build();

                paint(getGraphics());
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    private void build() {
        Point p;
        if (getWidth() * getHeight() == 0) {
            p = new Point(new long[]{300000, 100000});
        } else {
            p = new Point(new long[]{1000 * getWidth(), 1000 * getHeight()});
        }

        Collection<Point> points = PointGenerator.nextUniforms(n, p);

        vorDelaunayGraph = new VorDelaunayGraphBuilder(points, m).build();
        visadDelaunayGraph = new VisadDelaunayGraphBuilder(points, m).build();
    }

    @Override
    public void paint(Graphics g) {

        AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph = vorDelaunayGraph;

        for (AbstractDelaunayGraphBuilder.Triangle triangle : graph.getPointTriangles()) {
            paintTriangle(triangle, graph, g);
        }

        for (AbstractDelaunayGraphBuilder.Triangle triangle : graph.getCreepPointTriangles()) {
            paintTriangle(triangle, graph, g, new Color(150 + new Random().nextInt(106), 0, 0));
        }

        paintDelaunayGraph(visadDelaunayGraph, g, new Color(0, 100, 0), 4);

        paintDelaunayGraph(graph, g, new Color(130, 100, 130), 1);

        /*g.setColor(new Color(0, 100, 200));
        for (Map.Entry<Integer, Point> entry : vorDelaunayGraphBuilder.id2point.entrySet()) {
            g.drawString(entry.getKey().toString(), (int)entry.getValue().getX(), (int)entry.getValue().getY());
        }
          */


    }


    private void clear(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}

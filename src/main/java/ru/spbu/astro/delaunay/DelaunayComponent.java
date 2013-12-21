package ru.spbu.astro.delaunay;

import java.awt.*;
import java.util.*;


public class DelaunayComponent extends Component {
    private DelaunayBinder delaunayBinder;

    private int n;
    private int m;

    public DelaunayComponent(int n, int m) {
        this.n = n;
        this.m = m;
    }

    @Override
    public void paint(Graphics g) {
        delaunayBinder = new DelaunayBinder(n, m, getWidth(), getHeight());

        //Box2D box = circle2D.boundingBox();
        //g.drawOval((int)box.getMinX(), (int)box.getMinY(), (int)box.getWidth(), (int)box.getHeight());
        for (DelaunayGraph.Triangle triangle : delaunayBinder.getCreepTriangles()) {
            paintTriangle(triangle, g);
        }


        paintDelaunayGraph(new DelaunayGraph(delaunayBinder.getPoints()), g, new Color(0, 0, 0), 4);


        for (DelaunayGraph graph : delaunayBinder.getDelaunayGraphs()) {
            Color color = new Color(
                    new Random().nextInt(156) + 100,
                    new Random().nextInt(156) + 100,
                    new Random().nextInt(156) + 100
            );
            paintDelaunayGraph(graph, g, color, 4);
        }

        paintDelaunayGraph(new DelaunayGraph(delaunayBinder.getBindPoints()), g, new Color(100, 100, 100), 1);

        //System.out.println(m + " " + 1.0 * border.size() / points.size());

    }

    private void paintDelaunayGraph(final DelaunayGraph graph, Graphics g, final Color color, int width) {
        ((Graphics2D)g).setStroke(new BasicStroke(width));
        g.setColor(color);
        for (DelaunayGraph.Edge edge : graph.getEdges()) {
            int x1 = edge.first.toAwtPoint().x;
            int y1 = edge.first.toAwtPoint().y;
            int x2 = edge.second.toAwtPoint().x;
            int y2 = edge.second.toAwtPoint().y;
            g.drawLine(x1, y1, x2, y2);
        }
    }

    private void paintTriangle(final DelaunayGraph.Triangle triangle, Graphics g) {

        int x1 = triangle.points.get(0).toAwtPoint().x;
        int y1 = triangle.points.get(0).toAwtPoint().y;

        int x2 = triangle.points.get(1).toAwtPoint().x;
        int y2 = triangle.points.get(1).toAwtPoint().y;

        int x3 = triangle.points.get(2).toAwtPoint().x;
        int y3 = triangle.points.get(2).toAwtPoint().y;

        g.setColor(new Color(255, 0, 0));
        g.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
    }

}

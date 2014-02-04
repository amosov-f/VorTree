package ru.spbu.astro.graphics;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.model.Line;
import ru.spbu.astro.model.Simplex;
import ru.spbu.astro.model.Rectangle;

import java.awt.*;
import java.awt.Point;
import java.util.Random;

@Deprecated
public abstract class DelaunayGraphPainter extends ClickableView {

    private static final int ALIGN = 5;
    Rectangle rect;

    protected void paintDelaunayGraph(AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph, Graphics g, final Color color, int width) {
        rect = graph.getFrameRectangle();

        ((Graphics2D) g).setStroke(new BasicStroke(width));

        for (Line edge : graph.getPointEdges()) {
            Point p1 = toWindow(edge.getFirst(), rect);
            Point p2 = toWindow(edge.getSecond(), rect);

            g.setColor(color);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);

            g.setColor(new Color(0, 0, 0));
            paintPoint(p1, g);
            paintPoint(p2, g);

        }
    }

    protected void paintTriangle(
            final Simplex triangle,
            Rectangle rect,
            Graphics g,
            Color color
    ) {
        Point p1 = toWindow(triangle.getVertices().get(0), rect);
        Point p2 = toWindow(triangle.getVertices().get(1), rect);
        Point p3 = toWindow(triangle.getVertices().get(2), rect);

        g.setColor(color);
        g.fillPolygon(new int[]{p1.x, p2.x, p3.x}, new int[]{p1.y, p2.y, p3.y}, 3);
    }

    protected void paintTriangle(
            final Simplex triangle,
            Rectangle rect,
            Graphics g) {
        paintTriangle(triangle, rect, g, Color.getHSBColor(240f / 360, 1f, 1 - Math.min(triangle.getLevel() * 30f / 255, 1f)));
    }

    public void paintCreepTriangles(AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph, Graphics g) {
        for (Simplex t : graph.getCreepPointTriangles()) {
            paintTriangle(t, rect, g, new Color(150 + new Random().nextInt(106), 0, 0));
        }
    }

    private Point toWindow(ru.spbu.astro.model.Point p, Rectangle rect) {
        int w = getWidth() - 2 * ALIGN;
        int h = getHeight() - 2 * ALIGN;
        long rw = rect.getWidth();
        long rh = rect.getHeight();
        long rx = p.getX() - rect.getX();
        long ry = p.getY() - rect.getY();
        int x;
        int y;
        if (w * rh > rw * h) {
            x = (int) ((w * rh - rw * h + 2 * h * rx) / (2 * rh));
            y = (int) (ry * h / rh);
        } else {
            x = (int) (rx * w / rw);
            y = (int) ((rw * h - w * rh + 2 * w * ry) / (2 * rw));
        }

        return new Point(x + ALIGN, y + ALIGN);
    }

    private void paintPoint(java.awt.Point p, Graphics g) {
        g.fillOval(p.x - 2, p.y - 2, 4, 4);
    }

}

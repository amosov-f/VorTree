package ru.spbu.astro.graphics;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.model.*;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;
import ru.spbu.astro.utility.ColorGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class CenteredView extends Component {
    private static final int ALIGN = 20;
    protected Rectangle frameRect;

    protected List<Item> items = new ArrayList();

    public enum DelaunayGraphViewMode {
        DEFAULT, NO_TRIANGLES, CREEP_ONLY;
    }

    public enum TriangleViewMode {
        DEFAULT, CREEP
    }

    public abstract class AbstractPainter<T extends Framable> implements Painter<T> {
        private static final int SIZE = 18;

        protected void handle(Graphics g) {
            g.setColor(Color.BLACK);
            g.setFont(new Font(Font.DIALOG, Font.ITALIC, SIZE));
        }

        @Override
        public void sign(T object, String signature, Graphics g) {
            handle(g);

            java.awt.Point center = toWindow(object.getFrameRectangle().getCenter());
            g.drawString(signature, center.x, center.y);
        }
    }


    public class PointPainter extends AbstractPainter<Point> {
        private Color color = Color.BLUE;
        private int size = 3;

        public PointPainter() {
        }

        public PointPainter(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public void paint(Point p, Graphics g) {
            g.setColor(color);
            g.fillOval(toWindow(p).x - size, toWindow(p).y - size, 2 * size, 2 * size);
        }

        @Override
        public void sign(Point p, String signature, Graphics g) {
            handle(g);

            java.awt.Point center = toWindow(p);
            g.drawString(signature, center.x + 5, center.y - 4);
        }
    }

    public class RectanglePainter extends AbstractPainter<Rectangle> {
        private Color color =  new Color(100, 200, 100);
        private int width = 1;

        @Override
        public void paint(Rectangle rect, Graphics g) {
            g.setColor(color);
            ((Graphics2D) g).setStroke(new BasicStroke(width));

            java.awt.Point minVertex = toWindow(rect.getMinVertex());
            java.awt.Point maxVertex = toWindow(rect.getMaxVertex());

            g.drawRect(minVertex.x, minVertex.y, maxVertex.x - minVertex.x, maxVertex.y - minVertex.y);
        }
    }

    public class BallPainter extends AbstractPainter<Ball> {
        private Color color = Color.RED;

        public BallPainter() {
        }

        public BallPainter(Color color) {
            this.color = color;
        }

        @Override
        public void paint(Ball b, Graphics g) {
            g.setColor(color);

            java.awt.Point minVertex = toWindow(b.getFrameRectangle().getMinVertex());
            java.awt.Point maxVertex = toWindow(b.getFrameRectangle().getMaxVertex());

            g.drawOval(minVertex.x, minVertex.y, maxVertex.x - minVertex.x, maxVertex.y - minVertex.y);
        }

        @Override
        public void sign(Ball b, String signature, Graphics g) {
            handle(g);

            java.awt.Point p = toWindow(b.getCenter().add(b.getRadius(), 0));
            g.drawString(signature, p.x - 15 * signature.length(), p.y - 8);
        }
    }

    public class LinePainter extends AbstractPainter<Line> {
        private Color color = new Color(130, 100, 130);
        private int width = 1;

        public LinePainter() {
        }

        public LinePainter(Color color, int width) {
            this.color = color;
            this.width = width;
        }

        @Override
        public void paint(Line line, Graphics g) {
            ((Graphics2D) g).setStroke(new BasicStroke(width));
            g.setColor(color);

            java.awt.Point p1 = toWindow(line.getFirst());
            java.awt.Point p2 = toWindow(line.getSecond());

            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    public class TrianglePainter extends AbstractPainter<Simplex> {
        TriangleViewMode mode = TriangleViewMode.DEFAULT;

        public TrianglePainter() {
        }

        public TrianglePainter(TriangleViewMode mode) {
            this.mode = mode;
        }

        @Override
        public void paint(Simplex t, Graphics g) {
            Color color = ColorGenerator.nextLight();
            if (mode == TriangleViewMode.CREEP) {
                color = ColorGenerator.nextRed();
            }

            Point v0 = t.getVertices().get(0);
            Point v1 = t.getVertices().get(1);
            Point v2 = t.getVertices().get(2);

            java.awt.Point p1 = toWindow(v0);
            java.awt.Point p2 = toWindow(v1);
            java.awt.Point p3 = toWindow(v2);

            g.setColor(color);
            g.fillPolygon(new int[]{p1.x, p2.x, p3.x}, new int[]{p1.y, p2.y, p3.y}, 3);

            LinePainter linePainter = new LinePainter(Color.BLACK, 1);
            linePainter.paint(new Line(v0, v1), g);
            linePainter.paint(new Line(v1, v2), g);
            linePainter.paint(new Line(v2, v0), g);

            PointPainter pointPainter = new PointPainter(Color.BLACK, 2);
            pointPainter.paint(v0, g);
            pointPainter.paint(v1, g);
            pointPainter.paint(v2, g);
        }

        @Override
        public void sign(Simplex t, String signature, Graphics g) {
            handle(g);

            java.awt.Point center = toWindow(t.getCenter());
            g.drawString(signature, center.x, center.y);
        }
    }

    public class DelaunayGraphPainter extends AbstractPainter<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> {

        private Color edgeColor = ColorGenerator.EDGE_DEFAULT;
        private int width = 1;
        DelaunayGraphViewMode mode = DelaunayGraphViewMode.DEFAULT;


        public DelaunayGraphPainter() {
        }

        public DelaunayGraphPainter(int width, DelaunayGraphViewMode mode) {
            this.width = width;
            this.mode = mode;
        }

        public DelaunayGraphPainter(Color edgeColor, int width, DelaunayGraphViewMode mode) {
            this.edgeColor = edgeColor;
            this.width = width;
            this.mode = mode;
        }

        @Override
        public void paint(AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph, Graphics g) {

            Composite composite = ((Graphics2D) g).getComposite();


            ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

            TrianglePainter trianglePainter = null;
            if (mode == DelaunayGraphViewMode.DEFAULT) {
                trianglePainter = new TrianglePainter(TriangleViewMode.DEFAULT);
            }

            if (trianglePainter != null) {
                for (Simplex s : graph.getPointSimplexes()) {
                    trianglePainter.paint(s, g);
                }
            }
            /*
            if (mode != DelaunayGraphViewMode.NO_TRIANGLES) {
                trianglePainter = new TrianglePainter(TriangleViewMode.CREEP);
                for (Simplex s : getCreepPointSimplexes(graph)) {
                    trianglePainter.paint(s, g);
                }
            }
            */

            ((Graphics2D) g).setComposite(composite);

            ((Graphics2D) g).setStroke(new BasicStroke(width));

            LinePainter linePainter = new LinePainter(edgeColor, width);
            PointPainter pointPainter = new PointPainter(Color.BLACK, 0);

            for (Line edge : graph.getPointEdges()) {
                linePainter.paint(edge, g);
                pointPainter.paint(edge.getFirst(), g);
                pointPainter.paint(edge.getSecond(), g);
            }
        }
    }

    private class Item {
        private Framable f;
        private Painter painter;
        private String signature;

        private Item(Framable f, Painter painter) {
            this.f = f;
            this.painter = painter;
        }

        private Item(Framable f, Painter painter, String signature) {
            this.f = f;
            this.painter = painter;
            this.signature = signature;
        }

        public void paint(Graphics g) {
            painter.paint(f, g);
            if (signature != null && !signature.isEmpty()) {
                painter.sign(f, signature, g);
            }
        }
    }

    private Painter getPainter(Framable f) {
        if (f instanceof Point) {
            return new PointPainter();
        }
        if (f instanceof Rectangle) {
            return new RectanglePainter();
        }
        if (f instanceof Ball) {
            return new BallPainter();
        }
        if (f instanceof Line) {
            return new LinePainter();
        }
        if (f instanceof Simplex) {
            return new TrianglePainter();
        }
        if (f instanceof AbstractDelaunayGraphBuilder.AbstractDelaunayGraph) {
            return new DelaunayGraphPainter();
        }
        return null;
    }

    public boolean add(Framable f) {
        return add(f, "");
    }

    public boolean add(Framable f, String signature) {
        return add(f, getPainter(f), signature);
    }

    public <T extends Framable> boolean add(T f, Painter<T> p) {
        return add(f, p, null);
    }

    public <T extends Framable> boolean add(T f, Painter<T> p, String signature) {
        if (p == null) {
            return false;
        }

        if (frameRect == null) {
            frameRect = f.getFrameRectangle();
        } else {
            frameRect = frameRect.add(f.getFrameRectangle());
        }
        return items.add(new Item(f, p, signature));
    }



    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (Item item : items) {
            item.paint(g);
        }
    }

    private java.awt.Point toWindow(Point p) {
        int w = getWidth() - 2 * ALIGN;
        int h = getHeight() - 2 * ALIGN;
        long rw = frameRect.getWidth();
        long rh = frameRect.getHeight();
        long rx = p.getX() - frameRect.getX();
        long ry = p.getY() - frameRect.getY();
        int x;
        int y;
        if (w * rh > rw * h) {
            x = (int) ((w * rh - rw * h + 2 * h * rx) / (2 * rh));
            y = (int) (ry * h / rh);
        } else {
            x = (int) (rx * w / rw);
            y = (int) ((rw * h - w * rh + 2 * w * ry) / (2 * rw));
        }

        return new java.awt.Point(x + ALIGN, y + ALIGN);
    }

}

package ru.spbu.astro.graphics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.WalkableDelaunayGraphBuilder;
import ru.spbu.astro.model.*;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.utility.ColorGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class CenteredView extends Component {
    private static final int ALIGN = 20;
    @Nullable
    protected Rectangle frameRect;
    @NotNull
    protected final List<Item> items = new ArrayList<>();

    public enum DelaunayGraphViewMode {
        DEFAULT, NO_TRIANGLES, CREEP, CREEP_ONLY, CIRCUM, CREEP_CIRCUM, NO_CREEP, BORDER, NO_TRIANGLES_CIRCUM
    }

    public enum VorTreeViewMode {
        VORONOI, DELAUNAY
    }

    public enum TriangleViewMode {
        DEFAULT, CREEP
    }

    public abstract class AbstractPainter<T extends Framable> implements Painter<T> {
        private static final int SIZE = 18;

        protected void handle(@NotNull final Graphics g) {
            g.setColor(Color.BLACK);
            g.setFont(new Font(Font.DIALOG, Font.ITALIC, SIZE));
        }

        @Override
        public void sign(@NotNull final T object, @NotNull final String signature, @NotNull final Graphics g) {
            handle(g);

            java.awt.Point center = toWindow(object.getFrameRectangle().getCenter());
            g.drawString(signature, center.x, center.y);
        }
    }

    public final class PointPainter extends AbstractPainter<Point> {
        @NotNull
        private final Color color;
        private final int size;

        public PointPainter() {
            this(3);
        }

        public PointPainter(final int size) {
            this(Color.BLUE, size);
        }

        public PointPainter(@NotNull final Color color, final int size) {
            this.color = color;
            this.size = size;
        }

        @Override
        public void paint(@NotNull final Point p, @NotNull final Graphics g) {
            g.setColor(color);

            final int x = toWindow(p).x;
            final int y = toWindow(p).y;
            if (size == 0) {
                g.drawLine(x, y, x, y);
            } else {
                g.fillOval(x - size, y - size, 2 * size, 2 * size);
            }
        }

        @Override
        public void sign(@NotNull final Point p, @NotNull final String signature, @NotNull final Graphics g) {
            handle(g);

            final java.awt.Point center = toWindow(p);
            g.drawString(signature, center.x + 5, center.y - 4);
        }
    }

    public final class RectanglePainter extends AbstractPainter<Rectangle> {
        @NotNull
        private final Color color;
        private final int width;

        public RectanglePainter() {
            this(new Color(100, 200, 100), 1);
        }

        public RectanglePainter(@NotNull final Color color, final int width) {
            this.color = color;
            this.width = width;
        }

        @Override
        public void paint(@NotNull final Rectangle rect, @NotNull final Graphics g) {
            g.setColor(color);
            ((Graphics2D) g).setStroke(new BasicStroke(width));

            final java.awt.Point minVertex = toWindow(rect.getMinVertex());
            final java.awt.Point maxVertex = toWindow(rect.getMaxVertex());

            g.drawRect(minVertex.x, minVertex.y, maxVertex.x - minVertex.x, maxVertex.y - minVertex.y);
        }
    }

    public final class BallPainter extends AbstractPainter<Ball> {
        @NotNull
        private final Color color;
        private final int width;

        public BallPainter() {
            this(Color.BLACK, 1);
        }

        public BallPainter(@NotNull final Color color) {
            this(color, 1);
        }

        public BallPainter(final int width) {
            this(Color.BLACK, width);
        }

        public BallPainter(@NotNull final Color color, final int width) {
            this.color = color;
            this.width = width;
        }

        @Override
        public void paint(@NotNull final Ball b, @NotNull final Graphics g) {
            g.setColor(color);

            final java.awt.Point minVertex = toWindow(b.getFrameRectangle().getMinVertex());
            final java.awt.Point maxVertex = toWindow(b.getFrameRectangle().getMaxVertex());

            ((Graphics2D) g).setStroke(new BasicStroke(width));

            g.drawOval(minVertex.x, minVertex.y, maxVertex.x - minVertex.x, maxVertex.y - minVertex.y);
        }

        @Override
        public void sign(@NotNull final Ball b, @NotNull final String signature, @NotNull final Graphics g) {
            handle(g);

            java.awt.Point p = toWindow(b.getCenter().add(b.getRadius(), 0));
            g.drawString(signature, p.x - 15 * signature.length(), p.y - 8);
        }
    }

    public final class LinePainter extends AbstractPainter<Line> {
        @NotNull
        private final Color color;
        private final int width;

        public LinePainter() {
            this(new Color(130, 100, 130), 1);
        }

        public LinePainter(@NotNull final Color color, final int width) {
            this.color = color;
            this.width = width;
        }

        @Override
        public void paint(@NotNull final Line line, @NotNull final Graphics g) {
            ((Graphics2D) g).setStroke(new BasicStroke(width));
            g.setColor(color);

            final java.awt.Point p1 = toWindow(line.getFirst());
            final java.awt.Point p2 = toWindow(line.getSecond());

            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    public final class VoronoiDiagramPainter extends AbstractPainter<VoronoiDiagram> {
        @Override
        public void paint(@NotNull final VoronoiDiagram diagram, @NotNull final Graphics g) {
            for (int x = 0; x < getWidth(); ++x) {
                for (int y = 0; y < getHeight(); ++y) {
                    final Point p = fromWindow(new java.awt.Point(x, y));

                    final PointPainter pointPainter = new PointPainter(
                            ColorGenerator.next(diagram.getNearestNeighbor(p)),
                            2
                    );
                    pointPainter.paint(p, g);
                }
            }
            for (final Point p : diagram.getSites()) {
                final PointPainter pointPainter = new PointPainter(4);
                pointPainter.paint(p, g);
            }
        }
    }

    public final class TrianglePainter extends AbstractPainter<Simplex> {
        @NotNull
        private final TriangleViewMode mode;

        public TrianglePainter() {
            this(TriangleViewMode.DEFAULT);
        }

        public TrianglePainter(@NotNull final TriangleViewMode mode) {
            this.mode = mode;
        }

        @Override
        public void paint(@NotNull final Simplex t, @NotNull final Graphics g) {
            final Color color = mode == TriangleViewMode.CREEP ? ColorGenerator.nextRed() : ColorGenerator.nextLight();

            final Point v0 = t.getVertices().get(0);
            final Point v1 = t.getVertices().get(1);
            final Point v2 = t.getVertices().get(2);

            final java.awt.Point p1 = toWindow(v0);
            final java.awt.Point p2 = toWindow(v1);
            final java.awt.Point p3 = toWindow(v2);

            g.setColor(color);
            g.fillPolygon(new int[]{p1.x, p2.x, p3.x}, new int[]{p1.y, p2.y, p3.y}, 3);

            final LinePainter linePainter = new LinePainter(Color.BLACK, 1);
            linePainter.paint(new Line(v0, v1), g);
            linePainter.paint(new Line(v1, v2), g);
            linePainter.paint(new Line(v2, v0), g);

            final PointPainter pointPainter = new PointPainter(Color.BLACK, 2);
            pointPainter.paint(v0, g);
            pointPainter.paint(v1, g);
            pointPainter.paint(v2, g);
        }

        @Override
        public void sign(@NotNull final Simplex t, @NotNull final String signature, @NotNull final Graphics g) {
            handle(g);

            final java.awt.Point center = toWindow(t.getCenter());
            g.drawString(signature, center.x, center.y);
        }
    }

    public final class DelaunayGraphPainter extends AbstractPainter<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> {
        @NotNull
        private final Color edgeColor;
        private final int width;
        @NotNull
        private final DelaunayGraphViewMode mode;

        public DelaunayGraphPainter() {
            this(DelaunayGraphViewMode.DEFAULT);
        }

        public DelaunayGraphPainter(@NotNull final DelaunayGraphViewMode mode) {
            this(1, mode);
        }


        public DelaunayGraphPainter(final int width, @NotNull final DelaunayGraphViewMode mode) {
            this(ColorGenerator.EDGE_DEFAULT, width, mode);
        }

        public DelaunayGraphPainter(@NotNull final Color edgeColor,
                                    final int width,
                                    @NotNull final DelaunayGraphViewMode mode)
        {
            this.edgeColor = edgeColor;
            this.width = width;
            this.mode = mode;
        }

        @Override
        public void paint(@NotNull final AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph,
                          @NotNull final Graphics g)
        {
            //Composite composite = ((Graphics2D) g).getComposite();
            //((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

            TrianglePainter trianglePainter;
            final BallPainter ballPainter;
            LinePainter linePainter;
            PointPainter pointPainter;
            switch (mode) {
                case DEFAULT:
                    trianglePainter = new TrianglePainter();
                    for (final Simplex s : graph.getPointSimplexes()) {
                        trianglePainter.paint(s, g);
                    }
                    break;
                case CREEP:
                    trianglePainter = new TrianglePainter();
                    for (final Simplex s : graph.getPointSimplexes()) {
                        trianglePainter.paint(s, g);
                    }
                    trianglePainter = new TrianglePainter(TriangleViewMode.CREEP);
                    for (final Simplex s : graph.getBuilder().getCreepPointSimplexes(graph)) {
                        trianglePainter.paint(s, g);
                    }
                    break;
                case CREEP_ONLY:
                    trianglePainter = new TrianglePainter(TriangleViewMode.CREEP);
                    for (final Simplex s : graph.getBuilder().getCreepPointSimplexes(graph)) {
                        trianglePainter.paint(s, g);
                    }
                    break;
                case CIRCUM:
                    trianglePainter = new TrianglePainter();
                    ballPainter = new BallPainter(2);
                    for (final Simplex s : graph.getPointSimplexes()) {
                        trianglePainter.paint(s, g);
                        ballPainter.paint(new Ball(s.getVertices()), g);
                    }

                    trianglePainter = new TrianglePainter(TriangleViewMode.CREEP);
                    for (final Simplex s : graph.getBuilder().getCreepPointSimplexes(graph)) {
                        trianglePainter.paint(s, g);
                    }
                    break;
                case CREEP_CIRCUM:
                    trianglePainter = new TrianglePainter(TriangleViewMode.CREEP);
                    ballPainter = new BallPainter(1);
                    for (final Simplex s : graph.getBuilder().getCreepPointSimplexes(graph)) {
                        ballPainter.paint(new Ball(s.getVertices()), g);
                        trianglePainter.paint(s, g);

                    }
                    break;
                case NO_CREEP:
                    linePainter = new LinePainter(edgeColor, width);
                    pointPainter = new PointPainter(Color.BLACK, 2);

                    for (final Line edge : graph.getPointEdges()) {
                        linePainter.paint(edge, g);
                        pointPainter.paint(edge.getFirst(), g);
                        pointPainter.paint(edge.getSecond(), g);
                    }

                    for (final Point p : graph.getIsolatedPoints()) {
                        pointPainter.paint(p, g);
                    }
                    return;
                case BORDER:
                    //((Graphics2D) g).setComposite(composite);
                    ((Graphics2D) g).setStroke(new BasicStroke(width));

                    linePainter = new LinePainter(edgeColor.brighter().brighter().brighter(), width);
                    pointPainter = new PointPainter(Color.GRAY.brighter(), 0);
                    for (final Line edge : graph.getPointEdges()) {
                        linePainter.paint(edge, g);
                        pointPainter.paint(edge.getFirst(), g);
                        pointPainter.paint(edge.getSecond(), g);
                    }

                    linePainter = new LinePainter(edgeColor, width);
                    pointPainter = new PointPainter(Color.BLACK, 3);
                    for (final Line edge : ((WalkableDelaunayGraphBuilder.WalkableDelaunayGraph) graph).getBorderEdges()) {
                        linePainter.paint(edge, g);
                        pointPainter.paint(edge.getFirst(), g);
                        pointPainter.paint(edge.getSecond(), g);
                    }

                    for (final Point p : graph.getIsolatedPoints()) {
                        pointPainter.paint(p, g);
                    }

                    return;
                case NO_TRIANGLES_CIRCUM:
                    ballPainter = new BallPainter(Color.RED);
                    for (final Simplex s : graph.getPointSimplexes()) {
                        if (new Random().nextInt(5) == 0) {
                            ballPainter.paint(new Ball(s.getVertices()), g);
                        }
                    }
                    break;
            }

            linePainter = new LinePainter(edgeColor, width);
            pointPainter = new PointPainter(Color.BLACK, 1);
            for (final Line edge : graph.getPointEdges()) {
                linePainter.paint(edge, g);
                pointPainter.paint(edge.getFirst(), g);
                pointPainter.paint(edge.getSecond(), g);
            }
        }
    }

    public final class VorTreePainter extends AbstractPainter<AbstractVorTreeBuilder.AbstractVorTree> {
        @NotNull
        private final VorTreeViewMode mode;

        public VorTreePainter() {
            this(VorTreeViewMode.VORONOI);
        }

        public VorTreePainter(@NotNull final VorTreeViewMode mode) {
            this.mode = mode;
        }

        @Override
        public void paint(@NotNull final AbstractVorTreeBuilder.AbstractVorTree t, @NotNull final Graphics g) {
            switch (mode) {
                case VORONOI:
                    final VoronoiDiagram diagram = new VoronoiDiagram(t.getPoints());
                    new VoronoiDiagramPainter().paint(diagram, g);
                    break;
                case DELAUNAY:
                    new DelaunayGraphPainter().paint(t, g);
                    break;
            }

            new RTreePainter().paint(t.getRTree(), g);

            for (@NotNull final Point p : t.getPoints()) {
                final PointPainter pointPainter = new PointPainter(4);
                pointPainter.paint(p, g);
            }
        }

    }

    public final class RTreePainter extends AbstractPainter<AbstractVorTreeBuilder.AbstractVorTree.RTree> {
        private final int width;

        public RTreePainter() {
            this(5);
        }

        public RTreePainter(int width) {
            this.width = width;
        }

        @Override
        public void paint(@NotNull final AbstractVorTreeBuilder.AbstractVorTree.RTree t, @NotNull final Graphics g) {
            paint(t, 0, g);
            for (final Point p : t.getPoints()) {
                final PointPainter pointPainter = new PointPainter(3);
                pointPainter.paint(p, g);
            }
        }

        private void paint(@NotNull final AbstractVorTreeBuilder.AbstractVorTree.RTree t,
                           final int level,
                           @NotNull final Graphics g)
        {
            System.out.println(level);
            RectanglePainter rectanglePainter = new RectanglePainter(
                    ColorGenerator.next(level * 7),
                    Math.max(width - level, 0)
            );
            rectanglePainter.paint(t.cover, g);
            for (final AbstractVorTreeBuilder.AbstractVorTree.RTree son : t.sons) {
                paint(son, level + 1, g);
            }
        }
    }

    private class Item<T extends Framable> {
        @NotNull
        private final T f;
        @NotNull
        private final Painter<T> painter;
        @Nullable
        private final String signature;

        private Item(@NotNull final T f, @NotNull final Painter<T> painter, @Nullable final String signature) {
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

    @Nullable
    private Painter getPainter(@NotNull final Framable f) {
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
        if (f instanceof AbstractVorTreeBuilder.AbstractVorTree) {
            return new VorTreePainter();
        }
        if (f instanceof AbstractDelaunayGraphBuilder.AbstractDelaunayGraph) {
            return new DelaunayGraphPainter();
        }
        if (f instanceof VoronoiDiagram) {
            return new VoronoiDiagramPainter();
        }
        if (f instanceof AbstractVorTreeBuilder.AbstractVorTree.RTree) {
            return new RTreePainter();
        }
        return null;
    }

    public boolean add(@NotNull final Framable f) {
        return add(f, "");
    }

    public boolean add(@NotNull final Framable f, @NotNull final String signature) {
        return add(f, getPainter(f), signature);
    }

    public <T extends Framable> boolean add(@NotNull final T f, @NotNull final Painter<T> p) {
        return add(f, p, null);
    }

    public <T extends Framable> boolean add(@NotNull final T f,
                                            @Nullable final Painter<T> p,
                                            @Nullable final String signature)
    {
        if (p == null) {
            return false;
        }

        if (frameRect == null) {
            frameRect = f.getFrameRectangle();
        } else {
            frameRect = frameRect.add(f.getFrameRectangle());
        }
        return items.add(new Item<>(f, p, signature));
    }

    @Override
    public void paint(@NotNull final Graphics g) {
        super.paint(g);
        for (final Item item : items) {
            item.paint(g);
        }
    }

    @NotNull
    private java.awt.Point toWindow(@NotNull final Point p) {
        assert frameRect != null;

        final int w = getWidth() - 2 * ALIGN;
        final int h = getHeight() - 2 * ALIGN;
        final long rw = frameRect.getWidth();
        final long rh = frameRect.getHeight();

        final long rx = p.getX() - frameRect.getX();
        final long ry = p.getY() - frameRect.getY();

        final int x;
        final int y;
        if (w * rh > rw * h) {
            x = (int) ((w * rh - rw * h + 2 * h * rx) / (2 * rh));
            y = (int) (ry * h / rh);
        } else {
            x = (int) (rx * w / rw);
            y = (int) ((rw * h - w * rh + 2 * w * ry) / (2 * rw));
        }

        return new java.awt.Point(x + ALIGN, y + ALIGN);
    }

    @NotNull
    private Point fromWindow(@NotNull final java.awt.Point p) {
        assert frameRect != null;

        final int w = getWidth() - 2 * ALIGN;
        final int h = getHeight() - 2 * ALIGN;
        final long rw = frameRect.getWidth();
        final long rh = frameRect.getHeight();

        final int x = p.x - ALIGN;
        final int y = p.y - ALIGN;

        final long rx;
        final long ry;
        if (w * rh > rw * h) {
            rx = ((rw * h - w * rh + 2 * x * rh) / (2 * h));
            ry = y * rh / h;
        } else {
            rx = x * rw / w;
            ry = (w * rh - rw * h + 2 * y * rw) / (2 * w);
        }

        return new Point(rx + frameRect.getX(), ry + frameRect.getY());
    }
}

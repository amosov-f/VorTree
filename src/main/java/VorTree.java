import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VorTree {
    private Node root;
    private List<Point> points = new ArrayList<>();

    private final static int MIN_SONS = 2;
    private final static int MAX_SONS = 4;

    public VorTree(List<Point> points) {
        this.points = points;
        if (points.size() == 1) {
            root = new Node(new Rectangle(points));
            return;
        }

        int k = Math.min((MIN_SONS + MAX_SONS) / 2, points.size());

        Collections.shuffle(points);
        List<Point> pivots = points.subList(0, k);

        int[] parents = new int[points.size()];

        for (int i = 0; i < points.size(); ++i) {
            for (int j = 0; j < pivots.size(); ++j) {
                Point p = points.get(i);
                Point s = pivots.get(j);
                if (p.distanceTo(s) < p.distanceTo(pivots.get(parents[i]))) {
                    parents[i] = j;
                }
            }
        }

        List<Point>[] groups = new ArrayList[pivots.size()];
        for (int i = 0; i < groups.length; ++i) {
            groups[i] = new ArrayList<>();
        }

        for (int i = 0; i < parents.length; ++i) {
            groups[parents[i]].add(points.get(i));
        }

        Rectangle cover = new Rectangle(points);

        root = new Node(cover);
        for (List<Point> group : groups) {
            if (!group.isEmpty()) {
                root.add((new VorTree(group)).root);
            }
        }
    }

    @Override
    public String toString() {
        return toString(root, 0);
    }

    public String toString(Node u, int tab) {
        String result = tab(tab) + u.toString();
        if (!u.getSons().isEmpty()) {
            result += " {\n";
        }
        for (Node v : u.getSons()) {
            result += toString(v, tab + 4);
        }
        if (!u.getSons().isEmpty()) {
            result += tab(tab) + "}";
        }
        result += "\n";
        return result;
    }

    private static class Node {
        private Rectangle cover;

        private List<Node> sons = new ArrayList<>();

        public Node(Rectangle cover) {
            this.cover = cover;
        }

        public void add(Node son) {
            sons.add(son);
        }

        public List<Node> getSons() {
            return sons;
        }

        public Rectangle getCover() {
            return cover;
        }

        @Override
        public String toString() {
            return cover.toString();
        }

    }

    public Component getComponent(final int width, final int height) {
        return new Component() {
            private int ALIGN = 10;

            @Override
            public void paint(Graphics g) {
                setSize(width, height);
                setBounds(0, 0, width, height);

                paint(g, root, 0);

                g.setColor(new Color(0, 0, 0));
                for (Point p : points) {
                    g.fillOval((int)translate(p).getX() - 4, (int)translate(p).getY() - 4, 8, 8);
                }
            }

            private Point2D.Double translate(Point p) {
                return new Point2D.Double(
                        (p.getX() - root.getCover().getX()) / root.getCover().getWidth() * (width - 2 * ALIGN) + ALIGN,
                        (p.getY() - root.getCover().getY()) / root.getCover().getHeight() * (height - 2 * ALIGN) + ALIGN
                );
            }

            private void paint(Graphics g, Node u, int level) {
                g.setColor(color(level));
                ((Graphics2D)g).setStroke(new BasicStroke(Math.max(6 - level, 1)));

                Point2D.Double minVertex = translate(u.getCover().getMinVertex());
                Point2D.Double maxVertex = translate(u.getCover().getMaxVertex());

                g.drawRect(
                        (int)minVertex.getX(),
                        (int)minVertex.getY(),
                        (int)(maxVertex.getX() - minVertex.getX()),
                        (int)(maxVertex.getY() - minVertex.getY())
                );

                for (Node v : u.getSons()) {
                    paint(g, v, level + 1);
                }
            }

            private Color color(int level) {
                return new Color((131 * level + 100) % 256, (241 * level + 200) % 256, (271 * level + 100) % 256);
            }
        };
    }

    private static String tab(int size) {
        String result = "";
        for (int i = 0; i < size; ++i) {
            result += " ";
        }
        return result;
    }

}

package ru.spbu.astro.other;

import ru.spbu.astro.graphics.ClickableView;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.utility.PointGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class MetricBorderViewEngine extends ClickableView {

    Point p1;
    Point p2;

    public MetricBorderViewEngine() {
        super();
        setSize(1320, 660);
        build();
    }

    public void build() {
        Collection<Point> points = PointGenerator.nextUniforms(2, getWidth(), getHeight());
        p1 = new ArrayList<Point>(points).get(0);
        p2 = new ArrayList<Point>(points).get(1);
    }

    @Override
    public void paint(Graphics g) {

        for (int x = 0; x < getWidth(); ++x) {
            for (int y = 0; y < getHeight(); ++y) {
                Point p = new Point(x, y);
                g.setColor(new Color(100, 150, 0));
                if (distance(p, p1) < distance(p, p2)) {
                    g.setColor(new Color(200, 100, 0));
                }
                g.drawLine(x, y, x, y);
            }
        }

        g.setColor(new Color(0, 0, 255));
        g.fillOval((int)p1.getX() - 4, (int)p1.getY() - 4, 8, 8);

        g.fillOval((int)p2.getX() - 4, (int)p2.getY() - 4, 8, 8);
    }

    public static double distance(Point p1, Point p2) {
        double dist = 0;
        for (int i = 0; i < p1.dim(); ++i) {
            dist += Math.abs(p1.get(i) - p2.get(i));
        }
        return dist;
    }

    public static void main(String[] args) {
        ClickableView frame = new MetricBorderViewEngine();
        frame.setVisible(true);
    }

}

package ru.spbu.astro;

import ru.spbu.astro.model.Point;
import ru.spbu.astro.utility.PointGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class MetricBorderVisualizerEngine {

    private static int SIZE_X = 1320;
    private static int SIZE_Y = 660;

    public static double distance(Point p1, Point p2) {
        double dist = 0;
        for (int i = 0; i < p1.dim(); ++i) {
            dist += Math.abs(p1.get(i) - p2.get(i));
        }
        return dist;
    }

    public static void main(String[] args) {

        Component component = new Component() {
            @Override
            public void paint(Graphics g) {
                Collection<Point> points = PointGenerator.nextUniforms(2);
                Point p1 = new ArrayList<Point>(points).get(0);
                Point p2 = new ArrayList<Point>(points).get(1);

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
        };

        JFrame frame = new JFrame();
        frame.add(component);
        frame.setSize(SIZE_X, SIZE_Y);
        frame.setVisible(true);
    }

}

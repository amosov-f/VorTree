package ru.spbu.astro.other;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VorDelaunayGraphBuilder;
import ru.spbu.astro.graphics.CenteredView;
import ru.spbu.astro.model.Ball;
import ru.spbu.astro.model.Line;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Simplex;
import ru.spbu.astro.utility.PointGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Lemma1VisualizerEngine {

    public static class Figure1 extends CenteredView {
        public Figure1() {
            setSize(660, 660);

            List<Point> points = PointGenerator.nextUniforms(50, 10000 * getWidth(), 10000 * getHeight());
            Collections.sort(points, new Comparator<Point>() {
                @Override
                public int compare(Point p1, Point p2) {
                    return new Long(p1.get(0)).compareTo(p2.get(0));
                }
            });

            Point q = points.get(points.size() - 1);

            List<Integer> pointIds = new ArrayList();
            for (int i = 0; i < points.size() - 1; ++i) {
                pointIds.add(i);
            }

            AbstractDelaunayGraphBuilder builder = new VorDelaunayGraphBuilder(points, 2);
            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph = builder.build(pointIds);

            add(graph);
            add(q, "q");
        }
    }

    public static class Figure2 extends CenteredView {
        public Figure2() {
            setSize(720, 655);

            Point f0 = new Point(130, 60).multiply(1.6);
            Point f1 = new Point(130, 360).multiply(1.6);
            Point p0 = new Point(20, 180).multiply(1.6);
            Point p = new Point(190, 390).multiply(1.6);
            Point q = new Point(280, 150).multiply(1.6);

            Ball b0 = new Ball(f0, f1, p0);
            add(b0, new BallPainter(Color.BLACK), "B0");
            add(f0);
            add(new Line(f0, f1), "F");

            Ball b1 = new Ball(f0, f1, p);
            add(b1, new BallPainter(Color.BLACK), "B1");

            add(f0);
            add(f1);
            add(p0);
            add(p, "p");
            add(q, "q");
        }
    }

    public static class Figure3 extends CenteredView {
        Figure3() {
            setSize(720, 655);

            Point f0 = new Point(130, 60).multiply(1.6);
            Point f1 = new Point(130, 360).multiply(1.6);
            Point p0 = new Point(20, 180).multiply(1.6);
            Point p = new Point(190, 390).multiply(1.6);
            Point q = new Point(280, 150).multiply(1.6);

            Line f = new Line(f0, f1);

            Point q0 = f.getProjection(q);
            Point q1 = new Line(f0, p).getProjection(q);

            add(q, "q");
            add(new Simplex(p0, f0, f1), "S0");
            add(new Simplex(p, f0, f1), "S1");

            add(f, "F");

            add(new Line(q, q0), "d0");
            add(new Line(q, q1), "d1");
        }
    }

    public static void main(String[] args) {
        Component balls = new Figure3();
        JWindow window = new JWindow();
        window.setSize(balls.getSize());
        window.add(balls);
        window.setVisible(true);
    }
}

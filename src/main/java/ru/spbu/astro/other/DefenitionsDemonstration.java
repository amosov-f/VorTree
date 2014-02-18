package ru.spbu.astro.other;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.graphics.ClickableView;
import ru.spbu.astro.graphics.View;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.VoronoiDiagram;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.search.VorTreeBuilder;
import ru.spbu.astro.utility.PointGenerator;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DefenitionsDemonstration {

    private static final int WIDTH = 1100;
    private static final int HEIGHT = 660;

    private static final int N = 50;

    private static class Figure0 extends ClickableView {

        public Figure0() {
            setSize(DefenitionsDemonstration.WIDTH, DefenitionsDemonstration.HEIGHT);
            setBackground(Color.WHITE);
            build();
        }

        @Override
        public void build() {

            final List<Point> points;
            try {
                points = getPoints();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            AbstractVorTreeBuilder.AbstractVorTree t = new VorTreeBuilder(points).build(3);

            add(t.getRTree());
        }
    }

    public static class Figure1 extends ClickableView {

        public Figure1() {
            setSize(DefenitionsDemonstration.WIDTH, DefenitionsDemonstration.HEIGHT);
            setBackground(Color.WHITE);
            build();
        }

        @Override
        public void build() {
            final List<Point> points;
            try {
                points = getPoints();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            add(new VoronoiDiagram(points));
        }

    }

    public static class Figure2 extends ClickableView {

        public Figure2() {
            setSize(DefenitionsDemonstration.WIDTH, DefenitionsDemonstration.HEIGHT);
            setBackground(Color.WHITE);
            build();
        }

        @Override
        public void build() {
            final List<Point> points;
            try {
                points = getPoints();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            AbstractDelaunayGraphBuilder builder = new VorTreeBuilder(points);

            add(new VoronoiDiagram(points));
            add(builder.build(), new DelaunayGraphPainter(Color.WHITE, 2, DelaunayGraphViewMode.NO_TRIANGLES));

            for (final Point p : points) {
                add(p, new PointPainter(4));
            }
        }

    }

    public static class Figure3 extends ClickableView {

        public Figure3() {
            setSize(DefenitionsDemonstration.WIDTH, DefenitionsDemonstration.HEIGHT);
            setBackground(Color.WHITE);
            build();
        }

        @Override
        public void build() {
            final List<Point> points;
            try {
                points = getPoints();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            AbstractDelaunayGraphBuilder builder = new VorTreeBuilder(points);

            add(builder.build(), new DelaunayGraphPainter(Color.BLACK, 2, DelaunayGraphViewMode.NO_TRIANGLES));

            for (final Point p : points) {
                add(p, new PointPainter(4));
            }
        }

    }

    public static class Figure4 extends ClickableView {

        public Figure4() {
            setSize(DefenitionsDemonstration.WIDTH, DefenitionsDemonstration.HEIGHT);
            setBackground(Color.WHITE);
            build();
        }

        @Override
        public void build() {
            final List<Point> points;
            try {
                points = getPoints();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            AbstractDelaunayGraphBuilder builder = new VorTreeBuilder(points);

            add(builder.build(), new DelaunayGraphPainter(Color.BLACK, 2, DelaunayGraphViewMode.NO_TRIANGLES_CIRCUM));

            for (final Point p : points) {
                add(p, new PointPainter(4));
            }
        }

    }

    private static List<Point> getPoints() throws FileNotFoundException {
        if (new File("input/points.txt").length() == 0) {
            final PrintWriter fout = new PrintWriter(new FileOutputStream("input/points.txt"));

            final List<Point> points = PointGenerator.nextUniforms(N, 1000 * WIDTH, 1000 * HEIGHT);

            for (Point p : points) {
                fout.println(p.toString());
            }
            fout.flush();
        }
        Scanner fin = new Scanner(new FileInputStream("input/points.txt"));

        final List<Point> points = new ArrayList<>();
        for (int i = 0; i < N; ++i) {
            points.add(new Point(fin.nextLine()));
        }

        return points;
    }

    public static void main(final String[] args) {
        new View.Window(new Figure4());
    }
}

package ru.spbu.astro.other;

import org.apache.commons.io.FileUtils;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VisadDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.WalkableDelaunayGraphBuilder;
import ru.spbu.astro.graphics.ClickableView;
import ru.spbu.astro.graphics.View;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Line;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.VoronoiDiagram;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.search.VorTreeBuilder;
import ru.spbu.astro.utility.ColorGenerator;
import ru.spbu.astro.utility.PointGenerator;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class AlgorithmDemonstration {

    private static final int WIDTH = 1100;
    private static final int HEIGHT = 660;

    private static final int N = 50;
    private static final int DIVISION = 3;

    private static class Figure1 extends ClickableView {

        public Figure1() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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

            AbstractDelaunayGraphBuilder builder = new VisadDelaunayGraphBuilder(points);

            for (Point p : points) {
                add(p, new PointPainter(Color.BLACK, 2));
            }
        }
    }

    public static class Figure2 extends ClickableView {

        public Figure2() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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

            for (int i = 0; i < DIVISION; ++i) {
                add(points.get(i), new PointPainter(4));
            }

            for (int i = DIVISION; i < points.size(); ++i) {
                add(points.get(i), new PointPainter(Color.BLACK, 2));
            }

        }
    }

    public static class Figure3 extends ClickableView {

        public Figure3() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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

            add(new VoronoiDiagram(points.subList(0, DIVISION)));

            for (int i = DIVISION; i < points.size(); ++i) {
                add(points.get(i), new PointPainter(Color.BLACK, 2));
            }
        }
    }

    public static class Figure4 extends ClickableView {

        public Figure4() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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

            VoronoiDiagram diagram = new VoronoiDiagram(points.subList(0, DIVISION));

            for (Point p : points) {
                int NN = diagram.getNearestNeighbor(p);
                add(p, new PointPainter(ColorGenerator.next(NN), 3));
            }
        }
    }

    public static class Figure5 extends ClickableView {

        public Figure5() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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

            for (AbstractDelaunayGraphBuilder.AbstractDelaunayGraph g : split(points)) {
                add(g, new DelaunayGraphPainter());
            }
        }
    }

    public static class Figure6 extends ClickableView {

        public Figure6() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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

            List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> sons = split(points);
            for (int i = 0; i < sons.size(); ++i) {
                add(sons.get(i), new DelaunayGraphPainter(ColorGenerator.next(i), 3, DelaunayGraphViewMode.CREEP_CIRCUM));
            }
        }
    }

    public static class Figure7 extends ClickableView {

        public Figure7() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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

            List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> sons = split(points);
            for (int i = 0; i < sons.size(); ++i) {
                add(sons.get(i), new DelaunayGraphPainter(ColorGenerator.next(i), 3, DelaunayGraphViewMode.NO_CREEP));
            }
        }
    }

    public static class Figure8 extends ClickableView {

        public Figure8() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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

            List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> sons = split(points);
            for (int i = 0; i < sons.size(); ++i) {
                add(sons.get(i), new DelaunayGraphPainter(ColorGenerator.next(i), 3, DelaunayGraphViewMode.BORDER));
            }
        }
    }

    public static class Figure9 extends ClickableView {

        public Figure9() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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
            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph g = builder.build();

            final List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> sons = split(points);
            final List<Integer> bindPointIds = new ArrayList<>();
            for (int i = 0; i < sons.size(); ++i) {
                add(sons.get(i), new DelaunayGraphPainter(ColorGenerator.next(i), 3, DelaunayGraphViewMode.BORDER));
                bindPointIds.addAll(g.getBindPointIds(sons.get(i)));
            }

            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph bindGraph = builder.build(bindPointIds);

            add(bindGraph, new DelaunayGraphPainter(1, DelaunayGraphViewMode.NO_TRIANGLES));

        }
    }

    public static class Figure10 extends ClickableView {

        public Figure10() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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
            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph g = builder.build();

            final List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> sons = split(points);

            final List<Integer> bindPointIds = new ArrayList<>();
            Graph removedGraph = new Graph();
            final Map<Integer, Integer> id2pivot = new HashMap<>();
            for (int i = 0; i < sons.size(); ++i) {
                bindPointIds.addAll(sons.get(i).getBorderVertices());
                removedGraph.addGraph(g.removeCreepSimplexes(sons.get(i)));

                add(sons.get(i), new DelaunayGraphPainter(ColorGenerator.next(i), 3, DelaunayGraphViewMode.BORDER));

                for (int u : sons.get(i).getVertices()) {
                    id2pivot.put(u, i);
                }
            }
            bindPointIds.addAll(removedGraph.getVertices());


            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph bindGraph = builder.build(bindPointIds);

            for (Graph.Edge e : bindGraph) {
                int u = e.getFirst();
                int v = e.getSecond();
                final Line edge = new Line(builder.get(u), builder.get(v));
                if (!id2pivot.get(u).equals(id2pivot.get(v))) {
                    add(edge);
                }
                if (removedGraph.containsEdge(e)) {
                    add(edge);

                }

            }

            for (int u : bindGraph.getVertices()) {
                add(builder.get(u), new PointPainter(Color.BLACK, 3));
            }

        }
    }

    public static class Figure11 extends ClickableView {

        public Figure11() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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
            add(builder.build());
        }
    }

    public static class Figure12 extends ClickableView {

        public Figure12() {
            setSize(AlgorithmDemonstration.WIDTH, AlgorithmDemonstration.HEIGHT);
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

            for (AbstractDelaunayGraphBuilder.AbstractDelaunayGraph son : split(points)) {
                add(son, new DelaunayGraphPainter());
            }

            for (AbstractDelaunayGraphBuilder.AbstractDelaunayGraph son : split(points)) {
                AbstractVorTreeBuilder.AbstractVorTree t = (AbstractVorTreeBuilder.AbstractVorTree) son;
                add(t.getRTree(), new RTreePainter(4));
            }

        }
    }

    private static List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> split(final List<Point> points) {
        VoronoiDiagram diagram = new VoronoiDiagram(points.subList(0, DIVISION));

        List<List<Integer>> cells = new ArrayList<>(DIVISION);
        for (int i = 0; i < DIVISION; ++i) {
            cells.add(new ArrayList<Integer>());
        }

        for (int i = 0; i < points.size(); ++i) {
            cells.get(diagram.getNearestNeighbor(points.get(i))).add(i);
        }

        AbstractVorTreeBuilder builder = new VorTreeBuilder(points);

        List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> result = new ArrayList<>();
        for (List<Integer> cell : cells) {
            result.add(builder.build(cell, 3));
        }

        return result;
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
        new View.Window(new Figure12());
    }

}

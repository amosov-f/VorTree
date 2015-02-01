package ru.spbu.astro.other;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VisadDelaunayGraphBuilder;
import ru.spbu.astro.graphics.View;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Line;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.VoronoiDiagram;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.search.VorTreeBuilder;
import ru.spbu.astro.utility.ColorGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class AlgorithmDemonstration extends Demonstration {
    private static final int DIVISION = 3;

    private static class Figure1 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            final AbstractDelaunayGraphBuilder builder = new VisadDelaunayGraphBuilder(points);
            for (final Point p : points) {
                add(p, new PointPainter(Color.BLACK, 2));
            }
        }
    }

    public static class Figure2 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            for (int i = 0; i < DIVISION; ++i) {
                add(points.get(i), new PointPainter(4));
            }
            for (int i = DIVISION; i < points.size(); ++i) {
                add(points.get(i), new PointPainter(Color.BLACK, 2));
            }
        }
    }

    public static class Figure3 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            add(new VoronoiDiagram(points.subList(0, DIVISION)));
            for (int i = DIVISION; i < points.size(); ++i) {
                add(points.get(i), new PointPainter(Color.BLACK, 2));
            }
        }
    }

    public static class Figure4 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            final VoronoiDiagram diagram = new VoronoiDiagram(points.subList(0, DIVISION));
            for (final Point p : points) {
                final int NN = diagram.getNearestNeighbor(p);
                add(p, new PointPainter(ColorGenerator.next(NN), 3));
            }
        }
    }

    public static class Figure5 extends Figure {
        @Override
        public void build() {
            for (final AbstractDelaunayGraphBuilder.AbstractDelaunayGraph g : split(getPoints())) {
                add(g, new DelaunayGraphPainter());
            }
        }
    }

    public static class Figure6 extends Figure {
        @Override
        public void build() {
            final List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> sons = split(getPoints());
            for (int i = 0; i < sons.size(); ++i) {
                add(sons.get(i), new DelaunayGraphPainter(ColorGenerator.next(i), 3, DelaunayGraphViewMode.CREEP_CIRCUM));
            }
        }
    }

    public static class Figure7 extends Figure {
        @Override
        public void build() {
            final List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> sons = split(getPoints());
            for (int i = 0; i < sons.size(); ++i) {
                add(sons.get(i), new DelaunayGraphPainter(ColorGenerator.next(i), 3, DelaunayGraphViewMode.NO_CREEP));
            }
        }
    }

    public static class Figure8 extends Figure {
        @Override
        public void build() {
            final List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> sons = split(getPoints());
            for (int i = 0; i < sons.size(); ++i) {
                add(sons.get(i), new DelaunayGraphPainter(ColorGenerator.next(i), 3, DelaunayGraphViewMode.BORDER));
            }
        }
    }

    public static class Figure9 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            final AbstractDelaunayGraphBuilder builder = new VorTreeBuilder(points);
            final AbstractDelaunayGraphBuilder.AbstractDelaunayGraph g = builder.build();
            final List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> sons = split(points);
            final List<Integer> bindPointIds = new ArrayList<>();
            for (int i = 0; i < sons.size(); ++i) {
                add(sons.get(i), new DelaunayGraphPainter(ColorGenerator.next(i), 3, DelaunayGraphViewMode.BORDER));
                bindPointIds.addAll(g.getBindPointIds(sons.get(i)));
            }
            final AbstractDelaunayGraphBuilder.AbstractDelaunayGraph bindGraph = builder.build(bindPointIds);
            add(bindGraph, new DelaunayGraphPainter(1, DelaunayGraphViewMode.NO_TRIANGLES));
        }
    }

    public static class Figure10 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            final AbstractDelaunayGraphBuilder builder = new VorTreeBuilder(points);
            final AbstractDelaunayGraphBuilder.AbstractDelaunayGraph g = builder.build();
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
            for (final Graph.Edge e : bindGraph) {
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

            for (final int u : bindGraph.getVertices()) {
                add(builder.get(u), new PointPainter(Color.BLACK, 3));
            }
        }
    }

    public static class Figure11 extends Figure {
        @Override
        public void build() {
            add(new VorTreeBuilder(getPoints()).build());
        }
    }

    public static class Figure12 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            for (final AbstractDelaunayGraphBuilder.AbstractDelaunayGraph son : split(points)) {
                add(son, new DelaunayGraphPainter());
            }
            for (final AbstractDelaunayGraphBuilder.AbstractDelaunayGraph son : split(points)) {
                final AbstractVorTreeBuilder.AbstractVorTree t = (AbstractVorTreeBuilder.AbstractVorTree) son;
                add(t.getRTree(), new RTreePainter(4));
            }
        }
    }

    @NotNull
    private static List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> split(@NotNull final List<Point> points) {
        final VoronoiDiagram diagram = new VoronoiDiagram(points.subList(0, DIVISION));
        final List<List<Integer>> cells = new ArrayList<>(DIVISION);
        for (int i = 0; i < DIVISION; ++i) {
            cells.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < points.size(); ++i) {
            cells.get(diagram.getNearestNeighbor(points.get(i))).add(i);
        }
        final AbstractVorTreeBuilder builder = new VorTreeBuilder(points);
        List<AbstractDelaunayGraphBuilder.AbstractDelaunayGraph> result = new ArrayList<>();
        for (List<Integer> cell : cells) {
            result.add(builder.build(cell, 3));
        }
        return result;
    }

    public static void main(@NotNull final String[] args) {
        new View.Window(new Figure12());
    }
}

package ru.spbu.astro.other;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VisadDelaunayGraphBuilder;
import ru.spbu.astro.graphics.ClickableView;
import ru.spbu.astro.graphics.View;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Triangulation;
import ru.spbu.astro.utility.PointGenerator;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class CreepTrianglesVisualizerEngine {

    public static class CreepFigure extends ClickableView {

        private final Collection<Point> points;

        CreepFigure(final Collection<Point> points) {
            this.points = points;
            setSize(500, 660);
            setBackground(Color.WHITE);
            build();
        }

        @Override
        public void build() {
            AbstractDelaunayGraphBuilder builder = new VisadDelaunayGraphBuilder(points);

            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph g = builder.build();

            for (final Point p : points) {
                add(p);
            }

            for (int i = 0; i < points.size(); ++i) {
                for (int j = i + 1; j < points.size(); ++j) {
                    for (int k = j + 1; k < points.size(); ++k) {
                        Triangulation.Simplex s = new Triangulation.Simplex(i, j, k);
                        if (g.isCreep(s)) {
                            add(builder.build(i, j, k), new DelaunayGraphPainter(DelaunayGraphViewMode.CIRCUM));
                            return;
                        }
                    }
                }
            }
        }
    }

    public static class NoCreepFigure extends ClickableView {

        private final Collection<Point> points;

        NoCreepFigure(final Collection<Point> points) {
            this.points = points;
            setSize(500, 660);
            setBackground(Color.WHITE);
            build();
        }

        @Override
        public void build() {
            AbstractDelaunayGraphBuilder builder = new VisadDelaunayGraphBuilder(points);

            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph g = builder.build();

            for (final Point p : points) {
                add(p);
            }

            for (int i = 0; i < points.size(); ++i) {
                for (int j = i + 1; j < points.size(); ++j) {
                    for (int k = j + 1; k < points.size(); ++k) {
                        Triangulation.Simplex s = new Triangulation.Simplex(i, j, k);
                        if (!g.isCreep(s)) {
                            add(builder.build(i, j, k), new DelaunayGraphPainter(DelaunayGraphViewMode.CIRCUM));
                            return;
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        final List<Point> points = PointGenerator.nextUniforms(10);
        new View.Frame(new CreepFigure(points));
        new View.Frame(new NoCreepFigure(points));
    }

}

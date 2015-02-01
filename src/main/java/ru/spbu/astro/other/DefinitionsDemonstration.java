package ru.spbu.astro.other;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.graphics.View;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.VoronoiDiagram;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.search.VorTreeBuilder;

import java.awt.*;
import java.util.List;

@SuppressWarnings("unused")
public class DefinitionsDemonstration extends Demonstration {
    public static class Figure0 extends Figure {
        @Override
        public void build() {
            AbstractVorTreeBuilder.AbstractVorTree t = new VorTreeBuilder(getPoints()).build(3);
            add(t.getRTree());
        }
    }

    public static class Figure1 extends Figure {
        @Override
        public void build() {
            add(new VoronoiDiagram(getPoints()));
        }
    }

    public static class Figure2 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            final AbstractDelaunayGraphBuilder builder = new VorTreeBuilder(points);
            add(new VoronoiDiagram(points));
            add(builder.build(), new DelaunayGraphPainter(Color.WHITE, 2, DelaunayGraphViewMode.NO_TRIANGLES));
            for (final Point p : points) {
                add(p, new PointPainter(4));
            }
        }
    }

    public static class Figure3 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            final AbstractDelaunayGraphBuilder builder = new VorTreeBuilder(points);
            add(builder.build(), new DelaunayGraphPainter(Color.BLACK, 2, DelaunayGraphViewMode.NO_TRIANGLES));
            for (final Point p : points) {
                add(p, new PointPainter(4));
            }
        }
    }

    public static class Figure4 extends Figure {
        @Override
        public void build() {
            final List<Point> points = getPoints();
            final AbstractDelaunayGraphBuilder builder = new VorTreeBuilder(points);
            add(builder.build(), new DelaunayGraphPainter(Color.BLACK, 2, DelaunayGraphViewMode.NO_TRIANGLES_CIRCUM));
            for (final Point p : points) {
                add(p, new PointPainter(4));
            }
        }
    }

    public static void main(@NotNull final String[] args) {
        new View.Window(new Figure1());
    }
}

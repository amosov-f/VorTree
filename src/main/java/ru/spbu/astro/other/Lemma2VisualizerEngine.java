package ru.spbu.astro.other;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.BindDelaunayGraphBuilder;
import ru.spbu.astro.graphics.ClickableView;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.utility.ColorGenerator;
import ru.spbu.astro.utility.PointGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Lemma2VisualizerEngine {

    public static class Figure1 extends ClickableView {
        @Override
        public void build() {
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


            AbstractDelaunayGraphBuilder builder = new BindDelaunayGraphBuilder(points, 2);

            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph dp = builder.build(pointIds);
            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph d = builder.build();

            Collection<Integer> bindPointIds = d.getBindPointIds(dp);
            bindPointIds.add(points.size() - 1);

            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph d1 = builder.build(bindPointIds);

            add(d, new DelaunayGraphPainter(Color.BLACK, 4, DelaunayGraphViewMode.NO_TRIANGLES));
            add(dp, new DelaunayGraphPainter(ColorGenerator.nextLight(), 4, DelaunayGraphViewMode.CREEP_ONLY));
            add(d1, new DelaunayGraphPainter(1, DelaunayGraphViewMode.NO_TRIANGLES));
            add(q, "q");
        }

        public Figure1() {
            setSize(1000, 660);
            setBackground(Color.white);
            build();
        }
    }

    public static void main(String[] args) {
        Component balls = new Figure1();
        JWindow window = new JWindow();
        window.setSize(balls.getSize());
        window.add(balls);
        window.setVisible(true);
    }

}

package ru.spbu.astro;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VisadDelaunayGraphBuilder;
import ru.spbu.astro.graphics.ClickableView;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.search.MapReduceVorTreeBuilder;
import ru.spbu.astro.search.VorTreeBuilder;
import ru.spbu.astro.utility.Plotter;
import ru.spbu.astro.utility.PointGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

public class VorTreeVisualizerEngine {

    public static class Figure extends ClickableView {

        Figure() {
            setSize(1000, 660);
            setBackground(Color.white);
            build();
        }

        @Override
        public void build() {
            final List<Point> points = PointGenerator.nextUniforms(10, 1000 * getWidth(), 1000 * getHeight());

            final AbstractDelaunayGraphBuilder builder1 = new VisadDelaunayGraphBuilder(points);
            final AbstractVorTreeBuilder builder2 = new MapReduceVorTreeBuilder(points);

            long t1 = System.currentTimeMillis();
            final AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph1 = builder1.build();
            long t2 = System.currentTimeMillis();
            final AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph2 = builder2.build(2);
            long t3 = System.currentTimeMillis();

            System.out.println("Visad time = " + (t2 - t1) / 1000);
            System.out.println("MapReduce VoR-tree time = " + (t3 - t2) / 1000);

            add(graph2);
        }

    }

    public static void main(String[] args) {
        final JFrame frame = new JFrame();
        final Component component = new Figure();
        frame.add(component);
        frame.setSize(component.getSize());
        frame.setVisible(true);

        if (!NativeDelaunayGraphBuilder.COUNT.isEmpty()) {
            final Plot2DPanel plot = new Plot2DPanel();
            plot.addPlot(Plotter.linePlot("count", Color.BLUE, NativeDelaunayGraphBuilder.COUNT));
            new FrameView(plot);
        }
    }

}

package ru.spbu.astro;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.graphics.ClickableView;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.search.mapreduce.MapReduceVorTreeBuilder;
import ru.spbu.astro.utility.Plotter;
import ru.spbu.astro.utility.PointGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class VorTreeVisualizerEngine {

    public static class Picture extends ClickableView {

        Picture() {
            setSize(1000, 660);
            setBackground(Color.white);
            build();
        }

        @Override
        public void build() {
            Collection<Point> points = PointGenerator.nextUniforms(10, 1000 * getWidth(), 1000 * getHeight());


            //AbstractDelaunayGraphBuilder builder1 = new BindDelaunayGraphBuilder(points, 2);
            AbstractDelaunayGraphBuilder builder2 = new MapReduceVorTreeBuilder(points, 2);

            long t1 = System.currentTimeMillis();

            //AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph1 = builder1.build();

            long t2 = System.currentTimeMillis();

            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph2 = builder2.build();

            long t3 = System.currentTimeMillis();

            System.out.println("bind time = " + (t2 - t1) / 1000);
            System.out.println("vor tree time = " + (t3 - t2) / 1000);

            add(graph2);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Component component = new Picture();
        frame.add(component);
        frame.setSize(component.getSize());
        frame.setVisible(true);

        if (!NativeDelaunayGraphBuilder.count.isEmpty()) {
            Plot2DPanel plot = new Plot2DPanel();
            plot.addPlot(Plotter.linePlot("count", Color.BLUE, NativeDelaunayGraphBuilder.count));
            new FrameView(plot);
        }

        /*
        for (int m = 30; m < 50; ++m) {
            double average = 0;
            for (int t = 0; t < 10; ++t) {
                Binder binder = new Binder(1000, m, SIZE_X, SIZE_Y);
                average += binder.getRate();
            }
            average /= 10;
            System.out.println(m + " " + average);
        }
        */
    }
}

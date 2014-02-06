package ru.spbu.astro;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.graphics.ClickableView;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.utility.Plotter;
import ru.spbu.astro.utility.PointGenerator;
import ru.spbu.astro.vortree.VorTreeBuilder;

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
            Collection<Point> points = PointGenerator.nextUniforms(1000, 1000 * getWidth(), 1000 * getHeight());

            AbstractDelaunayGraphBuilder builder = new VorTreeBuilder(points, 2);

            AbstractDelaunayGraphBuilder.AbstractDelaunayGraph graph = builder.build();

            add(graph);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Component component = new Picture();
        frame.add(component);
        frame.setSize(component.getSize());
        frame.setVisible(true);

        Plot2DPanel plot = new Plot2DPanel();
        plot.addPlot(Plotter.linePlot("count", Color.BLUE, NativeDelaunayGraphBuilder.count));
        new FrameView(plot);

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

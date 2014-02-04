package ru.spbu.astro;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.graphics.VorDelaunayGraphView;
import ru.spbu.astro.utility.Plotter;

import javax.swing.*;
import java.awt.*;

public class VorDelaunayGraphVisualizerEngine {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.add(new VorDelaunayGraphView(1000, 2));
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

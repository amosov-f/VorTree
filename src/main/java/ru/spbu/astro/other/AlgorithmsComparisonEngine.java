package ru.spbu.astro.other;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VorDelaunayGraphBuilder;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.utility.Plotter;
import ru.spbu.astro.utility.PointGenerator;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class AlgorithmsComparisonEngine {

    private static final int LN = 1;
    private static final int RN = 100;

    private static final int T = 5;

    public static void main(String[] args) throws Exception {
        Map<Integer, Double> f1 = new HashMap();
        Map<Integer, Double> f2 = new HashMap();

        Scanner fin = new Scanner(new FileInputStream("datasets/time.txt"));
        while (fin.hasNextInt()) {
            int n = fin.nextInt();
            f1.put(n, fin.nextDouble());
            f2.put(n, fin.nextDouble());
        }

        PrintWriter fout = new PrintWriter(new FileOutputStream("datasets/time.txt", true));

        for (int n = LN; n <= RN; ++n) {
            System.out.print(n + ": ");

            if (!f1.containsKey(n)) {
                f1.put(n, 0.0);
                f2.put(n, 0.0);
                for (int t = 0; t < T; ++t) {
                    Collection<Point> points = PointGenerator.nextUniforms(n);

                    NativeDelaunayGraphBuilder nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(points, 2);
                    VorDelaunayGraphBuilder vorDelaunayGraphBuilder = new VorDelaunayGraphBuilder(points, 2);

                    long t1 = System.currentTimeMillis();
                    nativeDelaunayGraphBuilder.build();
                    long t2 = System.currentTimeMillis();
                    vorDelaunayGraphBuilder.build();
                    long t3 = System.currentTimeMillis();

                    f1.put(n, f1.get(n) + (t2 - t1) / 1000.0);
                    f2.put(n, f2.get(n) + (t3 - t2) / 1000.0);

                    System.out.print((t + 1) + " ");
                }

                f1.put(n, f1.get(n) / T);
                f2.put(n, f2.get(n) / T);

                fout.println(String.format("%d %f %f", n, f1.get(n), f2.get(n)));
                fout.flush();

            } else {
                System.out.print("calculated");
            }

            System.out.println();
        }

        Plot2DPanel plot = new Plot2DPanel("SOUTH");
        plot.addPlot(Plotter.linePlot("native", Color.BLUE, f1));
        plot.addPlot(Plotter.linePlot("vor", Color.RED, f2));
        new FrameView(plot);
    }
}

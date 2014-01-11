package ru.spbu.astro;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VorDelaunayGraphBuilder;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.utility.PointGenerator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class AlgorithmsComparatorEngine {

    private static final int LN = 10;
    private static final int LR = 90;

    private static final int T = 3;

    public static void main(String[] args) throws Exception {
        double[] x = new double[LR - LN + 1];
        double[] y1 = new double[LR - LN + 1];
        double[] y2 = new double[LR - LN + 1];
        Set<Integer> calc = new HashSet();

        Scanner fin = new Scanner(new FileInputStream("output/time.txt"));
        while (fin.hasNextInt()) {
            int n = fin.nextInt();
            calc.add(n);
            y1[n - LN] = fin.nextDouble();
            y2[n - LN] = fin.nextDouble();
        }

        PrintWriter fout = new PrintWriter(new FileOutputStream("output/time.txt"));

        for (int n = LN; n <= LR; ++n) {
            System.out.println(n);

            x[n - LN] = n;

            if (!calc.contains(n)) {
                for (int t = 0; t < T; ++t) {
                    Collection<Point> points = PointGenerator.nextUniforms(n);

                    NativeDelaunayGraphBuilder nativeDelaunayGraphBuilder = new NativeDelaunayGraphBuilder(points, 2);
                    VorDelaunayGraphBuilder vorDelaunayGraphBuilder = new VorDelaunayGraphBuilder(points, 2);

                    long t1 = System.currentTimeMillis();

                    nativeDelaunayGraphBuilder.build();
                    long t2 = System.currentTimeMillis();
                    y1[n - LN] += (t2 - t1) / 1000.0;
                    vorDelaunayGraphBuilder.build();
                    long t3 = System.currentTimeMillis();
                    y2[n - LN] += (t3 - t2) / 1000.0;

                }

                y1[n - LN] /= T;
                y2[n - LN] /= T;
            }

            fout.println(String.format("%d %f %f", (int) x[n - LN], y1[n - LN], y2[n - LN]));
            fout.flush();
        }

        Plot2DPanel plot = new Plot2DPanel("SOUTH");
        plot.addLinePlot("native", x, y1);
        plot.addLinePlot("vor", x, y2);

        new FrameView(plot);
    }
}

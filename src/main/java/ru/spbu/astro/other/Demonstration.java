package ru.spbu.astro.other;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.graphics.ClickableView;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.utility.PointGenerator;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * User: amosov-f
 * Date: 01.02.15
 * Time: 4:12
 */
public class Demonstration {
    private static final int WIDTH = 1100;
    private static final int HEIGHT = 660;

    private static final int N = 50;

    public static abstract class Figure extends ClickableView {
        public Figure() {
            setSize(Demonstration.WIDTH, Demonstration.HEIGHT);
            setBackground(Color.WHITE);
            build();
        }
    }

    @NotNull
    static List<Point> getPoints() {
        InputStream in = DefinitionsDemonstration.class.getResourceAsStream("/input/points.txt");
        if (in == null) {
            final PrintWriter fout;
            try {
                fout = new PrintWriter(new FileOutputStream("src/main/resources/input/points.txt"));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            final List<Point> points = PointGenerator.nextUniforms(N, 1000 * WIDTH, 1000 * HEIGHT);
            for (final Point p : points) {
                fout.println(p.toString());
            }
            fout.flush();
            in = DefinitionsDemonstration.class.getResourceAsStream("/input/points.txt");
        }
        final Scanner fin = new Scanner(in);
        final List<Point> points = new ArrayList<>();
        for (int i = 0; i < N; ++i) {
            points.add(new Point(fin.nextLine()));
        }
        return points;
    }
}

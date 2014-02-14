package ru.spbu.astro.utility;

import org.math.plot.plots.LinePlot;
import org.math.plot.plots.Plot;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Plotter {
    public static Plot linePlot(final String name, final Color color, final Map<Integer, ? extends Number> f) {
        final List<Integer> keyList = new ArrayList<>(f.keySet());
        Collections.sort(keyList);

        double[][] xy = new double[keyList.size()][2];

        for (int i = 0; i < xy.length; ++i) {
            xy[i][0] = keyList.get(i);
            xy[i][1] = f.get(keyList.get(i)).doubleValue();
        }

        return new LinePlot(name, color, xy);
    }
}

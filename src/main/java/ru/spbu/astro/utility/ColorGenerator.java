package ru.spbu.astro.utility;

import java.awt.*;
import java.util.Random;

public final class ColorGenerator {

    public static final Color EDGE_DEFAULT = new Color(140, 100, 140);

    public static Color nextLight() {
        Random rand = new Random();
        return new Color(rand.nextInt(100) + 150, rand.nextInt(100) + 150, rand.nextInt(100) + 150);
    }

    public static Color nextRed() {
        Random rand = new Random();
        return new Color(rand.nextInt(106) + 150, 0, 0);
    }

    public static Color nextDeep(int level) {
        return Color.getHSBColor(240f / 360, 1f, 1 - Math.min(level * 30f / 255, 1f));
    }
}

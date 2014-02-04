package ru.spbu.astro.other;

import ru.spbu.astro.graphics.BinderView;

public class BinderVisualizerEngine {

    public static void main(String[] args) {
        BinderView frame = new BinderView(100, 2);
        frame.setVisible(true);

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

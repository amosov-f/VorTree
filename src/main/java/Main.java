import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    private static int POINTS_COUNT = 1000;

    private static int SIZE_X = 1320;
    private static int SIZE_Y = 660;

    public static void main(String[] args) throws Exception {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < POINTS_COUNT / 2; ++i) {
            points.add(new Point(new double[]{new Random().nextGaussian(), new Random().nextGaussian()}));
        }
        for (int i = 0; i < POINTS_COUNT / 2; ++i) {
            points.add(new Point(new double[]{new Random().nextGaussian() + 4, new Random().nextGaussian()}));
        }

        VorTree tree = new VorTree(points);
        Component component = tree.getComponent(SIZE_X, SIZE_Y);

        JWindow window = new JWindow();
        window.setSize(SIZE_X, SIZE_Y);
        window.getContentPane().add(component);
        window.setVisible(true);
    }
}

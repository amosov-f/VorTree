package ru.spbu.astro.graphics;

import javax.swing.*;
import java.awt.*;

public class View {

    public static class Frame extends JFrame {
        public Frame(final Component component) {
            setSize(component.getSize());
            add(component);
            setVisible(true);
        }
    }

    public static class Window extends JWindow {
        public Window(final Component component) {
            setSize(component.getSize());
            add(component);
            setVisible(true);
        }
    }

}

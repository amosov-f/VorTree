package ru.spbu.astro.graphics;

import java.awt.*;

public interface Painter<T> {
    void paint(T object, Graphics g);
    void sign(T object, String signature, Graphics g);
}

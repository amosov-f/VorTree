package ru.spbu.astro.graphics;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public interface Painter<T> {
    void paint(@NotNull T object, @NotNull Graphics g);
    void sign(@NotNull T object, @NotNull String signature, @NotNull Graphics g);
}

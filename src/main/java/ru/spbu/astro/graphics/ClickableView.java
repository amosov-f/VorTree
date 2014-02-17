package ru.spbu.astro.graphics;


import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public abstract class ClickableView extends CenteredView {

    public abstract void build();

    public void clear() {
        frameRect = null;
        items.clear();
    }

    public ClickableView() {

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getGraphics().clearRect(0, 0, getWidth(), getHeight());
                clear();
                build();
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }
}

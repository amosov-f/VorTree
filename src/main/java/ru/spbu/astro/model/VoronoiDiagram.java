package ru.spbu.astro.model;

import ru.spbu.astro.graphics.Framable;
import ru.spbu.astro.search.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VoronoiDiagram implements Framable, Index {
    private final List<Point> sites = new ArrayList<>();

    public VoronoiDiagram(final Collection<Point> sites) {
        this.sites.addAll(sites);
    }

    @Override
    public int getNearestNeighbor(Point p) {
        int bestNN = 0;
        for (int i = 1; i < sites.size(); ++i) {
            if (p.distance2to(sites.get(i)) < p.distance2to(sites.get(bestNN))) {
                bestNN = i;
            }
        }
        return bestNN;
    }

    @Override
    public Rectangle getFrameRectangle() {
        return new Rectangle(sites);
    }

    public List<Point> getSites() {
        return sites;
    }
}

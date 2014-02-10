package ru.spbu.astro.search;

import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.WalkableDelaunayGraphBuilder;
import ru.spbu.astro.model.Ball;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;

import java.util.*;

public abstract class AbstractVorTreeBuilder extends WalkableDelaunayGraphBuilder {
    protected final AbstractDelaunayGraphBuilder binder;
    protected final int division;

    protected AbstractVorTreeBuilder(final Iterable<Point> points, int division) {
        super(points);
        binder = new NativeDelaunayGraphBuilder(points);
        this.division = division;
    }

    protected AbstractVorTreeBuilder(final Collection<Integer> pointIds, int division) {
        super(pointIds);
        binder = new NativeDelaunayGraphBuilder(pointIds);
        this.division = division;
    }

    public class AbstractVorTree extends WalkableDelaunayGraph implements Index {
        protected final RTree rTree;

        protected AbstractVorTree(final Collection<Integer> pointIds) {
            super(pointIds);
            rTree = new RTree();
        }

        protected AbstractVorTree(final AbstractVorTree t) {
            super(t);
            rTree = new RTree(t.rTree);
        }

        @Override
        public boolean isCreep(final Simplex s) {
            final Ball b = new Ball(getPoints(s));
            final Point center = b.getCenter();

            for (RTree t : rTree.sons) {
                int curNN = t.getNearestNeighbor(center);
                if (b.contains(id2point.get(curNN))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getNearestNeighbor(final Point p) {
            return rTree.getNearestNeighbor(p);
        }

        protected class RTree implements Index {
            private final Rectangle cover;
            private final Set<Integer> pointIds;

            public final List<RTree> sons = new ArrayList<>();

            public RTree() {
                cover = new Rectangle(id2point.get(getVertices()).values());
                pointIds = getVertices();
            }

            public RTree(final RTree t) {
                cover = t.cover;
                pointIds = new HashSet<>(t.pointIds);
                for (RTree son : t.sons) {
                    sons.add(new RTree(son));
                }
            }

            @Override
            public int getNearestNeighbor(final Point p) {
                final PriorityQueue<RTree> heap = new PriorityQueue<>(rTree.sons.size() + 1, new Comparator<RTree>() {
                    @Override
                    public int compare(RTree v1, RTree v2) {
                        return Long.compare(v1.cover.distance2to(p), v2.cover.distance2to(p));
                    }
                });
                heap.add(rTree);
                long bestDist2 = Long.MAX_VALUE;
                int bestNN = -1;
                while (!heap.isEmpty()) {
                    RTree u = heap.poll();
                    if (u.sons.isEmpty()) {
                        for (int pointId : u.pointIds) {
                            if (id2point.get(pointId).distance2to(p) < bestDist2) {
                                bestNN = pointId;
                                bestDist2 = id2point.get(pointId).distance2to(p);
                            }
                        }
                        if (contains(bestNN, p)) {
                            return bestNN;
                        }
                    } else {
                        for (RTree v : u.sons) {
                            heap.add(v);
                        }
                    }
                }
                return -1;
            }
        }
    }
}

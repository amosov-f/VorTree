package ru.spbu.astro.search;

import ru.spbu.astro.Message;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.WalkableDelaunayGraphBuilder;
import ru.spbu.astro.model.Ball;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;

import java.util.*;

public abstract class AbstractVorTreeBuilder extends WalkableDelaunayGraphBuilder {
    protected final AbstractDelaunayGraphBuilder binder = new NativeDelaunayGraphBuilder(id2point);
    protected final int division;

    protected AbstractVorTreeBuilder(final Collection<Point> points, int division) {
        super(points);
        this.division = division;
    }

    protected AbstractVorTreeBuilder(final Map<Integer, Point> id2point, int division) {
        super(id2point);
        this.division = division;
    }

    public class AbstractVorTree extends WalkableDelaunayGraph implements Index {
        protected RTree rTree;

        protected AbstractVorTree() {
//            rTree = new RTree();
        }

        protected AbstractVorTree(final Collection<Integer> pointIds) {
            super(pointIds);

            rTree = new RTree(getFrameRectangle(), pointIds);
        }

        protected AbstractVorTree(final AbstractVorTree t) {
            super(t);
            rTree = new RTree(t.rTree);
        }

        protected AbstractVorTree(
                final Map<Integer, Set<Integer>> neighbors,
                final Set<Simplex> simplexes,
                final List<Integer> borderVertices,
                final Map<Simplex, Set<Simplex>> side2simplexes,
                final RTree rTree
        ) {
            super(neighbors, simplexes, borderVertices, side2simplexes);
            this.rTree = new RTree(rTree);
        }

        @Override
        public boolean isCreep(final Simplex s) {
            final Ball b = new Ball(get(s));
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

            public final List<RTree> sons;

            public RTree() {
                this(new Rectangle(), new HashSet<Integer>());
                //this(new Rectangle(id2point.get(getVertices()).values()), getVertices(), new ArrayList<RTree>());
            }

            public RTree(final RTree t) {
                this(t.cover, t.pointIds, t.sons);
            }

            public RTree(final Rectangle cover, final Collection<Integer> pointIds) {
                this(cover, pointIds, new ArrayList<RTree>());
            }

            private RTree(final Rectangle cover, final Collection<Integer> pointIds, final Collection<RTree> sons) {
                this.cover = cover;
                this.pointIds = new HashSet<>(pointIds);
                this.sons = new ArrayList<>(sons);
            }

            public RTree(Message.VorTreeMessage.RTreeMessage message) {
                cover = Rectangle.fromMessage(message.getCover());
                pointIds = new HashSet<>(message.getPointIdsList());

                sons = new ArrayList<>();
                for (final Message.VorTreeMessage.RTreeMessage sonMessage : message.getSonsList()) {
                    sons.add(new RTree(sonMessage));
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

            public Message.VorTreeMessage.RTreeMessage toMessage() {
                final Message.VorTreeMessage.RTreeMessage.Builder builder = Message.VorTreeMessage.RTreeMessage.newBuilder();
                builder.setCover(cover.toMessage());
                builder.addAllPointIds(pointIds);
                for (RTree son : sons) {
                    builder.addSons(son.toMessage());
                }
                return builder.build();
            }

        }

    }

}

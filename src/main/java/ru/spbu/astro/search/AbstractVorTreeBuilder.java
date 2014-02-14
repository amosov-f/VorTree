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

    protected AbstractVorTreeBuilder(final Collection<Point> points) {
        super(points);
    }

    protected AbstractVorTreeBuilder(final Map<Integer, Point> id2point) {
        super(id2point);
    }

    public abstract AbstractVorTree build(final Collection<Integer> pointIds, int division);

    @Override
    public AbstractVorTree build(final Collection<Integer> pointIds) {
        return build(pointIds, 2);
    }

    public AbstractVorTree build(int division) {
        return build(id2point.keySet(), division);
    }

    public AbstractVorTree build(final Message.AbstractVorTree message) {
        return new AbstractVorTree(message);
    }

    public class AbstractVorTree extends WalkableDelaunayGraph implements Index {
        private final RTree rTree;

        protected AbstractVorTree(final Collection<Integer> pointIds) {
            super(pointIds);
            rTree = new RTree(getFrameRectangle(), pointIds);
        }

        protected AbstractVorTree(final AbstractVorTree t) {
            super(t);
            rTree = new RTree(t.rTree);
        }

        protected AbstractVorTree(final Message.AbstractVorTree message) {
            super(message.getWalkableDelaunayGraph());
            rTree = new RTree(message.getRTree());
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

        public void addSon(final AbstractVorTree t) {
            rTree.sons.add(t.rTree);
        }

        public Message.AbstractVorTree toAbstractVorTreeMessage() {
            final Message.AbstractVorTree.Builder builder = Message.AbstractVorTree.newBuilder();
            builder.setWalkableDelaunayGraph(super.toWalkableDelaunayGraphMessage());
            builder.setRTree(rTree.toMessage());
            return builder.build();
        }

        protected class RTree implements Index {
            private final Rectangle cover;
            private final Set<Integer> pointIds;

            public final List<RTree> sons;

            public RTree() {
                this(new Rectangle(), new HashSet<Integer>());
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

            public RTree(Message.AbstractVorTree.RTree message) {
                cover = Rectangle.fromMessage(message.getCover());
                pointIds = new HashSet<>(message.getPointIdsList());

                sons = new ArrayList<>();
                for (final Message.AbstractVorTree.RTree sonMessage : message.getSonsList()) {
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

            public Message.AbstractVorTree.RTree toMessage() {
                final Message.AbstractVorTree.RTree.Builder builder = Message.AbstractVorTree.RTree.newBuilder();
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

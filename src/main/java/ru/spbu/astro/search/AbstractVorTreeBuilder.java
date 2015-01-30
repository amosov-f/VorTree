package ru.spbu.astro.search;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.Message;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.WalkableDelaunayGraphBuilder;
import ru.spbu.astro.graphics.Framable;
import ru.spbu.astro.model.Ball;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;

import java.util.*;

public abstract class AbstractVorTreeBuilder extends WalkableDelaunayGraphBuilder {
    @NotNull
    protected final AbstractDelaunayGraphBuilder binder = new NativeDelaunayGraphBuilder(id2point);

    protected AbstractVorTreeBuilder(@NotNull final Collection<Point> points) {
        super(points);
    }

    protected AbstractVorTreeBuilder(@NotNull final Map<Integer, Point> id2point) {
        super(id2point);
    }

    @NotNull
    public abstract AbstractVorTree build(@NotNull final Collection<Integer> pointIds, final int division);

    @NotNull
    @Override
    public AbstractVorTree build(@NotNull final Collection<Integer> pointIds) {
        return build(pointIds, 2);
    }

    public AbstractVorTree build(final int division) {
        return build(id2point.keySet(), division);
    }

    @NotNull
    public AbstractVorTree build(@NotNull final Message.AbstractVorTree message) {
        return new AbstractVorTree(message);
    }

    public class AbstractVorTree extends WalkableDelaunayGraph implements Index {
        @NotNull
        private final RTree rTree;

        protected AbstractVorTree(@NotNull final Collection<Integer> pointIds) {
            super(pointIds);
            rTree = new RTree(getFrameRectangle(), pointIds);
        }

        protected AbstractVorTree(@NotNull final Message.AbstractVorTree message) {
            super(message.getWalkableDelaunayGraph());
            rTree = new RTree(message.getRTree());
        }

        @Override
        public boolean isCreep(@NotNull final Simplex s) {
            final Ball b = new Ball(get(s));
            final Point center = b.getCenter();

            for (final RTree t : rTree.sons) {
                final int curNN = t.getNearestNeighbor(center);
                if (b.contains(id2point.get(curNN))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getNearestNeighbor(@NotNull final Point p) {
            return rTree.getNearestNeighbor(p);
        }

        public void addSon(@NotNull final AbstractVorTree t) {
            rTree.sons.add(t.rTree);
        }

        @NotNull
        public RTree getRTree() {
            return rTree;
        }

        @NotNull
        public Message.AbstractVorTree toAbstractVorTreeMessage() {
            final Message.AbstractVorTree.Builder builder = Message.AbstractVorTree.newBuilder();
            builder.setWalkableDelaunayGraph(super.toWalkableDelaunayGraphMessage());
            builder.setRTree(rTree.toMessage());
            return builder.build();
        }

        public final class RTree implements Index, Framable {
            @NotNull
            public final Rectangle cover;
            public final List<RTree> sons;
            @NotNull
            private final Set<Integer> pointIds;

            public RTree(@NotNull final RTree t) {
                this(t.cover, t.pointIds, t.sons);
            }

            public RTree(@NotNull final Rectangle cover, @NotNull final Collection<Integer> pointIds) {
                this(cover, pointIds, new ArrayList<RTree>());
            }

            private RTree(@NotNull final Rectangle cover,
                          @NotNull final Collection<Integer> pointIds,
                          @NotNull final Collection<RTree> sons) {
                this.cover = cover;
                this.pointIds = new HashSet<>(pointIds);
                this.sons = new ArrayList<>(sons);
            }

            public RTree(@NotNull final Message.AbstractVorTree.RTree message) {
                cover = Rectangle.fromMessage(message.getCover());
                pointIds = new HashSet<>(message.getPointIdsList());

                sons = new ArrayList<>();
                for (final Message.AbstractVorTree.RTree sonMessage : message.getSonsList()) {
                    sons.add(new RTree(sonMessage));
                }
            }

            @NotNull
            public List<Point> getPoints() {
                return get(pointIds);
            }

            @Override
            public int getNearestNeighbor(@NotNull final Point p) {
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
                    final RTree u = heap.poll();
                    if (u.sons.isEmpty()) {
                        for (final int pointId : u.pointIds) {
                            if (id2point.get(pointId).distance2to(p) < bestDist2) {
                                bestNN = pointId;
                                bestDist2 = id2point.get(pointId).distance2to(p);
                            }
                        }
                        if (contains(bestNN, p)) {
                            return bestNN;
                        }
                    } else {
                        for (final RTree v : u.sons) {
                            heap.add(v);
                        }
                    }
                }
                return -1;
            }

            @NotNull
            @Override
            public Rectangle getFrameRectangle() {
                return cover;
            }

            @NotNull
            public Message.AbstractVorTree.RTree toMessage() {
                final Message.AbstractVorTree.RTree.Builder builder = Message.AbstractVorTree.RTree.newBuilder();
                builder.setCover(cover.toMessage());
                builder.addAllPointIds(pointIds);
                for (final RTree son : sons) {
                    builder.addSons(son.toMessage());
                }
                return builder.build();
            }
        }
    }
}
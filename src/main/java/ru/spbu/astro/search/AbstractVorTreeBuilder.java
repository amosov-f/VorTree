package ru.spbu.astro.search;

import ru.spbu.astro.Schema;
import ru.spbu.astro.Schema.msg;
import ru.spbu.astro.Schema.msg.Builder;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.WalkableDelaunayGraphBuilder;
import ru.spbu.astro.model.Ball;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

public abstract class AbstractVorTreeBuilder extends WalkableDelaunayGraphBuilder {
    protected int m = 2;
    protected AbstractDelaunayGraphBuilder binder;

    protected AbstractVorTreeBuilder(Iterable<Point> points, int m) {
        super(points);
        this.m = m;
        binder = new NativeDelaunayGraphBuilder(points);
    }

    protected AbstractVorTreeBuilder(Collection<Integer> pointIds, int m) {
        super(pointIds);
        this.m = m;
        binder = new NativeDelaunayGraphBuilder(pointIds);
    }

    public static class AbstractVorTree extends WalkableDelaunayGraph implements Index {
        public RTree rTree;
        protected ArrayList<AbstractVorTree> sons = new ArrayList();

        void toMessage(final Schema.msg.Builder builder){
            builder.setRTree(rTree.toMessage);
            for (final AbstractVorTree son:sons){
                builder.addSons(son.toMessage());
            }
            super.toMessage(builder)
        }

        msg toMessage(){
            final Builder builder = msg.newBuilder();
            toMessage(builder);
            return builder.build();
        }

        public AbstractVorTree() {
            this(new ArrayList());
        }

        public AbstractVorTree(Collection<Integer> pointIds) {
            super(pointIds);
        }

        protected AbstractVorTree(AbstractVorTree t) {
            super(t);
            rTree = (RTree) t.rTree.clone();
            for (AbstractVorTree son : t.sons) {
                sons.add(new AbstractVorTree(son));
            }
        }

        @Override
        public boolean isCreep(Simplex s) {
            Ball b = new Ball(getPoints(s));
            Point center = b.getCenter();

            for (AbstractVorTree t : sons) {
                int curNN = t.getNearestNeighbor(center);
                if (b.contains(id2point.get(curNN))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getNearestNeighbor(final Point p) {
            PriorityQueue<RTree> heap = new PriorityQueue<RTree>(rTree.sons.size() + 1, new Comparator<RTree>() {
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


        protected class RTree {
            private Rectangle cover;
            public ArrayList<RTree> sons = new ArrayList();
            private ArrayList<Integer> pointIds;

            public RTree(Collection<Integer> pointIds) {
                this.cover = new Rectangle(id2point.get(pointIds).values());
                this.pointIds = new ArrayList(pointIds);
            }

            @Override
            protected Object clone() {
                RTree t;
                try {
                    t = (RTree) super.clone();
                } catch (CloneNotSupportedException e) {
                    throw new InternalError(e.toString());
                }
                t.cover = (Rectangle) cover.clone();
                for (RTree son : sons) {
                    t.sons.add((RTree) son.clone());
                }
                t.pointIds = new ArrayList(pointIds);
                return t;
            }
        }
    }
}

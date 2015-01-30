package ru.spbu.astro.delaunay;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.model.Point;

import java.util.*;

public final class NativeDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {
    public static final Map<Integer, Integer> COUNT = new HashMap<>();

    public NativeDelaunayGraphBuilder(@NotNull final Collection<Point> points) {
        super(points);
    }

    public NativeDelaunayGraphBuilder(@NotNull final Map<Integer, Point> id2point) {
        super(id2point);
    }

    @NotNull
    @Override
    public NativeDelaunayGraph build(@NotNull final Collection<Integer> pointIds) {
        return new NativeDelaunayGraph(pointIds);
    }

    public final class NativeDelaunayGraph extends AbstractDelaunayGraph {
        @NotNull
        private final List<Integer> pointIds;

        NativeDelaunayGraph(@NotNull final Collection<Integer> pointIds) {
            super(pointIds);
            this.pointIds = new ArrayList<>(pointIds);

            if (pointIds.size() <= dim()) {
                return;
            }
            if (pointIds.size() == dim() + 1) {
                addSimplex(new Simplex(pointIds));
                return;
            }

            System.out.println(pointIds.size());

            if (!COUNT.containsKey(pointIds.size())) {
                COUNT.put(pointIds.size(), 0);
            }
            COUNT.put(pointIds.size(), COUNT.get(pointIds.size()) + 1);

            build(new BitSet(pointIds.size()), 0, dim() + 1);
        }

        private void build(@NotNull final BitSet mask, final int pos, final int rem) {
            if (rem == 0) {
                final List<Integer> vertices = new ArrayList<>();
                for (int i = 0; i < mask.size(); ++i) {
                    if (mask.get(i)) {
                        vertices.add(pointIds.get(i));
                    }
                }
                final Simplex s = new Simplex(vertices);
                if (!isCreep(s)) {
                    addSimplex(s);
                }
                return;
            }
            if (pointIds.size() - pos < rem) {
                return;
            }

            mask.set(pos);
            build(mask, pos + 1, rem - 1);
            mask.set(pos, false);
            build(mask, pos + 1, rem);
        }
    }
}

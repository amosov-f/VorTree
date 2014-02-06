package ru.spbu.astro.delaunay;

import ru.spbu.astro.model.Point;

import java.util.*;

public class NativeDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {
    public static Map<Integer, Integer> count = new HashMap();

    public NativeDelaunayGraphBuilder(Collection<Point> points) {
        super(points);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds, int level) {
        return new NativeDelaunayGraph(pointIds);
    }

    public class NativeDelaunayGraph extends AbstractDelaunayGraph {

        private BitSet mask;
        private List<Integer> pointIds;

        NativeDelaunayGraph(Collection<Integer> pointIds) {
            super(pointIds);

            if (pointIds.size() <= dim) {
                return;
            }

            if (pointIds.size() == dim + 1) {
                Simplex s = new Simplex(pointIds);
                addGraph(s.toGraph());
                simplexes.add(s);
                return;
            }

            this.pointIds = new ArrayList(pointIds);
            mask = new BitSet(pointIds.size());

            System.out.println(pointIds.size());

            if (!count.containsKey(pointIds.size())) {
                count.put(pointIds.size(), 0);
            }
            count.put(pointIds.size(), count.get(pointIds.size()) + 1);

            build(0, dim + 1);
        }

        private void build(int pos, int rem) {
            if (rem == 0) {
                List<Integer> vertices = new ArrayList();
                for (int i = 0; i < mask.size(); ++i) {
                    if (mask.get(i)) {
                        vertices.add(pointIds.get(i));
                    }
                }
                Simplex s = new Simplex(vertices);
                if (!isCreep(s, pointIds)) {
                    addGraph(s.toGraph());
                    addSimplex(s);
                }
                return;
            }
            if (pointIds.size() - pos < rem) {
                return;
            }

            mask.set(pos);
            build(pos + 1, rem - 1);
            mask.set(pos, false);
            build(pos + 1, rem);
        }
    }
}

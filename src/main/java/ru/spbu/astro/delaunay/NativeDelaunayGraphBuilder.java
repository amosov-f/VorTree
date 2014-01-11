package ru.spbu.astro.delaunay;

import ru.spbu.astro.model.Point;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class NativeDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {
    public NativeDelaunayGraphBuilder(Collection<Point> points, int m) {
        super(points, m);
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

            if (pointIds.size() <= DIM) {
                return;
            }

            if (pointIds.size() == DIM + 1) {
                Triangle t = new Triangle(pointIds);
                addGraph(t.toGraph());
                triangles.add(t);
                return;
            }

            this.pointIds = new ArrayList(pointIds);
            mask = new BitSet(pointIds.size());

            //System.out.println("size: " + pointIds.size());

            build(0, DIM + 1);
        }

        private void build(int pos, int rem) {
            if (rem == 0) {
                List<Integer> vertices = new ArrayList();
                for (int i = 0; i < mask.size(); ++i) {
                    if (mask.get(i)) {
                        vertices.add(pointIds.get(i));
                    }
                }
                Triangle t = new Triangle(vertices);
                if (!new AbstractDelaunayGraphBuilder.Triangle(t).isCreep(pointIds)) {
                    addGraph(t.toGraph());
                    triangles.add(t);
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

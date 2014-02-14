package ru.spbu.astro.delaunay;

import ru.spbu.astro.model.Point;
import visad.Delaunay;
import visad.VisADException;

import java.util.*;

public final class VisadDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {

    public VisadDelaunayGraphBuilder(final Collection<Point> points) {
        super(points);
    }

    public VisadDelaunayGraphBuilder(final Map<Integer, Point> id2point) {
        super(id2point);
    }

    @Override
    public VisadDelaunayGraph build(final Collection<Integer> pointIds) {
        return new VisadDelaunayGraph(pointIds);
    }

    public class VisadDelaunayGraph extends AbstractDelaunayGraph {

        private final Delaunay delaunay;
        private final List<Integer> index2pointId;

        public VisadDelaunayGraph(final Collection<Integer> pointIds) {
            super(pointIds);

            index2pointId = new ArrayList<>(pointIds);

            final float[][] samples = new float[dim()][pointIds.size()];
            for (int i = 0; i < index2pointId.size(); ++i) {
                for (int d = 0; d < dim(); ++d) {
                    samples[d][i] = (float) id2point.get(index2pointId.get(i)).get(d);
                }
            }

            try {
                delaunay = Delaunay.factory(samples, false);
            } catch (VisADException e) {
                throw new InternalError(e.toString());
            }

            for (int t = 0; t < delaunay.Tri.length; ++t) {
                addSimplex(new Simplex(toPointIds(delaunay.Tri[t])));
            }
        }

        private List<Integer> toPointIds(int[] indexes) {
            final List<Integer> ids = new ArrayList<>(indexes.length);
            for (int index : indexes) {
                ids.add(index2pointId.get(index));
            }
            return ids;
        }

        @Override
        public String toString() {
            return delaunay.toString();
        }

    }

}

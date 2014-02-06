package ru.spbu.astro.delaunay;

import ru.spbu.astro.model.Point;
import visad.Delaunay;
import visad.VisADException;

import java.util.*;

public class VisadDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {

    public VisadDelaunayGraphBuilder(final Collection<Point> points) {
        super(points);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds, int level) {
        return new VisadDelaunayGraph(pointIds);
    }

    public class VisadDelaunayGraph extends AbstractDelaunayGraph {

        private Delaunay delaunay;
        private List<Integer> index2pointId;

        public VisadDelaunayGraph(final Collection<Integer> pointIds) {
            super(pointIds);

            index2pointId = new ArrayList(pointIds);
            float[][] samples = new float[dim][pointIds.size()];

            for (int i = 0; i < index2pointId.size(); ++i) {
                for (int d = 0; d < dim; ++d) {
                    samples[d][i] = (float) id2point.get(index2pointId.get(i)).get(d);
                }
            }

            try {
                delaunay = Delaunay.factory(samples, false);
            } catch (VisADException e) {
                e.printStackTrace();
                return;
            }

            if (delaunay == null) {
                return;
            }

            for (int u = 0; u < delaunay.Vertices.length; ++u) {
                for (int i = 0; i < delaunay.Vertices[u].length; ++i) {
                    int t = delaunay.Vertices[u][i];
                    for (int j = 0; j < delaunay.Tri[t].length; ++j) {
                        int v = delaunay.Tri[t][j];
                        addEdge(index2pointId.get(u), index2pointId.get(v));
                    }
                }
            }

            for (int t = 0; t < delaunay.Tri.length; ++t) {
                addSimplex(new Simplex(toPointIds(delaunay.Tri[t])));
            }
        }

        private ArrayList<Integer> toPointIds(int[] indexes) {
            ArrayList<Integer> ids = new ArrayList(indexes.length);
            for (int index : indexes) {
                ids.add(index2pointId.get(index));
            }
            return ids;
        }

        @Override
        public String toString() {
            return "VisadDelaunayGraph{" +
                    "delaunay=" + delaunay +
                    '}';
        }
    }
}

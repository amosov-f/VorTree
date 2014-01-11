package ru.spbu.astro.delaunay;

import ru.spbu.astro.model.Point;
import visad.Delaunay;
import visad.VisADException;

import java.util.*;

public class VisadDelaunayGraphBuilder extends AbstractDelaunayGraphBuilder {

    public VisadDelaunayGraphBuilder(final Collection<Point> points, int m) {
        super(points, m);
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
            float[][] samples = new float[DIM][pointIds.size()];

            for (int i = 0; i < index2pointId.size(); ++i) {
                for (int d = 0; d < DIM; ++d) {
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
                triangles.add(new Triangle(toPointIds(delaunay.Tri[t])));
            }
        }

        private int[] toPointIds(int[] indexes) {
            int[] ids = new int[indexes.length];
            for (int i = 0; i < indexes.length; ++i) {
                ids[i] = index2pointId.get(indexes[i]);
            }
            return ids;
        }

        /*@Override
        public List<Integer> getBorderVertices() {
            if (delaunay == null) {
                return new ArrayList();
            }

            List<Integer> borderVertices = new ArrayList();
            for (int v = 0; v < delaunay.Vertices.length; ++v) {
                Map<Integer, Integer> neibCount = new HashMap();
                for (int i = 0; i < delaunay.Vertices[v].length; ++i) {
                    int t = delaunay.Vertices[v][i];
                    for (int j = 0; j < delaunay.Edges[t].length; ++j) {
                        if (!neibCount.containsKey(delaunay.Edges[t][j])) {
                            neibCount.put(delaunay.Edges[t][j], 1);
                        } else {
                            neibCount.put(delaunay.Edges[t][j], neibCount.get(delaunay.Edges[t][j]) + 1);
                        }
                    }
                }
                int count2 = 0;
                for (Integer count : neibCount.values()) {
                    if (count == 2) {
                        count2++;

                    }
                }
                if (count2 != delaunay.Vertices[v].length) {
                    borderVertices.add(index2pointId.get(v));
                }
            }
            return borderVertices;
        }   */

        @Override
        public String toString() {
            return "VisadDelaunayGraph{" +
                    "delaunay=" + delaunay +
                    '}';
        }
    }
}

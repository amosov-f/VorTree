package ru.spbu.astro.search;

import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.io.Writable;
import ru.spbu.astro.delaunay.NativeDelaunayGraphBuilder;
import ru.spbu.astro.model.*;

import java.io.*;
import java.util.*;

public class VorTreeBuilder extends AbstractVorTreeBuilder {

    public VorTreeBuilder(Iterable<Point> points, int m) {
        super(points, m);
    }

    public VorTreeBuilder(Collection<Integer> pointIds, int m) {
        super(pointIds, m);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds) {
        return new VorTree(pointIds);
    }

    public class VorTree extends AbstractVorTree implements Serializable {

        public VorTree() {
            this(new ArrayList());
        }

        public VorTree(Collection<Integer> pointIds) {
            super(pointIds);

            rTree = new RTree(pointIds);

            if (pointIds.size() <= dim) {
                borderVertices.addAll(pointIds);
                return;
            }

            Map<Integer, Integer> pointId2pivotId = new HashMap();
            if (pointIds.size() > m) {
                ArrayList<Integer> pointIdList = new ArrayList(pointIds);
                Collections.shuffle(pointIdList);
                List<Integer> pivotIds = pointIdList.subList(0, Math.min(m, pointIdList.size()));

                VorTree pivotVorTree = (VorTree) build(pivotIds);
                for (int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pivotVorTree.getNearestNeighbor(id2point.get(pointId)));
                }
            } else {
                for (int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pointId);
                }
            }

            HashMap<Integer, ArrayList<Integer>> pivotId2pointIds = new HashMap();
            for (int pointId : pointId2pivotId.keySet()) {
                int pivotId = pointId2pivotId.get(pointId);
                if (!pivotId2pointIds.containsKey(pivotId)) {
                    pivotId2pointIds.put(pivotId, new ArrayList());
                }
                pivotId2pointIds.get(pivotId).add(pointId);
            }

            Collection<ArrayList<Integer>> cells = pivotId2pointIds.values();

            for (List<Integer> cell : cells) {
                VorTree vorTree = (VorTree) build(cell);
                sons.add(vorTree);
                rTree.sons.add(vorTree.rTree);
            }

            Collection<Integer> bindPointIds = new HashSet();
            Graph removedGraph = new Graph();
            for (AbstractVorTree t : sons) {
                bindPointIds.addAll(t.getBorderVertices());
                removedGraph.addGraph(removeCreepSimplexes(t));
                addTriangulation(t);
            }
            bindPointIds.addAll(removedGraph.getVertices());

            AbstractDelaunayGraph bindDelanayGraph;
            if (bindPointIds.size() != pointIds.size()) {
                bindDelanayGraph = build(bindPointIds);
            } else {
                bindDelanayGraph = binder.build(bindPointIds);
            }

            borderVertices = new ArrayList(bindDelanayGraph.getBorderVertices());

            Graph newEdges = new Graph();
            for (Edge edge : bindDelanayGraph) {
                int u = edge.getFirst();
                int v = edge.getSecond();
                if (!pointId2pivotId.get(u).equals(pointId2pivotId.get(v))) {
                    addEdge(u, v);
                    newEdges.addEdge(u, v);
                } else if (removedGraph.containsEdge(u, v)) {
                    addEdge(u, v);
                }
            }

            for (Simplex simplex : bindDelanayGraph.getSimplexes()) {
                if (containsGraph(simplex.toGraph())) {
                    for (Edge edge : newEdges) {
                        if (simplex.toGraph().containsEdge(edge)) {
                            addSimplex(simplex);
                            break;
                        }
                    }
                }
            }
        }


        /*@Override
        public void write(DataOutput dataOutput) throws IOException {
            //System.out.println(SerializationUtils.serialize(this));
            //dataOutput.write(SerializationUtils.serialize(this));
            dataOutput.writeChars("!!!!");
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            AbstractVorTree t = (AbstractVorTree) SerializationUtils.deserialize((DataInputStream) dataInput);
            neighbors = (HashMap) t.neighbors.clone();
            simplexes = (HashSet) t.simplexes.clone();
            side2simplexes = (HashMap) t.simplexes.clone();
            borderVertices = (ArrayList) t.borderVertices.clone();
            rTree = (RTree) t.rTree.clone();
            sons.clear();
            for (AbstractVorTree son : t.sons) {
                sons.add(new AbstractVorTree(son));
            }
        }         */


    }
}

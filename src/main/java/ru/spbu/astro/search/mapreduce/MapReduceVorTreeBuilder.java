package ru.spbu.astro.search.mapreduce;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import ru.spbu.astro.Schema.msg;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.search.VorTreeBuilder.VorTree;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MapReduceVorTreeBuilder extends AbstractVorTreeBuilder {

    public MapReduceVorTreeBuilder(Iterable<Point> points, int m) {
        super(points, m);
    }

    public MapReduceVorTreeBuilder(Collection<Integer> pointIds, int m) {
        super(pointIds, m);
    }

    @Override
    public AbstractDelaunayGraph build(Collection<Integer> pointIds) {
        return new MapReduceVorTree(pointIds);
    }

    public class MapReduceVorTree extends AbstractVorTree {

        public MapReduceVorTree(Collection<Integer> pointIds) {
            super(pointIds);

            rTree = new RTree(pointIds);

            if (pointIds.size() <= dim) {
                borderVertices.addAll(pointIds);
                return;
            }

            final Map<Integer, Integer> pointId2pivotId = new HashMap<>();
            if (pointIds.size() > m) {
                final ArrayList<Integer> pointIdList = new ArrayList<Integer>(pointIds);
                Collections.shuffle(pointIdList);
                final List<Integer> pivotIds = pointIdList.subList(0, Math.min(m, pointIdList.size()));

                final MapReduceVorTree pivotMapReduceVorTree = (MapReduceVorTree) build(pivotIds);
                for (final int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pivotMapReduceVorTree.getNearestNeighbor(id2point.get(pointId)));
                }
            } else {
                for (final int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pointId);
                }
            }

            final HashMap<Integer, List<Integer>> pivotId2pointIds = new HashMap<Integer,List<Integer>>();
            for (final int pointId : pointId2pivotId.keySet()) {
                final int pivotId = pointId2pivotId.get(pointId);
                if (!pivotId2pointIds.containsKey(pivotId)) {
                    pivotId2pointIds.put(pivotId, new ArrayList<Integer>());
                }
                pivotId2pointIds.get(pivotId).add(pointId);
            }

            final Collection<List<Integer>> cells = pivotId2pointIds.values();

            try {
                processMapReduce(cells);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }

            final Collection<Integer> bindPointIds = new HashSet<Integer>();
            final Graph removedGraph = new Graph();
            for (final AbstractVorTree t : sons) {
                bindPointIds.addAll(t.getBorderVertices());
                removedGraph.addGraph(removeCreepSimplexes(t));
                addTriangulation(t);
            }
            bindPointIds.addAll(removedGraph.getVertices());

            final AbstractDelaunayGraph bindDelanayGraph;
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

        public void processMapReduce(Collection<List<Integer>> cells) throws Exception {

            PrintWriter fout = new PrintWriter(new FileOutputStream("input.txt"));
            for (List<Integer> cell : cells) {
                for (int pointId : cell) {
                    fout.print(pointId + " ");
                }
                fout.println();
            }
            fout.flush();

            Configuration configuration = new Configuration();

            Job job = new Job(configuration);

            job.setJarByClass(MapReduceVorTreeBuilder.class);
            job.setMapperClass(DelaunayMapper.class);
            job.setNumReduceTasks(0);

            job.setInputFormatClass(TextInputFormat.class);

            job.setMapOutputKeyClass(NullWritable.class);
            job.setMapOutputValueClass(BytesWritable.class);

            FileUtils.deleteDirectory(new File("output"));
            FileInputFormat.addInputPath(job, new Path("input.txt"));
            FileOutputFormat.setOutputPath(job, new Path("output"));

            job.waitForCompletion(true);

            DataInputStream fin = new DataInputStream(new FileInputStream("output/part-m-00000"));
            final FileInputStream fis = new FileInputStream("");
            for (int i = 0; i < m; ++i) {
                final VorTree v = VorTree.fromMessage(msg.parseDelimitedFrom(fis));
                AbstractDelaunayGraph t = (AbstractDelaunayGraph) SerializationUtils.deserialize(fin);

                System.out.println("deserialized: " + t);
                //sons.add(t);
                //rTree.sons.add(t.rTree);
            }
        }
    }
}

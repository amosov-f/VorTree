package ru.spbu.astro.search.mapreduce;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.search.VorTreeBuilder;

import java.io.*;
import java.util.*;

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

            Map<Integer, Integer> pointId2pivotId = new HashMap();
            if (pointIds.size() > m) {
                ArrayList<Integer> pointIdList = new ArrayList(pointIds);
                Collections.shuffle(pointIdList);
                List<Integer> pivotIds = pointIdList.subList(0, Math.min(m, pointIdList.size()));

                MapReduceVorTree pivotMapReduceVorTree = (MapReduceVorTree) build(pivotIds);
                for (int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pivotMapReduceVorTree.getNearestNeighbor(id2point.get(pointId)));
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

            try {
                processMapReduce(cells);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
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

        public void processMapReduce(Collection<ArrayList<Integer>> cells) throws Exception {

            PrintWriter fout = new PrintWriter(new FileOutputStream("input.txt"));
            for (ArrayList<Integer> cell : cells) {
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
            for (int i = 0; i < m; ++i) {
                AbstractDelaunayGraph t = (AbstractDelaunayGraph) SerializationUtils.deserialize(fin);

                System.out.println("deserialized: " + t);
                //sons.add(t);
                //rTree.sons.add(t.rTree);
            }
        }
    }
}

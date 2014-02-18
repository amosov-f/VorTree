package ru.spbu.astro.search;

import com.google.common.base.Joiner;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.json.JSONObject;
import ru.spbu.astro.Message;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.search.mapreduce.DelaunayMapper;

import java.io.*;
import java.util.*;

public final class MapReduceVorTreeBuilder extends AbstractVorTreeBuilder {

    private static int fileNumber = 0;

    public MapReduceVorTreeBuilder(final Collection<Point> points) {
        super(points);

        try {
            FileUtils.deleteDirectory(new File("clipboard/input"));
            FileUtils.deleteDirectory(new File("clipboard/output"));
            new File("clipboard/input").mkdirs();
            new File("clipboard/output").mkdirs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MapReduceVorTreeBuilder(final Map<Integer, Point> id2point) {
        super(id2point);
    }

    @Override
    public MapReduceVorTree build(final Collection<Integer> pointIds, int division) {
        return new MapReduceVorTree(pointIds, division);
    }

    public class MapReduceVorTree extends AbstractVorTree {

        public MapReduceVorTree(final Collection<Integer> pointIds, int division) {
            super(pointIds);

            if (pointIds.size() <= dim()) {
                return;
            }

            final Map<Integer, Integer> pointId2pivotId = new HashMap<>();
            if (pointIds.size() > division) {
                final List<Integer> pointIdList = new ArrayList<>(pointIds);
                Collections.shuffle(pointIdList);
                final List<Integer> pivotIds = pointIdList.subList(0, Math.min(division, pointIdList.size()));

                final AbstractVorTree pivotVorTree = build(pivotIds, 2);
                for (int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pivotVorTree.getNearestNeighbor(id2point.get(pointId)));
                }
            } else {
                for (int pointId : pointIds) {
                    pointId2pivotId.put(pointId, pointId);
                }
            }

            final Map<Integer, List<Integer>> pivotId2pointIds = new HashMap<>();
            for (int pointId : pointId2pivotId.keySet()) {
                int pivotId = pointId2pivotId.get(pointId);
                if (!pivotId2pointIds.containsKey(pivotId)) {
                    pivotId2pointIds.put(pivotId, new ArrayList<Integer>());
                }
                pivotId2pointIds.get(pivotId).add(pointId);
            }

            final Collection<List<Integer>> cells = pivotId2pointIds.values();

            List<AbstractVorTree> sons = new ArrayList<>();
            try {
                sons = processMapReduce(cells);
            } catch (Exception e) {
                e.printStackTrace();
            }

            final Set<Integer> bindPointIds = new HashSet<>();
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

            final Graph newEdges = new Graph();
            for (final Edge edge : bindDelanayGraph) {
                int u = edge.getFirst();
                int v = edge.getSecond();
                if (!pointId2pivotId.get(u).equals(pointId2pivotId.get(v))) {
                    addEdge(u, v);
                    newEdges.addEdge(u, v);
                } else if (removedGraph.containsEdge(u, v)) {
                    addEdge(u, v);
                }
            }

            for (final Simplex simplex : bindDelanayGraph.getSimplexes()) {
                if (containsGraph(simplex.toGraph())) {
                    for (final Edge edge : newEdges) {
                        if (simplex.toGraph().containsEdge(edge)) {
                            addSimplex(simplex);
                            break;
                        }
                    }
                }
            }
        }

        public List<AbstractVorTree> processMapReduce(final Collection<List<Integer>> cells) throws Exception {
            ++fileNumber;
            int currentFileNumber = fileNumber;

            PrintWriter fout = new PrintWriter(new FileOutputStream("clipboard/input/" + currentFileNumber));
            for (final List<Integer> cell : cells) {
                fout.println(Joiner.on(' ').join(cell));
            }
            fout.flush();

            final Configuration conf = new Configuration();

            final JSONObject json = new JSONObject();
            json.put("id2point", id2point);
            conf.set("id2point", json.toString());

            final Job job = new Job(conf);

            job.setJarByClass(MapReduceVorTreeBuilder.class);
            job.setMapperClass(DelaunayMapper.class);
            job.setNumReduceTasks(0);

            job.setInputFormatClass(TextInputFormat.class);
            job.setOutputFormatClass(SequenceFileOutputFormat.class);

            job.setOutputKeyClass(IntWritable.class);
            job.setOutputValueClass(BytesWritable.class);

            FileInputFormat.addInputPath(job, new Path("clipboard/input/" + currentFileNumber));
            FileOutputFormat.setOutputPath(job, new Path("clipboard/output/" + currentFileNumber));

            job.waitForCompletion(true);

            final SequenceFile.Reader reader = new SequenceFile.Reader(
                    FileSystem.get(conf),
                    new Path("clipboard/output/" + currentFileNumber + "/part-m-00000"),
                    conf
            );

            final IntWritable key = new IntWritable();
            final BytesWritable value = (BytesWritable) reader.getValueClass().newInstance();

            final List<AbstractVorTree> sons = new ArrayList<>();
            while (reader.next(key, value)) {
                byte[] b = Arrays.copyOf(value.getBytes(), key.get());
                final Message.AbstractVorTree message = Message.AbstractVorTree.parseFrom(b);

                final AbstractVorTree t = build(message);
                sons.add(t);
                addSon(t);
            }

            return sons;
        }

    }

}

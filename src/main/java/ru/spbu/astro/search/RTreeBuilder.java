package ru.spbu.astro.search;

import com.google.common.base.Joiner;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import ru.spbu.astro.other.RTreeVisualizerEngine;
import ru.spbu.astro.db.PointDepot;
import ru.spbu.astro.search.mapreduce.PointMapper;
import ru.spbu.astro.search.mapreduce.PointReducer;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.model.Rectangle;


import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

@Deprecated
public class RTreeBuilder {
    private VorTree vorTree;

    private PointDepot pointDepot;

    private final static int MIN_SONS = 3;
    private final static int MAX_SONS = 5;

    public void build(List<Point> points) throws Exception {
        pointDepot.clear();

        List<Integer> ids = new ArrayList();
        for (Point p : points) {
            ids.add(pointDepot.add(p));
        }

        System.out.println("Building started!");

        this.vorTree = new VorTree(ids);
    }

    private class VorTree {
        private Node root;
        private List<Integer> pointIds = new ArrayList();

        public VorTree(List<Integer> pointIds) throws Exception {

            this.pointIds = pointIds;

            root = new Node(new Rectangle(pointDepot.get(pointIds).values()));

            if (pointIds.size() == 1) {
                return;
            }

            int k = Math.min((MIN_SONS + MAX_SONS) / 2, pointIds.size());

            Collections.shuffle(pointIds);
            List<Integer> pivotIds = pointIds.subList(0, k);

            List<List<Integer>> groups = processMapReduce(pointIds, pivotIds);

            for (List group : groups) {
                root.add(new VorTree(group).root);
            }
        }

        private List<List<Integer>> processMapReduce(List<Integer> pointIds, List<Integer> pivotIds) throws Exception {
            PrintWriter fout = new PrintWriter(new FileOutputStream("input.txt"));
            for (Integer id : pointIds) {
                fout.println(id);
            }
            fout.flush();

            Configuration configuration = new Configuration();
            configuration.set("pivotIds", Joiner.on(' ').join(pivotIds));

            Job job = new Job(configuration);

            job.setJarByClass(RTreeVisualizerEngine.class);
            job.setMapperClass(PointMapper.class);
            job.setReducerClass(PointReducer.class);

            job.setInputFormatClass(TextInputFormat.class);

            job.setMapOutputKeyClass(IntWritable.class);
            job.setMapOutputValueClass(IntWritable.class);
            job.setOutputKeyClass(NullWritable.class);
            job.setOutputValueClass(Text.class);

            FileUtils.deleteDirectory(new File("output"));
            FileInputFormat.addInputPath(job, new Path("input.txt"));
            FileOutputFormat.setOutputPath(job, new Path("output"));

            job.waitForCompletion(true);

            Scanner fin = new Scanner(new FileInputStream("output/part-r-00000"));
            List<List<Integer>> groups = new ArrayList();
            while (fin.hasNextLine()) {
                List<Integer> group = new ArrayList();
                for (String s : fin.nextLine().split("\\s+")) {
                    group.add(Integer.valueOf(s));
                }
                groups.add(group);
            }

            return groups;
        }

        @Override
        public String toString() {
            return toString(root, 0);
        }

        public String toString(Node u, int tab) {
            String result = tab(tab) + u.toString();
            if (!u.getSons().isEmpty()) {
                result += " {\n";
            }
            for (Node v : u.getSons()) {
                result += toString(v, tab + 4);
            }
            if (!u.getSons().isEmpty()) {
                result += tab(tab) + "}";
            }
            result += "\n";
            return result;
        }

        private class Node {
            private Rectangle cover;

            private List<Node> sons = new ArrayList();

            public Node(Rectangle cover) {
                this.cover = cover;
            }

            public void add(Node son) {
                sons.add(son);
            }

            public List<Node> getSons() {
                return sons;
            }

            public ru.spbu.astro.model.Rectangle getCover() {
                return cover;
            }

            @Override
            public String toString() {
                return cover.toString();
            }
        }

        public Component getComponent(final int width, final int height) {
            return new Component() {
                private int ALIGN = 10;

                @Override
                public void paint(Graphics g) {
                    setSize(width, height);
                    setBounds(0, 0, width, height);

                    paint(g, root, 0);

                    g.setColor(new Color(0, 0, 0));
                    for (Point p : pointDepot.get(pointIds).values()) {
                        g.fillOval((int)translate(p).getX() - 2, (int)translate(p).getY() - 2, 4, 4);
                    }
                }

                private Point2D.Double translate(ru.spbu.astro.model.Point p) {
                    return new Point2D.Double(
                            (p.getX() - root.getCover().getX()) / root.getCover().getWidth() * (width - 2 * ALIGN) + ALIGN,
                            (p.getY() - root.getCover().getY()) / root.getCover().getHeight() * (height - 2 * ALIGN) + ALIGN
                    );
                }

                private void paint(Graphics g, Node u, int level) {
                    g.setColor(color(level));
                    ((Graphics2D)g).setStroke(new BasicStroke(Math.max(6 - level, 1)));

                    Point2D.Double minVertex = translate(u.getCover().getMinVertex());
                    Point2D.Double maxVertex = translate(u.getCover().getMaxVertex());

                    g.drawRect(
                            (int)minVertex.getX(),
                            (int)minVertex.getY(),
                            (int)(maxVertex.getX() - minVertex.getX()),
                            (int)(maxVertex.getY() - minVertex.getY())
                    );

                    for (Node v : u.getSons()) {
                        paint(g, v, level + 1);
                    }
                }

                private Color color(int level) {
                    return new Color((131 * level + 100) % 256, (241 * level + 200) % 256, (271 * level + 100) % 256);
                }
            };
        }
    }

    private static String tab(int size) {
        String result = "";
        for (int i = 0; i < size; ++i) {
            result += " ";
        }
        return result;
    }

    public Component getComponent(final int width, final int height) {
        return vorTree.getComponent(width, height);
    }


    public void setPointDepot(PointDepot pointDepot) {
        this.pointDepot = pointDepot;
    }
}

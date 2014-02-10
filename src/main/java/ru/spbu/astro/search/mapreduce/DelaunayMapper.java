package ru.spbu.astro.search.mapreduce;

import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.serializer.JavaSerialization;
import org.apache.hadoop.mapreduce.Mapper;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.spbu.astro.db.PointDepot;
import ru.spbu.astro.delaunay.AbstractDelaunayGraphBuilder;
import ru.spbu.astro.delaunay.VisadDelaunayGraphBuilder;
import ru.spbu.astro.model.Graph;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.search.VorTreeBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DelaunayMapper extends Mapper<LongWritable, Text, NullWritable, BytesWritable> {
    //private final PointDepot id2point;

    //public DelaunayMapper() {
    //    id2point = (PointDepot) new ClassPathXmlApplicationContext("application-context.xml").getBean("pointDepot");
    //}

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        ArrayList<Integer> pointIds = new ArrayList();
        for (String s : value.toString().split("\\s+")) {
            pointIds.add(Integer.valueOf(s));
        }

        System.out.println(pointIds);

        //VisadDelaunayGraphBuilder builder = new VisadDelaunayGraphBuilder(pointIds);
        //VisadDelaunayGraphBuilder.VisadDelaunayGraph t = (VisadDelaunayGraphBuilder.VisadDelaunayGraph) builder.build();


        VorTreeBuilder builder = new VorTreeBuilder(pointIds, 2);
        VorTreeBuilder.VorTree t = (VorTreeBuilder.VorTree) builder.build();

        Graph g = new Graph();
        g.addGraph(t);

        System.out.println(g);
        byte[] bytes = SerializationUtils.serialize(g);
        System.out.println("serialization completed");

        //System.out.println(Arrays.toString(bytes));

        context.write(NullWritable.get(), new BytesWritable(bytes));
    }
}

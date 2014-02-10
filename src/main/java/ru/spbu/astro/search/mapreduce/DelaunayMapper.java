package ru.spbu.astro.search.mapreduce;

import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import ru.spbu.astro.Schema.msg;
import ru.spbu.astro.search.VorTreeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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

        System.out.println(t);
        byte[] bytes = SerializationUtils.serialize(t);
        final msg b = t.toMessage();
        System.out.println("serialization completed");

        System.out.println(Arrays.toString(bytes));
        b.writeDelimitedTo();
        context.write(NullWritable.get(), new BytesWritable(b.toByteArray()));

        msg.parseFrom(bytes)
    }
}

package ru.spbu.astro.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.spbu.astro.db.SQLPointDepot;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.db.PointDepot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointMapper extends Mapper<LongWritable, Text, IntWritable, IntWritable> {

    PointDepot pointDepot;

    Map<Integer, Point> id2pivot = new HashMap();

    public PointMapper() {
        this.pointDepot = (PointDepot) new ClassPathXmlApplicationContext("application-context.xml").getBean("pointDepot");
    }

    @Override
    public void setup(Context context) throws IOException, InterruptedException {
        List<Integer>  pivotIds = new ArrayList();
        for (String id : context.getConfiguration().get("pivotIds").split("\\s+")) {
            pivotIds.add(Integer.valueOf(id));
        }

        this.id2pivot = pointDepot.get(pivotIds);
    }

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        int id = Integer.valueOf(value.toString());
        Point p = pointDepot.get(id);
        Integer closestId = null;
        for (Map.Entry<Integer, Point> pivotEntry : id2pivot.entrySet()) {
            Point s = pivotEntry.getValue();
            if (closestId == null) {
                closestId = pivotEntry.getKey();
            }
            if (p.distance2to(s) < p.distance2to(id2pivot.get(closestId))) {
                closestId = pivotEntry.getKey();
            }
        }

        context.write(new IntWritable(closestId), new IntWritable(id));
    }
}

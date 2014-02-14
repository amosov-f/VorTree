package ru.spbu.astro.search.mapreduce;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.search.VorTreeBuilder;

import java.io.IOException;
import java.util.*;

public class DelaunayMapper extends Mapper<LongWritable, Text, IntWritable, BytesWritable> {

    private final Map<Integer, Point> id2point = new HashMap<>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Map<String, String> idString2pointString = null;
        try {
            idString2pointString = new ObjectMapper().readValue(
                    new JSONObject(context.getConfiguration().get("id2point")).get("id2point").toString(),
                    new TypeReference<HashMap<String, String>>() {
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert idString2pointString != null;
        for (Map.Entry<String, String> entry : idString2pointString.entrySet()) {
            id2point.put(new Integer(entry.getKey()), new Point(entry.getValue()));
        }
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        final List<Integer> pointIds = new ArrayList<>();
        for (String s : value.toString().split("\\s+")) {
            pointIds.add(Integer.valueOf(s));
        }
        System.out.println("map: " + pointIds);

        VorTreeBuilder builder = new VorTreeBuilder(id2point, 2);
        VorTreeBuilder.VorTree t = (VorTreeBuilder.VorTree) builder.build(pointIds);

        byte[] b = t.toMessage().toByteArray();

        context.write(new IntWritable(b.length), new BytesWritable(b));
    }
}

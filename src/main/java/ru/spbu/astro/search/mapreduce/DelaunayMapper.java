package ru.spbu.astro.search.mapreduce;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import ru.spbu.astro.model.Point;
import ru.spbu.astro.search.AbstractVorTreeBuilder;
import ru.spbu.astro.search.MapReduceVorTreeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DelaunayMapper extends Mapper<LongWritable, Text, IntWritable, BytesWritable> {
    @NotNull
    private final Map<Integer, Point> id2point = new HashMap<>();

    @Override
    protected void setup(@NotNull final Context context) throws IOException, InterruptedException {
        final Map<String, String> idString2pointString;
        try {
            idString2pointString = new ObjectMapper().readValue(
                    new JSONObject(context.getConfiguration().get("id2point")).get("id2point").toString(),
                    new TypeReference<HashMap<String, String>>() {
                    }
            );
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        for (final Map.Entry<String, String> entry : idString2pointString.entrySet()) {
            id2point.put(new Integer(entry.getKey()), new Point(entry.getValue()));
        }
    }

    @Override
    protected void map(@NotNull final LongWritable key,
                       @NotNull final Text value,
                       @NotNull final Context context) throws IOException, InterruptedException {
        final List<Integer> pointIds = new ArrayList<>();
        for (final String s : value.toString().split("\\s+")) {
            pointIds.add(Integer.valueOf(s));
        }
        System.out.println("map: " + pointIds);

        final AbstractVorTreeBuilder builder = new MapReduceVorTreeBuilder(id2point);
        final AbstractVorTreeBuilder.AbstractVorTree t = builder.build(pointIds, 2);

        final byte[] b = t.toAbstractVorTreeMessage().toByteArray();

        context.write(new IntWritable(b.length), new BytesWritable(b));
    }
}

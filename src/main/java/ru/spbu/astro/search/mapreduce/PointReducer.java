package ru.spbu.astro.search.mapreduce;

import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class PointReducer extends Reducer<IntWritable, IntWritable, NullWritable, Text> {
    @Override
    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        /*IntWritable[] result = Iterables.toArray(values, IntWritable.class);
        for (int i = 0; i < result.length; ++i) {
            System.out.print(result[i] + " ");
        }
        System.out.println();   */

        System.out.println("reduce");
        String result = "";
        for (IntWritable value : values) {
            result += value + " ";
        }

        context.write(NullWritable.get(), new Text(result));
    }
}

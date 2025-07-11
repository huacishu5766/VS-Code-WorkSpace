package com.huacishu.FlinkStudy.day01;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @Auther:huacishu
 * @Date: 2025/7/11
 */
public class demo01_socket {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> stream = env.socketTextStream("hadoop101", 9898);

        SingleOutputStreamOperator<Tuple2<String, Integer>> flatMap = stream.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
            @Override
            public void flatMap(String s, Collector<Tuple2<String, Integer>> collector) throws Exception {
                String[] split = s.split(" ");

                for (String word : split) {
                    collector.collect(Tuple2.of(word, 1));
                }
            }
        });

        // 分组

        KeyedStream<Tuple2<String, Integer>, String> keyBy = flatMap.keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
            @Override
            public String getKey(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return stringIntegerTuple2.f0;
            }
        });

        // 聚合
        SingleOutputStreamOperator<Tuple2<String, Integer>> sum = keyBy.sum(1);

        SingleOutputStreamOperator<Tuple2<String, Integer>> f1 = keyBy.sum("f1");

        sum.print();
    }
}

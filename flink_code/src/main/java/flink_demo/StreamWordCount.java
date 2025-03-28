package flink_demo;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class StreamWordCount {
    public static void main(String[] args) throws Exception{
        // 1. 创建流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // 2. 读取文件
        DataStreamSource<String> lineStream = env.readTextFile("input/words.txt");
        
        // 3. 转换、分组、求和，得到统计结果
        SingleOutputStreamOperator<Tuple2<String, Long>> sum = 

        lineStream.flatMap(new FlatMapFunction<String,Tuple2<String,Long>>() {
            @Override
            public void flatMap(String line, Collector<Tuple2<String, Long>> out) throws Exception {
                //3.1 按照空格来切分
                String[] words = line.split(" ");
                //3.2 转换成二元组(word,1)
                for (String word : words) {
                    //3.3 通过采集器向下游发送数据
                    out.collect(Tuple2.of(word, 1L));
                }
            }
        }).keyBy(new KeySelector<Tuple2<String,Long>,String>() {
            @Override
            public String getKey(Tuple2<String, Long> value) throws Exception {
                return value.f0;
               }
            
        })    //.keyBy(data -> data.f0)
           .sum(1);

         // 4. 打印
         sum.print();
        
         // 5. 执行
         env.execute();
    }

}
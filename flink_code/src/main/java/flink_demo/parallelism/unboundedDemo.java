package flink_demo.parallelism;


import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class unboundedDemo {
    public static void main(String[] args) throws Exception {
       // 1. 创建流式执行环境
        //StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //withwebui在idea页面也有webui 一般用于本地测试
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
         //全局设置并行度
        env.setParallelism(3);

          //2. 读取数据
        DataStreamSource<String> socketDS = env.socketTextStream("hadoop101", 7777);
        //3. 处理数据
       SingleOutputStreamOperator<Tuple2<String,Integer>> sum = socketDS.flatMap((String value, Collector<Tuple2<String ,Integer>> out) -> {
            
        String[] words = value.split(" ");
            for (String word : words) {
                out.collect(Tuple2.of(word,1));
            }
        })
               //.setParallelism(2)
        .returns(Types.TUPLE(Types.STRING,Types.INT))   //解决 泛型擦除问题
        .keyBy(data -> data.f0)
        .sum(1);

        //4. 打印数据
        sum.print();

        env.execute();
    }
}

/**
 * 并行度等级 ： 算子 > env > 命令行
 */


/**
 1、sLot特点：
 1)均分隔离内存，不隔离cpu
 2)可以共享
 同一个job中，不同算子的子任务才可以共享同一个sot,同时在运行的
 前提是，属于同一个sLot共享组，默认都是“default”
 2、sLot数量与并行度的关系
 1)sLot是一种静态的概念，表示最大的并发上限
 并行度是一种动态的概念，表示实际运行占用了几个
 2)要求：sLot数量>=job并行度（算子最大并行度），job才能运行
 */
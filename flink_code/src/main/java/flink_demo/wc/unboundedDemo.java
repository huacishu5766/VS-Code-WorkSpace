package flink_demo.wc;


import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class unboundedDemo {
    public static void main(String[] args) throws Exception {
         // 1. 创建流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //2. 读取数据
        DataStreamSource<String> socketDS = env.socketTextStream("hadoop101", 7777);
        //3. 处理数据
       SingleOutputStreamOperator<Tuple2<String,Integer>> sum = socketDS.flatMap((String value, Collector<Tuple2<String ,Integer>> out) -> {
            
        String[] words = value.split(" ");
            for (String word : words) {
                out.collect(Tuple2.of(word,1));
            }
        })
        .returns(Types.TUPLE(Types.STRING,Types.INT))   //解决 泛型擦除问题
        .keyBy(data -> data.f0)
        .sum(1);

        //4. 打印数据
        sum.print();

        env.execute();
    }
}

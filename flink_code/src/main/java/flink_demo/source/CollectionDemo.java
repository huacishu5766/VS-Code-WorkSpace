package flink_demo.source;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;

/**
 * @Auther:huacishu
 * @Date: 2025/3/28
 */
public class CollectionDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //从集合中读数据
        DataStreamSource<Integer> source = env.fromCollection(Arrays.asList(1, 22, 33, 44, 55));
        //直接写元素
        DataStreamSource<Integer> source1 = env.fromElements(1, 223, 3);

        source.print();


        env.execute();
    }
}

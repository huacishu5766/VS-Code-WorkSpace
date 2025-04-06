package flink_demo.function.partition;

import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Auther:huacishu
 * @Date: 2025/4/6
 */
public class PartitionCustomDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        DataStreamSource<String> socketDS = env.socketTextStream("hadoop101", 7777);

        //socketDS.partitionCustom(new MyPartitioner(), new KeySelector<String, String>() {
        //            @Override
        //            public String getKey(String key) throws Exception {
        //                return key;
        //            }
        //        }).print();
        socketDS.partitionCustom(new MyPartitioner(),key ->key)
                        .print();
        env.execute();
    }
}

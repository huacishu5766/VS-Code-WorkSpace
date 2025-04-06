package flink_demo.split;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Auther:huacishu
 * @Date: 2025/4/6
 *
 * 分流： 奇偶数拆分成不同的流
 */
public class FilterDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        DataStreamSource<String> socketDS = env.socketTextStream("hadoop101", 7777);

        //使用Filter来实现  缺点同一份数据要被处理两遍
        SingleOutputStreamOperator<String> filter1 = socketDS.filter(value -> Integer.parseInt(value) % 2 == 0);
        filter1.print("偶数");

        SingleOutputStreamOperator<String> filter2 = socketDS.filter(value -> Integer.parseInt(value) % 2 != 0);
        filter2.print("奇数");


        env.execute();
    }
}
package flink_demo.function.partition;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Auther:huacishu
 * @Date: 2025/4/6
 */
public class PartitionDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        DataStreamSource<String> socketDS = env.socketTextStream("hadoop101", 7777);

        //shuffle 随机分区   random.nextInt(下游算子并行度)
        //socketDS.shuffle().print();

        //rebalance 轮询： nextChannelToSendTo = (nextChannelToSendTo + 1) % 下游算子并行度;
        // 如果是数据源倾斜的场景，Source读进来后，调用rebalance 就可以解决
        //socketDS.rebalance().print();

        //rescale缩放：实现轮询，局部组队，比rebalence高效
        //socketDS.rescale().print();

        //broadcast 广播：将数据发送给下游所有子任务
        //socketDS.broadcast().print();

        //global 全局分区：将所有数据发送给下游第一个子任务
        //return 0;
        //socketDS.global().print();

        //keyby分区
        //one-to-one Forward分区器
        env.execute();



    }
}

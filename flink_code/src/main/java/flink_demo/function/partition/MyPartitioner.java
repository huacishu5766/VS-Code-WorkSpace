package flink_demo.function.partition;

import org.apache.flink.api.common.functions.Partitioner;

/**
 * @Auther:huacishu
 * @Date: 2025/4/6
 */
public class MyPartitioner implements Partitioner<String> {
    @Override
    public int partition(String key, int numPartitions) {
        return Integer.parseInt(key) % numPartitions;
    }
}

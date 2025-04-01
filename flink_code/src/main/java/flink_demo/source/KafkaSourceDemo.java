package flink_demo.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.stream.Stream;

/**
 * @Auther:huacishu
 * @Date: 2025/3/31
 */
public class KafkaSourceDemo {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        KafkaSource<String> kafkaSource = KafkaSource
                .<String>builder()
                .setBootstrapServers("hadoop101:9092,hadoop102:9092,hadoop103:9092,")
                .setGroupId("flink")
                .setTopics("flink-topic")
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setStartingOffsets(OffsetsInitializer.earliest())
                .build();

        env
                .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(),"kafka-source")
                .print();
    }
}

package flink_demo.sink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.producer.ProducerConfig;


/**
 * @Auther:huacishu
 * @Date: 2025/4/8
 */
public class SinkKafka {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        //如果是精准一次，必须开启checkpoint
        env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);


        SingleOutputStreamOperator<String> sensorDS = env.socketTextStream("hadoop102", 7777);
                //.map(new WaterSensorMapFunction());
/**
 *  kafka Sink
 *  注意：如果要使用 精准一次写入到Kafka 需满足以下条件，缺一不可
 *  1、开启checkpoint
 *  2、设置事务前缀
 *  3、设置事务超时时间  checkpoint间隔 < 事务超时时间  < max 的15分钟
 *
 */


        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                //指定kafka 的地址和端口
                .setBootstrapServers("")
                //指定序列化器：指定topic名称，具体的序列化器
                .setRecordSerializer(KafkaRecordSerializationSchema.<String>builder()
                        .setTopic("sink_topic")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                //写到kafka的一致性级别 ： 精准一次，至少一次
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                //如果是精准一次，必须设置事务的前缀
                .setTransactionalIdPrefix("huacishu") //设置事务id的前缀
                //如果是精准一次，必须设置 事务超时时间：大于checkpoint间隔 小于Max15分钟
                .setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG,10*60*1000+"")
                .build();


        env.execute();
    }
}

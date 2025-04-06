package flink_demo.aggreagte;

import flink_demo.bean.WaterSensor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Auther:huacishu
 * @Date: 2025/4/1
 */
public class KeybyDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("sensor_1", 1000L, 10),
                new WaterSensor("sensor_2", 2000L, 20),
                new WaterSensor("sensor_1", 3000L, 30),
                new WaterSensor("sensor_2", 4000L, 40)
        );
        //按照keyby来分组
        /**
         * keyby 按照id分组
         * 1、 返回的是一个KeyedStream 键控流
         * 2、 keyby不是转换算子，只是对数据进行重分区，不能设置并行度
         * 3、 keyby 分组 和 分区的关系
         *      1） keyby是对数据分组，保证相同key的数据在同一个分区  一个分区中可以存在多个分组 多个key
         *      2） 分区： 一个子任务 理解为一个分区
         *
         */

        KeyedStream<WaterSensor, String> keyBy = sensorDS.keyBy(new KeySelector<WaterSensor, String>() {
            @Override
            public String getKey(WaterSensor value) throws Exception {
                return value.getId();
            }
        });


        keyBy.print();

        env.execute();

    }
}

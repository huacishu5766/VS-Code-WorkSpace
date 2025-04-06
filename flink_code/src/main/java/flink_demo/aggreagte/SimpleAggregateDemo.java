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
public class SimpleAggregateDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("sensor_1", 1000L, 10),
                new WaterSensor("sensor_2", 2000L, 20),
                new WaterSensor("sensor_1", 3000L, 30),
                new WaterSensor("sensor_3", 4000L, 40)
        );


        KeyedStream<WaterSensor, String> keyBy = sensorDS.keyBy(new KeySelector<WaterSensor, String>() {
            @Override
            public String getKey(WaterSensor value) throws Exception {
                return value.getId();
            }
        });

        /**
         * 简单聚合算子
         * 1、在keyby之后才可以使用
         * 2、分组内的聚合，对同一个key的数据进行聚合
         */

        //传位置索引的适用于Tuple类型，pojo不行
        //SingleOutputStreamOperator<WaterSensor> result = keyBy.sum("vc");

        /**
         * min/max/ 和 minby/maxby
         * min/max/: 只会取比较字段的最大值，非比较字段保留第一次的值
         * minby/maxby: 会取比较字段的最大值，比较字段保留最大的值
         */
        //SingleOutputStreamOperator<WaterSensor> result = keyBy.min("vc");
        SingleOutputStreamOperator<WaterSensor> result = keyBy.minBy("vc");


        result.print();

        env.execute();

    }
}

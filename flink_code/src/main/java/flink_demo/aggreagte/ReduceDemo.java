package flink_demo.aggreagte;

import flink_demo.bean.WaterSensor;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Auther:huacishu
 * @Date: 2025/4/1
 */
public class ReduceDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("sensor_1", 1000L, 10),
                new WaterSensor("sensor_1", 1000L, 20),
                new WaterSensor("sensor_1", 1000L, 30),
                new WaterSensor("sensor_2", 2000L, 20),
                new WaterSensor("sensor_1", 3000L, 40),
                new WaterSensor("sensor_2", 4000L, 40)
        );

        KeyedStream<WaterSensor, String> keyBy = sensorDS.keyBy(new KeySelector<WaterSensor, String>() {
            @Override
            public String getKey(WaterSensor value) throws Exception {
                return value.getId();
            }
        });

        /**
         * reduce：
         * 1、keyby之后调用
         * 2、输入类型 = 输出类型，类型不能变
         * 3、每个key的第一次来的时候 不会进入reduce方法，存起来，直接输出
         * 4、reduce方法中的两个参数
         *      value1 之前的计算结果，存状态
         *      value2 现在来得数据
         */

        SingleOutputStreamOperator<WaterSensor> result = keyBy.reduce(new ReduceFunction<WaterSensor>() {
            @Override
            public WaterSensor reduce(WaterSensor value1, WaterSensor value2) throws Exception {
                System.out.println("value1 = " + value1);
                System.out.println("value2 = " + value2);
                return new WaterSensor(value1.id, value2.ts, value1.vc + value2.vc);
            }
        });

        result.print();

        env.execute();

    }
}

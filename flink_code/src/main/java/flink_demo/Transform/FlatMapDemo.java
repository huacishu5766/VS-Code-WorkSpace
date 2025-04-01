package flink_demo.Transform;

import flink_demo.bean.WaterSensor;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @Auther:huacishu
 * @Date: 2025/4/1
 */
public class FlatMapDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("sensor_1", 1000L, 10),
                new WaterSensor("sensor_1", 2000L, 20),
                new WaterSensor("sensor_2", 3000L, 30),
                new WaterSensor("sensor_3", 4000L, 40)
        );
        //TODO flatmap:  一进多出  1对1 1对多 1对0
        /**
         * map 和 FlatMap
         * flatMap： 通过Collector 调用几次输出几次
         */
        SingleOutputStreamOperator<String> flatMap = sensorDS.flatMap(new FlatMapFunction<WaterSensor, String>() {
            @Override
            public void flatMap(WaterSensor value, Collector<String> out) throws Exception {
                if ("sensor_1".equals(value.getId())) {
                    out.collect(value.getVc() + "");
                } else if ("sensor_2".equals(value.getId())) {
                    out.collect(value.getTs().toString()); //转字符串的两种写法
                    out.collect(String.valueOf(value.getVc()));
                }
            }
        });

        flatMap.print();

        env.execute();

    }
}

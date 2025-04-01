package flink_demo.Transform;

import flink_demo.bean.WaterSensor;
import flink_demo.function.MapFunctionImpl;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Auther:huacishu
 * @Date: 2025/4/1
 */
public class FilterDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("sensor_1", 1000L, 10),
                new WaterSensor("sensor_2", 2000L, 20),
                new WaterSensor("sensor_3", 3000L, 30),
                new WaterSensor("sensor_4", 4000L, 40)
        );
        //TODO filter: true b保留 false 过滤掉
        //SingleOutputStreamOperator<WaterSensor> filter = sensorDS.filter(new FilterFunction<WaterSensor>() {
        //    @Override
        //    public boolean filter(WaterSensor waterSensor) throws Exception {
        //        return "sensor_1".equals(waterSensor.getId());
        //    }
        //});

        SingleOutputStreamOperator<WaterSensor> filter = sensorDS.filter(waterSensor -> "sensor_1".equals(waterSensor.getId()));


        filter.print();

        env.execute();

    }
}

package flink_demo.Transform;

import flink_demo.bean.WaterSensor;
import flink_demo.function.MapFunctionImpl;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.common.protocol.types.Field;

/**
 * @Auther:huacishu
 * @Date: 2025/4/1
 */
public class MapDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<WaterSensor> sensorDS = env.fromElements(
                new WaterSensor("sensor_1", 1000L, 10),
                new WaterSensor("sensor_2", 2000L, 20),
                new WaterSensor("sensor_3", 3000L, 30)
        );
        //TODO map算子： 一进一出
        //实现方式 1 匿名实现类
        //SingleOutputStreamOperator<String> map = sensorDS.map(new MapFunction<WaterSensor, String>() {
        //    @Override
        //    public String map(WaterSensor value) throws Exception {
        //        return value.getId();
        //    }
        //});

        //实现方式 2 lambda表达式
        //SingleOutputStreamOperator<String> map = sensorDS.map(sensor -> sensor.getId());

        //实现方式 3 实现类
        SingleOutputStreamOperator<String> map = sensorDS.map(new MapFunctionImpl());
       // SingleOutputStreamOperator<String> map = sensorDS.map(new MyMap());


        map.print();
        env.execute();

    }

    //内部类
    //public static class MyMap implements MapFunction<WaterSensor, String>{
    //
    //    @Override
    //    public String map(WaterSensor value) throws Exception {
    //        return value.getId();
    //    }
    //}
}

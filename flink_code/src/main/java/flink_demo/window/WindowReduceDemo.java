package flink_demo.window;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * @Auther:huacishu
 * @Date: 2025/7/23
 */
public class WindowReduceDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> sensorDS = env
                .socketTextStream("hadoop101", 7777)
                .map(new WaterSensorMapFunction());

        KeyedStream<WaterSensor, String> keyedStream = sensorDS
                .keyBy(sensor -> sensor.getId());

        // 1. 窗口分配器
        WindowedStream<WaterSensor, String, TimeWindow> sensorWS = keyedStream.window(TumblingProcessingTimeWindows.of(Time.seconds(10)));

        // 2. 窗口函数 增量聚合
        /**
         * 窗口的reduce：
         * 1、想通的key 的第一条数据来的时候，不会调用reduce方法
         * 2、增量聚合：来一条数据，计算一次，不会输出
         * 3、在窗口触发的时候，才会输出最终改的结果
         */
        SingleOutputStreamOperator<WaterSensor> reduce = sensorWS
                .reduce(new ReduceFunction<WaterSensor>() {
                    @Override
                    public WaterSensor reduce(WaterSensor value1, WaterSensor value2) throws Exception {
                        System.out.println("调用reduce方法，value1 = " + value1);
                        return new WaterSensor(value1.getId(), value2.getTs(), value1.getVc() + value2.getVc());
                    }
                }
                );

        reduce.print();

        env.execute();
    }
}

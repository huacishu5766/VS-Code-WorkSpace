package flink_demo.window;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

/**
 * @Auther:huacishu
 * @Date: 2025/7/23
 */
public class WindowApiDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> sensorDS = env
                .socketTextStream("hadoop101", 7777)
                .map(new WaterSensorMapFunction());


        KeyedStream<WaterSensor, String> keyedStream = sensorDS
                .keyBy(sensor -> sensor.getId());

        //TODO 1.指定窗口分配器：指定使用哪一种窗口 --> 时间 or 计数 ？ 滚动、滑动、会话
        // 1.1 没有keyby 的窗口 : 窗口内的所有数据 进入同一个 子任务，并行度 只能为1
        //sensorDS.windowAll();
        // 1.2 有keyby的窗口 ： 没个key上都定义了一组窗口 并独立自主的进行统计计算
        // 基于时间的
        ////1.2.1 TumblingProcessingTimeWindows 滚动
        //keyedStream.window(TumblingProcessingTimeWindows.of(Time.seconds(10))); //滚动窗口 长度10s
        //keyedStream.window(SlidingProcessingTimeWindows.of(Time.seconds(10),Time.seconds(2))); // 滑动窗口 长度10 步长2
        //keyedStream.window(ProcessingTimeSessionWindows.withGap(Time.seconds(5)));// 会话窗口，超时间隔 5s
        //
        //// 基于计数的
        //keyedStream.countWindow(5);// 滚动窗口，窗口长度 = 5个元素
        //keyedStream.countWindow(5,2); // 滑动窗口，窗口长度 = 5个元素 滑动步长 = 2个元素
        //keyedStream.window(GlobalWindows.create().) // 全局窗口， 计数窗口底层用的就是这个

        //TODO 2. 指定 窗口函数： 窗口内数据的计算逻辑
        WindowedStream<WaterSensor, String, TimeWindow> sensorWS = keyedStream.window(TumblingProcessingTimeWindows.of(Time.seconds(10)));
        // 增量聚合： 来一条数据 计算一条数据，窗口触发的时候输出计算结果
        //sensorWS
                //.reduce()
                //.aggregate()

        // 全窗口函数： 数据不来不计算，存起来，窗口触发的时候，计算并输出结果


        env.execute();
    }
}

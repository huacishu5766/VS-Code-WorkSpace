package flink_demo.window;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @Auther:huacishu
 * @Date: 2025/7/23
 */
public class WindowProcessDemo {
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

       //sensorWS
       //        .apply(new WindowFunction<WaterSensor, String, String, TimeWindow>() {
       //    /**
       //     *
       //     * @param s  分组的key
       //     * @param window 窗口对象
       //     * @param input  存的数据
       //     * @param out    采集器
       //     * @throws Exception
       //     */
       //    @Override
       //    public void apply(String s, TimeWindow window, Iterable<WaterSensor> input, Collector<String> out) throws Exception {
       //
       //    }
       //})
        SingleOutputStreamOperator<String> process = sensorWS
                .process(new ProcessWindowFunction<WaterSensor, String, String, TimeWindow>() {
                    /**
                     *  全窗口函数计算逻辑 ： 窗口触发时 才会调用一次 统一计算所有的数据
                     <IN> – The type of the input value.
                     <OUT> – The type of the output value.
                     <KEY> – The type of the key.
                     <W> – The type of Window that this window function can be applied on.
                     * @param s
                     * @param context
                     * @param elements
                     * @param out
                     * @throws Exception
                     */
                    @Override
                    public void process(String s, ProcessWindowFunction<WaterSensor, String, String, TimeWindow>.Context context, Iterable<WaterSensor> elements, Collector<String> out) throws Exception {
                        long startTs = context.window().getStart();
                        long endTs = context.window().getEnd();
                        String windowStart = DateFormatUtils.format(startTs, "yyyy-MM-dd HH:mm:ss.SSS");
                        String windowEnd = DateFormatUtils.format(endTs, "yyyy-MM-dd HH:mm:ss.SSS");
                        long count = elements.spliterator().estimateSize();

                        out.collect("key" + s + "的窗口[" + windowStart + "," + windowEnd + "，包含" + count + "条数据===>" + elements.toString());
                    }
                });

        process.print();

        env.execute();
    }
}

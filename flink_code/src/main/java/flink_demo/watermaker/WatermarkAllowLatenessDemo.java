package flink_demo.watermaker;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @Auther:huacishu
 * @Date: 2025/7/28
 */
public class WatermarkAllowLatenessDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> sensorDS = env
                .socketTextStream("hadoop101", 7777)
                .map(new WaterSensorMapFunction());

        //TODO 指定watermark 策略

        //1、定义Watermark 策略
        WatermarkStrategy<WaterSensor> waterSensorWatermarkStrategy = WatermarkStrategy
                //1.1  指定Watermark生成，乱序的，等待3秒
                .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                //1.2 指定时间戳 分配器 从数据中提取
                .withTimestampAssigner((element ,recordTimestamp ) ->  {
                            System.out.println("数据 = " + element + ",recordTs = " + recordTimestamp);
                            return element.getTs() * 1000L;
                        }
                );

        SingleOutputStreamOperator<WaterSensor> sensorDSWithWatermark = sensorDS
                .assignTimestampsAndWatermarks(waterSensorWatermarkStrategy);

        sensorDSWithWatermark.keyBy(sensor -> sensor.getId())
                //使用 事件时间语义 的窗口
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .allowedLateness(Time.seconds(5))//允许迟到5秒 关闭窗户
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
                })
                .print();

        env.execute();
    }
}

/**
 * 窗口允许迟到
 *  推迟关窗时间，在关窗之前，迟到数据来了，还能被窗口计算，来一条迟到数据出发一次计算
 *  关窗后，迟到数据不会被计算
 *
 *
 */
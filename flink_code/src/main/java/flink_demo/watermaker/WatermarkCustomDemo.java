package flink_demo.watermaker;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
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
 * @Date: 2025/7/23
 */
public class WatermarkCustomDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> sensorDS = env
                .socketTextStream("hadoop101", 7777)
                .map(new WaterSensorMapFunction());
        //默认周期
        env.getConfig().setAutoWatermarkInterval(2000);

        //TODO 指定watermark 策略
        WatermarkStrategy<WaterSensor> waterSensorWatermarkStrategy = WatermarkStrategy
                //TODO 指定自定义的watermark生成器
                .forGenerator((WatermarkStrategy<WaterSensor>) context -> new MyPeriodWatermarkGenerator<>(3000L))
                .withTimestampAssigner((element ,recordTimestamp ) ->  {
                        return element.getTs() * 1000L;
                    }
                );

        SingleOutputStreamOperator<WaterSensor> sensorDSWithWatermark = sensorDS
                .assignTimestampsAndWatermarks(waterSensorWatermarkStrategy);

        sensorDSWithWatermark.keyBy(sensor -> sensor.getId())
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
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



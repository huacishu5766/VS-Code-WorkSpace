package flink_demo.process;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
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
public class KeyedProcessTimeDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> sensorDS = env
                .socketTextStream("hadoop101", 7777)
                .map(new WaterSensorMapFunction());

        SingleOutputStreamOperator<WaterSensor> sensorDSWithWatermark = sensorDS
                .assignTimestampsAndWatermarks(
                    WatermarkStrategy
                            .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                            .withTimestampAssigner((element, ts ) -> {
                                return element.getTs() * 1000L;
                            })

        );

        KeyedStream<WaterSensor, String> keyedStream =
                sensorDSWithWatermark.keyBy(sensor -> sensor.getId());

        keyedStream.process(new KeyedProcessFunction<String, WaterSensor, String>() {
            /**
             * 来一条调用一次
             * @param value
             * @param ctx
             * @param out
             * @throws Exception
             */
            @Override
            public void processElement(WaterSensor value, KeyedProcessFunction<String, WaterSensor, String>.Context ctx, Collector<String> out) throws Exception {
                System.out.println(ctx.getCurrentKey());

                //定时器
                TimerService timerService = ctx.timerService();
                //注册定时器：事件时间
                timerService.registerEventTimeTimer(5000L);
                System.out.println("当前时间是：" + ctx.getCurrentKey()+"注册了一个5s的定时器");
            }

            /**
             * 定时器触发一次调用一次
             * @param timestamp 当前时间进展
             * @param ctx   上下文
             * @param out   采集器
             * @throws Exception
             */
            @Override
            public void onTimer(long timestamp, KeyedProcessFunction<String, WaterSensor, String>.OnTimerContext ctx, Collector<String> out) throws Exception {

                System.out.println("现在时间是：" + timestamp);
            }
        });

        env.execute();
    }
}


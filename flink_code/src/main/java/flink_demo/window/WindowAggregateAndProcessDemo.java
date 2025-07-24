package flink_demo.window;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @Auther:huacishu
 * @Date: 2025/7/23
 */
public class WindowAggregateAndProcessDemo {
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

        // 2. 窗口函数 增量聚合 + 全窗口 process
        /**
         *
         */
        SingleOutputStreamOperator<String> result = sensorWS.aggregate(new MyAgg(),new MyProcess());

        result.print();

        env.execute();
    }

    public static class MyAgg implements AggregateFunction<WaterSensor,Integer,String>{
        @Override
        public Integer createAccumulator() {
            System.out.println("创建累加器");
            return 0;
        }

        @Override
        public Integer add(WaterSensor waterSensor, Integer acc) {
            System.out.println("调用add");
            return acc + waterSensor.getVc();
        }

        @Override
        public String getResult(Integer acc) {
            System.out.println("调用getResult方法");
            return acc.toString();
        }

        @Override
        public Integer merge(Integer integer, Integer acc1) {
            // 只有会话窗口会用
            System.out.println("调用merge方法");
            return null;
        }
    }

    public static class MyProcess extends ProcessWindowFunction<String,String,String,TimeWindow>{
        @Override
        public void process(String s, ProcessWindowFunction<String, String, String, TimeWindow>.Context context, Iterable<String> elements, Collector<String> out) throws Exception {
            long startTs = context.window().getStart();
            long endTs = context.window().getEnd();
            String windowStart = DateFormatUtils.format(startTs, "yyyy-MM-dd HH:mm:ss.SSS");
            String windowEnd = DateFormatUtils.format(endTs, "yyyy-MM-dd HH:mm:ss.SSS");
            long count = elements.spliterator().estimateSize();

            out.collect("key" + s + "的窗口[" + windowStart + "," + windowEnd + "，包含" + count + "条数据===>" + elements.toString());
        }
    }
}

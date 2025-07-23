package flink_demo.window;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.flink.api.common.functions.AggregateFunction;
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
public class WindowAggregateDemo {
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
         * 1、属于本窗口的第一条数据来，创建窗口，创建累加器
         * 2、增量聚合：来一条计算一条 调用一次 add方法
         * 3、窗口输出时调用一次getResult方法
         * 4、输入输出中间 类型可以不一样非常灵活
         */
        SingleOutputStreamOperator<String> aggregate = sensorWS.aggregate(
                /**
                 * 1 输入数据的类型
                 * 2 累加器的类型，存储中间计算结果的类型
                 * 3 输出的类型
                 */
                new AggregateFunction<WaterSensor, Integer, String>() {
                    /**
                     * 初始化累加器
                     * @return
                     */
                    @Override
                    public Integer createAccumulator() {
                        System.out.println("创建累加器");
                        return 0;
                    }

                    /**
                     * 聚合逻辑
                     * @param waterSensor
                     * @param integer
                     * @return
                     */
                    @Override
                    public Integer add(WaterSensor waterSensor, Integer acc) {
                        System.out.println("调用add");
                        return acc + waterSensor.getVc();
                    }

                    /**
                     * 获取最终结果，窗口触发时输出
                     * @param integer
                     * @return
                     */
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
        );

        aggregate.print();

        env.execute();
    }
}

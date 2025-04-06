package flink_demo.split;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SideOutputDataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * @Auther:huacishu
 * @Date: 2025/4/6
 */
public class SplitStreamByoutputTag {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);

        SingleOutputStreamOperator<WaterSensor> sensorDS = env.socketTextStream("hadoop101", 7777)
                .map(new WaterSensorMapFunction());


        /**
         * 使用侧输出流，实现分流
         * 需求：watersensor的数据，s1 和s2的数据分开
         *
         * 总结：
         *      1、使用process算子
         *      2、定义OutputTag对象
         *      3、调用ctx.output(标签对象，数据)
         *      4、通过主流 获取测流
         */
        OutputTag<WaterSensor> s1Tag = new OutputTag<>("s1", Types.POJO(WaterSensor.class));//标签名  数据类型
        OutputTag<WaterSensor> s2Tag = new OutputTag<>("s2", Types.POJO(WaterSensor.class));//标签名  数据类型

        SingleOutputStreamOperator<WaterSensor> process = sensorDS
                .process(new ProcessFunction<WaterSensor, WaterSensor>() {
                    @Override
                    public void processElement(WaterSensor value, ProcessFunction<WaterSensor, WaterSensor>.Context ctx, Collector<WaterSensor> out) throws Exception {
                        String id = value.getId();
                        if ("s1".equals(id)) {
                            //s1 的数据, 放到侧输出流s1中

                            ctx.output(s1Tag, value);
                            /**
                             * 上下文调用output 将数据放入侧输出流
                             * 1、Tag对象，
                             * 2、放入测数据流汇中的数据
                             */
                        } else if ("s2".equals(id)) {
                            //s2 的数据, 放到侧输出流s2中
                             ctx.output(s2Tag, value);
                        } else {
                            out.collect(value);
                        }

                    }
                });
        //从主流中，根据标签，获取侧输出流
        SideOutputDataStream<WaterSensor> s1 = process.getSideOutput(s1Tag);
        SideOutputDataStream<WaterSensor> s2 = process.getSideOutput(s2Tag);
        //打印主流
        process.print();
        //打印侧输出流
        s1.printToErr("s1");
        s2.printToErr("s2");

        env.execute();
    }
}


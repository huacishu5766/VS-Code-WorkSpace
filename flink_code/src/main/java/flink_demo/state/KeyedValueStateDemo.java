package flink_demo.state;

import flink_demo.bean.WaterSensor;
import flink_demo.function.WaterSensorMapFunction;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * TODO
 *
 * @Auther:huacishu
 * @Date: 2025/8/1
 */
public class KeyedValueStateDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> sensorDS = env
                .socketTextStream("hadoop101", 77777)
                .map(new WaterSensorMapFunction())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((element, recordTimestamp) -> element.getTs() * 1000L)
                );

        sensorDS.keyBy(sensor -> sensor.getId())
                .process(new KeyedProcessFunction<String, WaterSensor, String>() {

                    //TODO 1、定义状态
                    ValueState<Integer> lastVcState;

                    //TODO 2、在open方法中，初始化状态
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        //状态描述器两个参数：第一个参数：起个名字：唯一不重复；第二个参数。存储的类型
                        lastVcState = getRuntimeContext().getState(new ValueStateDescriptor<Integer>("last-vc", Types.INT));
                    }

                    @Override
                    public void processElement(WaterSensor value, Context ctx, Collector<String> out) throws Exception {
                        //lastVcState.value() //取出值状态里的数据
                        //lastVcState.update() //更新 值状态里的数据;


                        //1、取出上一条数据的水位值
                        int lastVc = lastVcState.value() == null ? 0 : lastVcState.value();
                        //2、求差值的绝对值 判断超过十
                        Integer vc = value.getVc();
                        if (Math.abs(vc - lastVc) > 10){
                            out.collect("当前水位值 = " + vc + "，与上一条数据的水位值 = " + lastVc + "，相差超过10");
                        }
                        //3、更新状态
                        lastVcState.update(vc);
                    }
                });


        env.execute();
    }
}

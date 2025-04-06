package flink_demo.function;

import flink_demo.bean.WaterSensor;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @Auther:huacishu
 * @Date: 2025/4/6
 */
public class WaterSensorMapFunction implements MapFunction<String, WaterSensor> {
    @Override
    public WaterSensor map(String s) throws Exception {
        String[] datas = s.split(",");
        return new WaterSensor(datas[0], Long.valueOf(datas[1]), Integer.valueOf(datas[2]));
    }
}

package flink_demo.function;

import flink_demo.bean.WaterSensor;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @Auther:huacishu
 * @Date: 2025/4/1
 */
public class MapFunctionImpl implements MapFunction<WaterSensor,String> {
    @Override
    public String map(WaterSensor value) throws Exception {
        return value.getId();
    }
}

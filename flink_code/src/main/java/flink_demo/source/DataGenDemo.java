package flink_demo.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Auther:huacishu
 * @Date: 2025/3/31 23:51
 */
public class DataGenDemo {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        /**
         * 数据生成器Source 四个参数
         * 1.  GeneratorFunction接口，需要实现，重写map方法 数据类型固定式Long
         * 2.  Long 类型 自动生成的数字序列的最大值，达到这个值就停止
         * 3.  限速策略，比如美秒生成几条数据
         * 4.  返回的类型
         */
        DataGeneratorSource<String> dataGeneratorSource = new DataGeneratorSource<>(
                new GeneratorFunction<Long, String>() {
                    @Override
                    public String map(Long value) throws Exception {
                        return "Number" + value;
                    }
                },
                10,
                RateLimiterStrategy.perSecond(1),
                Types.STRING
        );

        env.fromSource(dataGeneratorSource, WatermarkStrategy.noWatermarks(), "data-gen");
    }
}

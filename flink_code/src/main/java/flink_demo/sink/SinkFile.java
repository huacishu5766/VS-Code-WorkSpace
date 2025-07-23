package flink_demo.sink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.time.Duration;
import java.time.ZoneId;

/**
 * @Auther:huacishu
 * @Date: 2025/4/7
 */
public class SinkFile {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        //必须开启checkpoint 否则一直都是inprocess
       env.enableCheckpointing(1000, CheckpointingMode.EXACTLY_ONCE);

        DataGeneratorSource<String> dataGeneratorSource = new DataGeneratorSource<>(
                new GeneratorFunction<Long, String>() {
                    @Override
                    public String map(Long value) throws Exception {
                        return "Number" + value;
                    }
                },
                1000,
                RateLimiterStrategy.perSecond(10),
                Types.STRING
        );

        DataStreamSource<String> dataGen = env.fromSource(dataGeneratorSource, WatermarkStrategy.noWatermarks(), "data-gen");

        //TODO 输出到文件系统
        FileSink<String> fileSink = FileSink
                //输出行式存储的文件，指定路径，指定编码
                .<String>forRowFormat(new Path("/tmp"), new SimpleStringEncoder<String>("UTF-8"))
                //输出文件的一些配置，文件名、前后缀、压缩方式等
                .withOutputFileConfig(OutputFileConfig.builder()
                        .withPartPrefix("huacishu")
                        .withPartSuffix(".txt")
                        .build()
                )
                //按照目录文件分桶
                .withBucketAssigner(new DateTimeBucketAssigner<>("yyyy-MM-dd HH", ZoneId.of("Asia/Shanghai")))
                //文件的滚动策略 根据条件生产  滚动时间为 10秒 大小为1M
                .withRollingPolicy(
                        DefaultRollingPolicy.builder()
                                .withRolloverInterval(Duration.ofSeconds(10))
                                .withMaxPartSize(new MemorySize(1024 * 1024))
                                .build()
                )
                .build();

        dataGen.sinkTo(fileSink);

        env.execute();
    }
}

package flink_demo.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Auther:huacishu
 * @Date: 2025/3/31
 */
public class FileSourceDemo {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(1);
        //TODO 从文件读：新Source架构\
        FileSource<String> fileSource = FileSource.
                forRecordStreamFormat(
                        new TextLineInputFormat(),
                        new Path("input/word.txt")
                )
                .build();
        env
                .fromSource(fileSource, WatermarkStrategy.noWatermarks(), "fileSource")
                .print();
    }
}

/**
 * 新的Source写法
 * env.fromSource(Source,Watermark,名字)
 */

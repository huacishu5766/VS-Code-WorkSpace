package flink_demo.Transform;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @Auther:huacishu
 * @Date: 2025/4/3
 */
public class RichFunctionDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        /**
         * RichXxxxxFunction : 富函数，可以获取运行时上下文，可以获取子任务编号，子任务名称，以及一些监控指标
         *
         *
         * 1、多了生命周期的管理方法：
         *      1、open()：每个子任务，在启动时，调用一次
         *      2、close()：每个子任务，在结束时，调用一次
         *      =》如果是flink程序异常刮掉，不会调用close方法
         *      =》如果是cancel 掉，会调用close方法
         * 2、多了一个运行时上下文
         *       可以获取一些运行时的环境信息，比如 子任务编号、名称、
         */

        env.fromElements("a", "b", "c", "d")
                .map(new RichMapFunction<String, String>() {
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        RuntimeContext runtimeContext = getRuntimeContext();

                        System.out.println(
                                "子任务编号 = "+ getRuntimeContext().getIndexOfThisSubtask()
                                        + " 子任务名称 = "+ getRuntimeContext().getTaskNameWithSubtasks()
                                        +"调用 open "
                        );

                    }

                    @Override
                    public void close() throws Exception {
                        super.close();
                        System.out.println(
                                "子任务编号 = "+ getRuntimeContext().getIndexOfThisSubtask()
                                        + " 子任务名称 = "+ getRuntimeContext().getTaskNameWithSubtasks()
                                        +"调用 close "
                        );
                    }

                    @Override
                    public String map(String s) throws Exception {
                        return s + 1;
                    }
                })
                .print();

        env.execute();
    }
}

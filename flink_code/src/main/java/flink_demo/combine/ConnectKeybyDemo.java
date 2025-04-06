package flink_demo.combine;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Auther:huacishu
 * @Date: 2025/4/6
 */
public class ConnectKeybyDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        DataStreamSource<Tuple2<Integer, String>> source1 = env.fromElements(
                Tuple2.of(1, "a1"),
                Tuple2.of(1, "a2"),
                Tuple2.of(2, "b"),
                Tuple2.of(3, "c")
        );
        DataStreamSource<Tuple3<Integer, String, Integer>> source2 = env.fromElements(
                Tuple3.of(1, "aa1", 1),
                Tuple3.of(1, "aa2", 2),
                Tuple3.of(2, "bb", 1),
                Tuple3.of(3, "cc", 1)
        );

        ConnectedStreams<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>> connect = source1.connect(source2);

        //多并行度下，需要根据关联条件进行keyby，才能保证key相同的数据到一起去，才能匹配上
        ConnectedStreams<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>> connectkeyBy = connect.keyBy(s1 -> s1.f0, s2 -> s2.f0);


        /**
         * 实现互相匹配的效果：不一定谁的数据先来
         *  1、每条流有数据来，存到一个变量中
         *      hashmap
         *          --》key = id，第一个字段值
         *          --》value = list<数据>
         *  2、每条流有数据来的时候，除了存变量中，不知道对方是否有匹配的 ，去另外一条流存到的变量查找是否有匹配上的
         */
        SingleOutputStreamOperator<String> process = connectkeyBy.process(new CoProcessFunction<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>, String>() {

            //定义hashmap 用来存数据
            HashMap<Integer, List<Tuple2<Integer, String>>> map1 = new HashMap<>();
            HashMap<Integer, List<Tuple3<Integer, String, Integer>>> map2 = new HashMap<>();

            /**
             * 第一条流的处理逻辑
             * @param value 第一条流的数据
             * @param ctx   上下文
             * @param out   采集器
             * @throws Exception
             */
            @Override
            public void processElement1(Tuple2<Integer, String> value, CoProcessFunction<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>, String>.Context ctx, Collector<String> out) throws Exception {
                Integer id = value.f0;
                // TODO 1、s1的数据来了，就存到变量中
                if (!map1.containsKey(id)) {
                    //1.1 如果key不存在，说明是该key 的第一条数据，初始化，put进map中
                    ArrayList<Tuple2<Integer, String>> s1Values = new ArrayList<>();
                    s1Values.add(value);
                    map1.put(id, s1Values);
                } else {
                    //1.2 不是第一条数据，key存在，直接添加到value的list中
                    map1.get(id).add(value);
                }

                //TODO 2、去map2中查找是否有id匹配上 不匹配不输出
                if (map2.containsKey(id)) {
                    for (Tuple3<Integer, String, Integer> s2Element : map2.get(id)) {
                        out.collect("s1:" + value + " -- " + "s2:" + s2Element);
                    }
                }
            }

            /**
             * 第二条流的处理逻辑
             * @param value 第一条流的数据
             * @param ctx   上下文
             * @param out   采集器
             * @throws Exception
             */
            @Override
            public void processElement2(Tuple3<Integer, String, Integer> value, CoProcessFunction<Tuple2<Integer, String>, Tuple3<Integer, String, Integer>, String>.Context ctx, Collector<String> out) throws Exception {
                Integer id = value.f0;
                // TODO 1、s2的数据来了，就存到变量中
                if (!map2.containsKey(id)) {
                    //1.1 如果key不存在，说明是该key 的第一条数据，初始化，put进map中
                    ArrayList<Tuple3<Integer, String, Integer>> s2Values = new ArrayList<>();
                    s2Values.add(value);
                    map2.put(id, s2Values);
                } else {
                    //1.2 不是第一条数据，key存在，直接添加到value的list中
                    map2.get(id).add(value);
                }

                //TODO 2、去map2中查找是否有id匹配上 不匹配不输出
                if (map1.containsKey(id)) {
                    for (Tuple2<Integer, String> s1Element : map1.get(id)) {
                        out.collect("s1:" + s1Element + " --- " + "s2:" + value);
                    }
                }
            }
        });

        process.print();

        env.execute();
    }
}

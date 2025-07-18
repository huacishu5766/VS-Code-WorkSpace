package com.huacishu.FlinkStudy.day01;

/**
 * @Auther:huacishu
 * @Date: 2025/7/11
 */

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 *
 *  从控制台向nc服务输入数据：
 *     {"order_id":1,"order_amt":38.8,"order_type":"团购"}
 *     {"order_id":2,"order_amt":38.2,"order_type":"普通"}
 *     {"order_id":3,"order_amt":40.0,"order_type":"普通"}
 *     {"order_id":4,"order_amt":25.8,"order_type":"秒杀"}
 *     {"order_id":5,"order_amt":52.4,"order_type":"团购"}
 *     {"order_id":6,"order_amt":24.0,"order_type":"秒杀"}
 *
 *  用 flink实时统计：当前的每种类型的订单总金额
 *
 **/
public class Code02_order {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> stream = env.socketTextStream("hadoop101", 9898);

        // 解析json
        // 解析json
        SingleOutputStreamOperator<Order> orderStream = stream.map(new MapFunction<String, Order>() {
            @Override
            public Order map(String json) throws Exception {
                return JSON.parseObject(json, Order.class);
            }
        });

        KeyedStream<Order, String> keyedStream = orderStream.keyBy(od -> od.order_type);

        SingleOutputStreamOperator<Order> sum = keyedStream.sum("order_amt");

        sum.print();

        env.execute();


    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private int order_id;
        private double order_amt;
        private String order_type;

    }


}

package com.huacishu.BigData.Spark.D02_RDD;

import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

public class S03_map {
    public static void main(String[] args) {
         //1、创建SparkConf对象
        SparkConf conf = new SparkConf();
        //2、加载配置
        conf.setMaster("local[*]");
        conf.setAppName("spark01_env");
        //3、创建SparkContext对象
        JavaSparkContext context = new JavaSparkContext(conf);//无参构造


        List<Integer> nums = Arrays.asList(1, 2, 3, 4);

        // RDD的方法会有很多，主要讲解核心，重要的方法
        // 学习的重点：
        //    1. 名字  干什么的，什么意思
        //    2. IN    传入参数
        //    3. OUT    输出  输出的是什么。

        // RDD的方法会有很多，但是分为2类
        // 1. 转换：将数据向后流转
        // 2. 行动：打开数据开关

        // RDD方法处理数据的分类
        // 1. 单值 : 1, "abc", new User(), new ArrayList(), (Key, Value)
        // 2. 键值 : KV => (Key, Value)  学号，姓名
        //          word -> count
//        int i = 10;
//        String s = "abc";
//        Object o = new Object();

        JavaRDD<Integer> rdd = context.parallelize(nums);

        JavaRDD<Object> javaRDD = rdd.map(new Function<Integer, Object>() {
            @Override
            public Object call(Integer num) throws Exception {
                return num + 2;
            }
        });

        javaRDD.collect().forEach(System.out::println);

        rdd.map(new Function<Integer, Object>() {
            @Override
            public Object call(Integer v1) throws Exception {
                return null;
            }
        });

        rdd.map(num ->{
                return num + 2;
        });
        //4、关闭context
                context.close();
    }
    
}
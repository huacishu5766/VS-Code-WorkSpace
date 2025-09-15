package com.huacishu.BigData.Spark.D01_instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class S02_memoryRDD {
    public static void main(String[] args) {
        
    
    //创建Spark环境
    //1、创建SparkConf对象
    SparkConf conf = new SparkConf();
    conf.setMaster("local[2]");
    conf.setAppName("spark01_env");

    JavaSparkContext context = new JavaSparkContext(conf);


    //1、加载数据
    ArrayList<Object> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    list.add(3);
    list.add(4);
    System.out.println(list);

    List<String> s1 = Arrays.asList("zhangsan","lisi","wangwu");

    //2、将数据加载进spark 加载进RDD，创建RDD
    //JavaRDD<String> javaRDD = context.parallelize(list);
    JavaRDD<String> javaRDD = context.parallelize(s1);
    //3.处理数据
    //4.保存结果
    javaRDD.saveAsTextFile("output");

    //4、关闭context
    context.close();

    }
    
}

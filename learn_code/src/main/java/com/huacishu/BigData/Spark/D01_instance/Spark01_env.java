package com.huacishu.BigData.Spark.D01_instance;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;

public class Spark01_env {
    public static void main(String[] args) {
        // 1、创建SparkConf对象，配置信息
        SparkConf conf = new SparkConf();
        conf.setMaster("local[2]");
        conf.setAppName("sprak01");
        // 2、 创建SparkContext对象，上下文环境
        SparkContext sparkContext = new SparkContext(conf);



        // 3、 关闭上下文环境
        sparkContext.stop();
    }
}
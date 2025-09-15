  package com.huacishu.BigData.SparkSQL;

import org.apache.spark.sql.SparkSession;

public class SparkSQL01_env {
  
    public static void main(String[] args) {
        //构建环境对象
        //Spark在结构化数据的处理场景中对核心功能、环境进行了封装
        //构建SparkSQL的环境对象，一般采用构造器模式
        

       SparkSession sparkSession = SparkSession
       .builder()
       .appName("spark01")
       .master("local[2]")
       .getOrCreate();

       sparkSession.close();
    }
  }
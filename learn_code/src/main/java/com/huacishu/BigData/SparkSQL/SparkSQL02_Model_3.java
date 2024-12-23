package com.huacishu.BigData.SparkSQL;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkSQL02_Model_3 {

   public static void main(String[] args) {
    
       SparkSession sparkSession = SparkSession
       .builder()
       .appName("spark01")
       .master("local[2]")
       .getOrCreate();
       Dataset<Row> ds = sparkSession.read().json("data/user.json");

       // 采用DSL语法进行访问
       ds.select("id","name","age")
       .show();


       sparkSession.close();
   } 
}
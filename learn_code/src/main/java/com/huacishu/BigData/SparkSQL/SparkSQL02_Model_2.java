package com.huacishu.BigData.SparkSQL;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkSQL02_Model_2 {

   public static void main(String[] args) {
    
       SparkSession sparkSession = SparkSession
       .builder()
       .appName("spark01")
       .master("local[2]")
       .getOrCreate();
       Dataset<Row> ds = sparkSession.read().json("data/uer.json");

       //模型对象的访问
         // 将数据模型转换为二维的结构（行，列），可以通过SQL进行访问
         // 视图：是表的查询结果集，表可以增加、修改、删除、查询
         // 视图不能增加、修改、删除，只能查询
         ds.createOrReplaceTempView("user");

         // 当前JDK版本不适合开发SQL （不能换行）
         sparkSession.sql("select * from user")
         .show();


       sparkSession.close();
   } 
}
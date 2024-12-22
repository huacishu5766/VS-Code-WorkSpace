package com.huacishu.BigData.SparkSQL;

import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkSQL02_Model {

   public static void main(String[] args) {
    
       SparkSession sparkSession = SparkSession
       .builder()
       .appName("spark01")
       .master("local[2]")
       .getOrCreate();

       //SparkSQL对数据模型也进行了封装：RDD --> Dataset
    //    对接文件数据源时，会将文件中的一行数据封装成ROW对象
       Dataset<Row> json = sparkSession.read().json("data/uer.json");

    //    RDD<Row>   = json.rdd();


       sparkSession.close();
   } 
}
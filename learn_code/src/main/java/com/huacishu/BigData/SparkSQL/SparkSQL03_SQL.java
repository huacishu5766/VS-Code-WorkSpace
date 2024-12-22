package com.huacishu.BigData.SparkSQL;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkSQL03_SQL {
    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession
        .builder()
        .appName("spark01")
        .master("local[2]")
        .getOrCreate();


       //读取数据
        Dataset<Row> ds = sparkSession.read().json("data/user.json");
 
        //将数据模型转换成表，方便SQL的使用
        ds.createOrReplaceTempView("user");


        //使用SQL文的方式操作数据
        String sql = "select age from user";

        Dataset<Row> sqlDS = sparkSession.sql(sql);

        //展示数据模型的效果
        sqlDS.show();

       sparkSession.close();
    }
    
}

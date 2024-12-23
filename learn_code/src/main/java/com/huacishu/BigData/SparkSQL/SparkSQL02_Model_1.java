package com.huacishu.BigData.SparkSQL;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;


public class SparkSQL02_Model_1 {

   public static void main(String[] args) {
    
       SparkSession sparkSession = SparkSession
       .builder()
       .appName("spark01")
       .master("local[2]")
       .getOrCreate();
       
       //SparkSQL对数据模型也进行了封装：RDD --> Dataset
       //    对接文件数据源时，会将文件中的一行数据封装成ROW对象
       Dataset<Row> ds = sparkSession.read().json("data/uer.json");
       //RDD<Row>  rdd = ds.rdd();

       //[1,2,3,4] 打印出 底层是数组
       ds.foreach(
          row -> {
             System.out.println(row);
         }
       );

       //dataframe 1.3
       ds.foreach(
          row -> {
             System.out.println(row.getInt(2));
         }
       );

       //将数据模型中的数据类型进行转化，将Row转换成其他对象进行处理
       Dataset<User> userDS = ds.as(Encoders.bean(User.class));

       userDS.foreach(
          user -> {
             System.out.println(user.getId()+","+user.getName()+","+user.getAge());
         }
       );


       sparkSession.close();
   } 
}

class User{
   private int id;
   private String name;
   private String age;
   public User() {
   }
   public User(int id, String name, String age) {
      this.id = id;
      this.name = name;
      this.age = age;
   }
   public void setId(int id) {
      this.id = id;
   }
   public void setName(String name) {
      this.name = name;
   }
   public void setAge(String age) {
      this.age = age;
   }
   public int getId() {
      return id;
   }
   public String getName() {
      return name;
   }
   public String getAge() {
      return age;
   }
   
   
}

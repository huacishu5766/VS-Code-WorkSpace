package com.huacishu.BigData.SparkSQL;

import org.apache.avro.generic.GenericData.StringType;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.;

public class SparkSQL03_SQL_1 {
    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession
        .builder()
        .appName("spark01")
        .master("local[2]")
        .getOrCreate();

       //读取数据
        Dataset<Row> ds = sparkSession.read().json("data/user.json");
        ds.createOrReplaceTempView("user");

        ////prefix + name 
        //  Name:Alice
        //String sql = "select 'Name' + name from user";
        //String sql = "select concat('Name' + name) from user";
        //可以解决但concat不具备通用性，mysql(+) orcale(||) db2 sqlserver(+)
        //Shark => Spark On Hive => SparkSQL
        //      => Hive On Spark => 数仓
        
      

        // SparkSQL ：可以在SQL中增加自定义方法来实现复杂的逻辑
        String sql = "select prefixName(name) from user";

        // 如果想要自定义的方法能够在SQL中直接使用，需要注册为UDF
        //     register 需要传递3个参
        //          第一个参数 表示SQL中使用的方法名
        //          第二个参数 表示逻辑
        //          第三个参数 返回的数据类型
        sparkSession.udf().register("prefixName",new UDF1<String,String>() {

            @Override
            public String call(String name) {
                return "Name:" + name;
            }
            
        },DataTypes.StringType);

        Dataset<Row> sqlDS = sparkSession.sql(sql);
        sqlDS.show();

        // 关闭
        sparkSession.close();
    }
    
}

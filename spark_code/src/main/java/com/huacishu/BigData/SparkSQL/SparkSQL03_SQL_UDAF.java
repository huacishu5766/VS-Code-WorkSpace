package com.huacishu.BigData.SparkSQL;

import java.io.Serializable;


import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.expressions.Aggregator;

public class SparkSQL03_SQL_UDAF {
    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession
        .builder()
        .appName("spark01")
        .master("local[2]")
        .getOrCreate();


       //读取数据
        Dataset<Row> ds = sparkSession.read().json("data/user.json");
        ds.createOrReplaceTempView("user");

        //SparkSQL 采用特殊的方式将UDAF转换成UDF使用
            //UDAF使用时需要创建自定义聚合对象
            //      udaf方法需要传递2个参数
            //          第一个参数表示UDAF对象
            //          第二个参数表示输入编码
            //
        sparkSession.udf().register("myAvg", functions.udaf(
            new MyAvgAgeUDAF(),Encoders.LONG()
        ));


        String sql = "select myAvg(age) from user";
        Dataset<Row> sqlDS = sparkSession.sql(sql);

        //展示数据模型的效果
        sqlDS.show();

       sparkSession.close();
    }
    
}

//  自定义UDAF函数，实现年龄的平均值
//      1、实现自定义的类
//      2、继承
//      3、设定泛型
            // IN : 输入数据类型
            // BUFF ： 缓冲区数据类型
            // OUT ： 输出数据类型
//      4、重写方法

 class MyAvgAgeUDAF extends Aggregator<Long,AvgAgeBuffer,Long>{

    @Override
    public Encoder<AvgAgeBuffer> bufferEncoder() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bufferEncoder'");
    }

    @Override
    public Long finish(AvgAgeBuffer reduction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'finish'");
    }

    @Override
    public AvgAgeBuffer merge(AvgAgeBuffer b1, AvgAgeBuffer b2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'merge'");
    }

    @Override
    public Encoder<Long> outputEncoder() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'outputEncoder'");
    }

    @Override
    public AvgAgeBuffer reduce(AvgAgeBuffer b, Long a) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reduce'");
    }

    @Override
    public AvgAgeBuffer zero() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'zero'");
    }
     

}
class AvgAgeBuffer implements Serializable{
    public long total;
    public long count;
}

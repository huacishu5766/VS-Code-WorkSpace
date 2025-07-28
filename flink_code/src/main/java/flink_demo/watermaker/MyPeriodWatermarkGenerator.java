package flink_demo.watermaker;

import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;

/**
 * @Auther:huacishu
 * @Date: 2025/7/28
 * 自定义水位线生成器
 */
public class MyPeriodWatermarkGenerator<T> implements WatermarkGenerator<T> {
    //乱序等待时间
    private Long delayTS;
    //用来保存当前为止最大的事件时间
    private Long maxTs;

    public MyPeriodWatermarkGenerator(Long delayTS) {
        this.delayTS = delayTS;
        this.maxTs = Long.MIN_VALUE + this.delayTS + 1;
    }

    /**
     * 周期性生成水印
     * 每条数据来都会调用一次，用来提取最大的事件时间，保存下来
     * @param event
     * @param eventTimestamp  提取到的数据的事件时间
     * @param output
     */
    @Override
    public void onEvent(T event, long eventTimestamp, WatermarkOutput output) {
        System.out.println("调用了onEvent方法，获取目前为止的最大时间戳" +  maxTs);
        maxTs = Math.max(maxTs, eventTimestamp);
    }

    /**
     * 周期性生成水印： 发射Watermark
     * @param output
     */
    @Override
    public void onPeriodicEmit(WatermarkOutput output) {
        output.emitWatermark(new Watermark(maxTs - delayTS - 1));
        System.out.println("调用onPeriodicEmit方法，生成Watermark = " + (maxTs - delayTS - 1));

    }
}

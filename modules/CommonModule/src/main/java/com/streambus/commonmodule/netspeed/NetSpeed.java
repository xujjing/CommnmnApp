package com.streambus.commonmodule.netspeed;

import android.net.TrafficStats;

import com.streambus.basemodule.utils.SLog;

/**
 * Yoostar_chen on 2021/5/12 18:18
 */
public class NetSpeed {
    private final String TAG = NetSpeed.class.getSimpleName();

    private long lastTotalRxBytes = 0; //上一次流量总和
    private long lastTimeStamp = 0;//上一次的时间戳

    public String getNetSpeed(int uid) {
        String unit = "\nB/S";
        long nowTotalRxByte = getTotalRxBytes(uid);
        long nowTimeStamp = System.currentTimeMillis();
        if (0 == lastTotalRxBytes) {
            lastTotalRxBytes = nowTotalRxByte;
        }
        long delayTotalRxByte = nowTotalRxByte - lastTotalRxBytes;
        //SLog.i(TAG, "[getNetSpeed]nowTotalRxByte=" + nowTotalRxByte + " ,lastTotalRxBytes=" + lastTotalRxBytes);
        if (delayTotalRxByte / 1024 > 0) {
            unit = "\nKB/S";
            delayTotalRxByte = delayTotalRxByte / 1024;//转换KB
            if (delayTotalRxByte / 1024 > 0) {
                delayTotalRxByte = delayTotalRxByte / 1024;//转换MB
                unit = "\nMB/S";
            }
        }
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxByte;
        //long speed = (delayTotalRxByte) * 1000 / (nowTimeStamp - lastTimeStamp); //毫秒转换秒
        return String.valueOf(delayTotalRxByte) + unit;
    }

    /**
     * 获取当前应用 已使用的流量总和
     * @param uid //getApplicationInfo().uid
     * @return unit kb
     */
    public long getTotalRxBytes(int uid) {
        //获取当前流量
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : TrafficStats.getTotalRxBytes();//unit Byte
    }

}

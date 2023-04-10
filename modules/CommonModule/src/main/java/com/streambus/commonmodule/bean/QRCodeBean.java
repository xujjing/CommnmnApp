package com.streambus.commonmodule.bean;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/4/20
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class QRCodeBean {

    /**
     * barrageSwitch : false
     * currentTime : 1618885967772
     * days : 0
     * qRCodeToken : 44d3a81e-734f-4e08-b3cd-b5f194cdcf28
     * result : 0
     */

    private int result;
    private String message;
    private int days;
    private String qRCodeToken;
    private boolean barrageSwitch;
    private long currentTime;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isBarrageSwitch() {
        return barrageSwitch;
    }

    public void setBarrageSwitch(boolean barrageSwitch) {
        this.barrageSwitch = barrageSwitch;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getQRCodeToken() {
        return qRCodeToken;
    }

    public void setQRCodeToken(String qRCodeToken) {
        this.qRCodeToken = qRCodeToken;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}

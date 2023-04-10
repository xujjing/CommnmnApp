package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/22
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class BuyServiceBean implements Serializable {

    /**
     * {
     * "autoRenew":false, //是否支持自动续费
     * "description":"<img src="http://qiniupicture.xiyanyuanma.com/quick-remark.png"></img>",//服务描述(富文本)
     * "price":6.0, //价格 ，美元
     * "discount":5.2, //折扣价格，美元
     * "discountRemark":"50% OFF",//折扣描述
     * "id":6, //服务id
     * "name":"Quick one year",
     * "period":1, //付费周期
     * "periodUnit":4,//付费周期单位 1-天,2-周,3-月,4-年
     * "remark":"Quick one year"
     * "deviceCount" : 支持设备数量
     * }
     */

    private static String[] TIME_UNIT = {"", "day", "week", "month", "year"};

    private boolean autoRenew;
    private String description;
    private String price;
    private String discount;
    private String discountRemark;
    private String id;
    private String name;
    private int period;
    private int periodUnit;
    private String remark;
    private int deviceCount = 1;

    private String univalent = "";


    private boolean itemSelect;

    public String getUnivalent() {
        return univalent;
    }

    public void setUnivalent(String univalent) {
        this.univalent = univalent;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDiscountRemark() {
        return discountRemark;
    }

    public void setDiscountRemark(String discountRemark) {
        this.discountRemark = discountRemark;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPeriodUnit() {
        return periodUnit;
    }

    public String getPeriodUnitTime() {
        return TIME_UNIT[periodUnit];
    }

    public void setPeriodUnit(int periodUnit) {
        this.periodUnit = periodUnit;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isItemSelect() {
        return itemSelect;
    }

    public void setItemSelect(boolean itemSelect) {
        this.itemSelect = itemSelect;
    }
}

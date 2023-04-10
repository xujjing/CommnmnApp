package com.streambus.commonmodule.bean;

import java.io.Serializable;

public class OrderBean implements Serializable {
    private float cost;//支付费用
    private int id;
    private String orderNo;//订单账号
    private String payType;//支付方式 //1:点数支付 2:PayPal支付
    private int serviceId;
    private String serviceName;//
    private String source;//订单来源
    private long subTime;//订购时间
    private String code;//订购时间

    private int status;//支付状态
//    UNPAID(1, "未支付"),
//    CENCEL(2, "已取消"),
//    PAIDED(3, "已支付"),
//    TO_AUDIT(4, "待人工审核"),
//    SHIPPED(5, "已发货"),
//    SIGNED(6, "已签收"),
//    REFUND(7, "已退款");

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getSubTime() {
        return subTime;
    }

    public void setSubTime(long subTime) {
        this.subTime = subTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPayMethodName() {
        if ("2".equals(payType)) {
            return "Paypal";
        } else if ("1".equals(payType)) {
            return "Point";
        } else {
            return payType;
        }
    }
}

package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/9/26
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class ResultBean implements Serializable {

    private int result;
    private String message;

    private int contentVersion;

    public ResultBean() {
    }

    public ResultBean(int result, String message) {
        this.result = result;
        this.message = message;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(int version) {
        this.contentVersion = version;
    }
}

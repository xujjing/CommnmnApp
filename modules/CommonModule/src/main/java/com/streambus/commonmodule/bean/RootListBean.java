package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/7/23
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class RootListBean<T> implements Serializable {
    private String result;
    private String message;
    private T list;
    private int contentVersion;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getList() {
        return list;
    }

    public void setList(T list) {
        this.list = list;
    }

    public int getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(int contentVersion) {
        this.contentVersion = contentVersion;
    }
}

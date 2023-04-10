package com.streambus.commonmodule.bean;

import java.io.Serializable;

public class EmailCodeBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String result;
    private String message;
    private String code;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "EmailCodeBean{" +
                "result='" + result + '\'' +
                ", message='" + message + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}

package com.streambus.commonmodule.bean;

import java.io.Serializable;

public class CardRechargeBean implements Serializable {
    private String accountId;
    private String token;
    private String code;

    public CardRechargeBean(String accountId, String token, String code) {
        this.accountId = accountId;
        this.token = token;
        this.code = code;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

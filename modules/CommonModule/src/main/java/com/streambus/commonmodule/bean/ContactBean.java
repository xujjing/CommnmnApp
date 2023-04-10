package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/3/10
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class ContactBean implements Serializable {

    private int result;
    private String msg;

    private String contactUsWebsite;
    private String contactUsEmail;
    private String contactUsFacebook;
    private String contactUsWhatsapp;

    public String getContactUsWebsite() {
        return contactUsWebsite;
    }

    public void setContactUsWebsite(String contactUsWebsite) {
        this.contactUsWebsite = contactUsWebsite;
    }

    public String getContactUsEmail() {
        return contactUsEmail;
    }

    public void setContactUsEmail(String contactUsEmail) {
        this.contactUsEmail = contactUsEmail;
    }

    public String getContactUsFacebook() {
        return contactUsFacebook;
    }

    public void setContactUsFacebook(String contactUsFacebook) {
        this.contactUsFacebook = contactUsFacebook;
    }

    public String getContactUsWhatsapp() {
        return contactUsWhatsapp;
    }

    public void setContactUsWhatsapp(String contactUsWhatsapp) {
        this.contactUsWhatsapp = contactUsWhatsapp;
    }


    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

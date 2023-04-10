package com.streambus.commonmodule.upgrade2.bean;

import java.io.Serializable;

/**
 * Author:Yoostar
 * Date:2021/6/7 17:44
 * Description:
 */
public class Introduction implements Serializable {
    private String title;
    private String language;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * Created by LiZhiGang on 2018/3/15.
 */

public class ImgBean implements Serializable {

    private String type;
    private String url;

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url == null ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ImgBean{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

}

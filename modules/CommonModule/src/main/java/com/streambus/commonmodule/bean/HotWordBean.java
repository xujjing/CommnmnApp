package com.streambus.commonmodule.bean;

import java.io.Serializable;

public class HotWordBean implements Serializable {
    private String hotword;//关键词
    private int type;//热词类型 1:点播节目; 2:演员\明星

    public HotWordBean(String hotword, int type) {
        this.hotword = hotword;
        this.type = type;
    }

    public String getHotword() {
        return hotword;
    }

    public void setHotword(String hotword) {
        this.hotword = hotword;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static class TYPE {
        //1:点播节目; 2:演员\明星
         public static final int TYPE_VOD = 1;
         public static final int TYPE_ROLE = 2;
    }
}

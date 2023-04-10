package com.streambus.commonmodule.bean;

import android.util.Log;

import com.streambus.basemodule.utils.SLog;

import java.io.Serializable;


/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/3/23
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class PictureBean implements Serializable {

    public static final String TYPE_ICON = "ICON";
    public static final String TYPE_POSTER_L = "POSTER_L";
    public static final String TYPE_POSTER_H = "POSTER_H";
    public static final String TYPE_BACKGROUND = "BACKGROUND";
    public static final String TYPE_RECOMMEND= "RECOMMEND";
    public static final String TYPE_BANNER= "BANNER";

    private static final String TAG = "ChannelAIDLBean";

    /**
     * type : ICON
     * url : upload/programme/1564650955989eaT.png
     */

    private String type;
    private String url;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type){
       SLog.d(TAG, "PictureBean_setType type=>" + type);
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "PictureBean{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

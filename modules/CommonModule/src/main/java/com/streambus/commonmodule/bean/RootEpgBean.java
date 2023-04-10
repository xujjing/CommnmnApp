package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/7/23
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class RootEpgBean implements Serializable {
    private String result;
    private String message;
    private List<Entry> data;
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

    public List<Entry> getData() {
        return data;
    }

    public void setData(List<Entry> list) {
        this.data = list;
    }

    public int getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(int contentVersion) {
        this.contentVersion = contentVersion;
    }


    @Override
    public String toString() {
        return "RootEpgBean{" +
                "result='" + result + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", contentVersion=" + contentVersion +
                '}';
    }

    public class Entry implements Serializable {
        private static final long serialVersionUID = 1L;
        private String result; //0或者null：成功；1：EPG未更新；other：失败
        private String message; ////返回结果说明
        private String channel; /////频道名称
        private String channelId; /////频道ID
        private ArrayList<EpgBean> programmes;

        public ArrayList<EpgBean> getProgrammes() {
            return programmes;
        }

        public void setProgrammes(ArrayList<EpgBean> programmes) {
            this.programmes = programmes;
        }

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

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "result='" + result + '\'' +
                    ", message='" + message + '\'' +
                    ", channel='" + channel + '\'' +
                    ", channelId='" + channelId + '\'' +
                    ", programmes=" + programmes +
                    '}';
        }
    }

}

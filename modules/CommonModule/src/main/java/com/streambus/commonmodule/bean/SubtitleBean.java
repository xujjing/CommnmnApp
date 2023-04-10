package com.streambus.commonmodule.bean;

import android.text.TextUtils;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2017/11/14.
 */

public class SubtitleBean implements Serializable {
    private String filepath;
    private String filetype;
    private String id;
    private boolean isdefault;
    private String language;
    private int subtitleIndex;//记录改字幕在Adapter中的index, 初始化的时候赋值
    protected CopyOnWriteArrayList<SubtitlesModel> list;//下载成功后存储的字幕
    private float tuningTime;

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isdefault() {
        return isdefault;
    }

    public void setIsdefault(boolean isdefault) {
        this.isdefault = isdefault;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isIsdefault() {
        return isdefault;
    }

    public int getSubtitleIndex() {
        return subtitleIndex;
    }

    public void setSubtitleIndex(int subtitleIndex) {
        this.subtitleIndex = subtitleIndex;
    }

    public CopyOnWriteArrayList<SubtitlesModel> getList() {
        return list;
    }

    public void setList(CopyOnWriteArrayList<SubtitlesModel> list) {
        this.list = list;
    }

    public float getTuningTime() {
        return tuningTime;
    }

    public void setTuningTime(float tuningTime) {
        this.tuningTime = tuningTime;
    }

    public String getUrlID() {
        if (!TextUtils.isEmpty(filepath)) {
            String subtitleId = getId();
            int beginIndex = 0, endIndex = 0;
            beginIndex = filepath.lastIndexOf(File.separator) + 1;
            endIndex = filepath.lastIndexOf('.');
            if (beginIndex < endIndex) {
                subtitleId = filepath.substring(beginIndex, endIndex);
            }
            return subtitleId;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "SubtitleBean{" +
                "filepath='" + filepath + '\'' +
                ", filetype='" + filetype + '\'' +
                ", id='" + id + '\'' +
                ", isdefault=" + isdefault +
                ", language='" + language + '\'' +
                ", subtitleIndex=" + subtitleIndex +
                ", tuningTime=" + tuningTime +
                '}';
    }
}

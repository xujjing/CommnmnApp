package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * Created by huanglu on 2017/7/26.
 */

public class EpgBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String start; //节目开始时间
    private String end;//节目结束时间
    private String category;//节目分类信息
    private String title;//节目标题
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ProgrammesBean{" +
                "start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", category='" + category + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

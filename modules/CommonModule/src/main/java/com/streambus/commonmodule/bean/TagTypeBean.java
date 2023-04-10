package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.List;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/7/24
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class TagTypeBean implements Serializable {
    private String name;
    private String id;
    private List<TagBean> taglist;

    public String getName() {List<TagBean> taglist;
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TagBean> getTaglist() {
        return taglist;
    }

    public void setTaglist(List<TagBean> taglist) {
        this.taglist = taglist;
    }

    @Override
    public String toString() {
        return "TagTypeBean{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", taglist=" + taglist +
                '}';
    }
}

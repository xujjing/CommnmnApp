package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * Created by huanglu on 2017/11/6.
 */

public class TagListBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String typeid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeid() {
        return typeid;
    }

    public void setTypeid(String typeid) {
        this.typeid = typeid;
    }

    @Override
    public String toString() {
        return "TagListBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", typeid='" + typeid + '\'' +
                '}';
    }
}

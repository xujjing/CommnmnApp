package com.streambus.commonmodule.bean;

import java.io.Serializable;

public class RoleBean implements Serializable {
    //角色类型RoleType
    public static final int TOLE_TYPE_DIRETORE = 1;//导演

    public static final int TOLE_TYPE_ACTOR= 2;//演员

    public static final int TOLE_TYPE_EDIT = 3;//编辑

    protected String alias;//别名
    protected String birthday;//出生日期
    protected String country;//国籍
    protected String id;
    protected String name;//姓名
    protected String description;//描述
    protected String photo;////海报
    protected String awards;//获奖描述

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAwards() {
        return awards;
    }

    public void setAwards(String awards) {
        this.awards = awards;
    }

    @Override
    public String toString() {
        return "RoleBean{" +
                "alias='" + alias + '\'' +
                ", birthday='" + birthday + '\'' +
                ", country='" + country + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", photo='" + photo + '\'' +
                ", awards='" + awards + '\'' +
                '}';
    }
}

package com.streambus.commonmodule.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/4/29
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class HomeTypeBean implements Serializable {

    /**
     * id : 695
     * name : Home
     * type : 1
     * description :
     * pictureList : [{"BACKGROUND":["upload/programme/158764477077545b.jpg"]},{"ICON":["upload/programme/15880646555145aH.jpg","upload/programme/15880646555145aH.jpg"]}]
     */
    private static final String ICTURE_TYPE_BACKGROUND = "BACKGROUND";
    private static final String ICTURE_TYPE_ICON = "ICON";


    private long id;
    private String name;
    private int type;
    private String description;
    private Map<String,List<String>> pictureList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String,List<String>> getPictureList() {
        return pictureList;
    }

    public void setPictureList(Map<String,List<String>> pictureList) {
        this.pictureList = pictureList;
    }

    public String getBackground() {
        if (pictureList != null) {
            List<String> list = pictureList.get(ICTURE_TYPE_BACKGROUND);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    public List<String> getIconList() {
        if (pictureList != null) {
            List<String> list = pictureList.get(ICTURE_TYPE_ICON);
            if (list != null && list.size() == 3) {
                return list;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return "HomeTypeBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

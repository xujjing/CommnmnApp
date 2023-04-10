package com.streambus.commonmodule.table;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/12/17
 * 描    述:
 * 修订历史：
 * ================================================
 */
@Entity
public class VodFavChannelInfo implements IVodChannelInfo{
    @Id
    private Long id;//channelId

    @NotNull
    private String channelJson;

    private Long updateTime;

    @Generated(hash = 1638341885)
    public VodFavChannelInfo(Long id, @NotNull String channelJson,
            Long updateTime) {
        this.id = id;
        this.channelJson = channelJson;
        this.updateTime = updateTime;
    }

    @Generated(hash = 1150830062)
    public VodFavChannelInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelJson() {
        return channelJson;
    }

    public void setChannelJson(String channelJson) {
        this.channelJson = channelJson;
    }

    @Override
    public Long getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}

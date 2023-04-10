package com.streambus.commonmodule.table;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/12/21
 * 描    述:
 * 修订历史：
 * ================================================
 */
public interface IVodChannelInfo {
    public Long getId();

    public void setId(Long id);
    public String getChannelJson();
    public void setChannelJson(String channelJson);
    public Long getUpdateTime();
    public void setUpdateTime(Long updateTime);
}

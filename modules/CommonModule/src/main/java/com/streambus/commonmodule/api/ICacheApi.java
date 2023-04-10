package com.streambus.commonmodule.api;

import java.io.IOException;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/2/25
 * 描    述:
 * 修订历史：
 * ================================================
 */
public interface ICacheApi {
    public int getCacheVersion(String key, String language);

    public void saveCacheData(String key, String language, int version, String content) throws IOException;

    public void deleteCache(String key, String language);
}

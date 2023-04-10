package com.streambus.tinkerlib.update;

import java.io.File;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/1/10
 * 描    述:
 * 修订历史：
 * ================================================
 */
public interface IUpgradeListener{

    void onProgress(long progress, long total, String tcpSpeed);

    void onSuccess(File file);

    void onFailed();
}

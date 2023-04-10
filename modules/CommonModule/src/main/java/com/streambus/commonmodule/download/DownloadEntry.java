package com.streambus.commonmodule.download;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.utils.GsonHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/8/2
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class DownloadEntry {
    private static final String TAG = "DownloadEntry";
    private static final String KEY_DOWNLOAD_ENTRY = "000000_download_entry";
    private static final String KEY_DOWNLOAD_INFO = "000000_download_info";
    private static final String FILE_FORMAT = "000000_%d_%d";
    private DownloadInfo downloadInfo;
    private String channelId;
    private MutableLiveData<Map.Entry<Integer,Integer>> downloadStatus = new MutableLiveData<>();
    private File saveDir;
    private File entryFile;
    private int currentState;
    private int progress;

    Disposable disposable;

    public static final int STATE_WAIT = 0;
    public static final int STATE_DOWNING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_COMPLETED = 3;
    public static final int STATE_ERROR = 4;
    public static final int STATE_DELETED = 5;

    public DownloadEntry(File saveDir) {
        this.saveDir = saveDir;
        this.entryFile = new File(saveDir, KEY_DOWNLOAD_ENTRY);
        if (!entryFile.exists()) {
            return;
        }
        try {
            FileInputStream ips = new FileInputStream(entryFile);
            byte[] bytes = new byte[1024];
            int len = ips.read(bytes);
            ips.close();
            String[] split = new String(bytes, 0, len).split("_");
            this.currentState = Integer.valueOf(split[1]);
            if ((this.currentState == STATE_WAIT || this.currentState == STATE_DOWNING)) {
                this.currentState = STATE_PAUSED;
            }
            this.progress = Integer.valueOf(split[2]);
            downloadStatus.postValue(new AbstractMap.SimpleEntry<>(this.currentState, this.progress));

            ips = new FileInputStream(new File(saveDir, KEY_DOWNLOAD_INFO));
            bytes = new byte[1024 * 64];
            len = ips.read(bytes);
            ips.close();
            this.downloadInfo = GsonHelper.toType(new String(bytes, 0, len), DownloadInfo.class);
            this.channelId = downloadInfo.getChannelBean().getId();
        } catch (Exception e) {
            SLog.w(TAG, "parsEntryFile Exception=>" + entryFile.getAbsolutePath(), e);
        }
    }

    public DownloadEntry(File saveDir, DownloadInfo downloadInfo) {
        this.saveDir = saveDir;
        this.downloadInfo = downloadInfo;
        this.channelId = downloadInfo.getChannelBean().getId();
        this.currentState = STATE_WAIT;
        this.progress = 0;
        downloadStatus.postValue(new AbstractMap.SimpleEntry<>(this.currentState, this.progress));
        entryFile = new File(saveDir, KEY_DOWNLOAD_ENTRY);
        resetEntryFile();
        try {
            FileOutputStream opt = new FileOutputStream(new File(saveDir, KEY_DOWNLOAD_INFO));
            opt.write(GsonHelper.toJson(downloadInfo).getBytes());
            opt.flush();
            opt.close();
        } catch (Exception e) {
            SLog.w(TAG, "create DownloadEntry Exception=>" + entryFile.getAbsolutePath(), e);
        }
    }

    public void onWait() {
        this.currentState = STATE_WAIT;
        downloadStatus.postValue(new AbstractMap.SimpleEntry<>(this.currentState, this.progress));
        resetEntryFile();
    }

    void onDownloading(int progress) {
        boolean b = this.currentState != STATE_DOWNING;
        this.currentState = STATE_DOWNING;
        this.progress = progress;
        downloadStatus.postValue(new AbstractMap.SimpleEntry<>(this.currentState, this.progress));
        if (b) {
            resetEntryFile();
        }
    }

    void onPause() {
        this.currentState = STATE_PAUSED;
        downloadStatus.postValue(new AbstractMap.SimpleEntry<>(this.currentState, this.progress));
        resetEntryFile();
    }

    void onError() {
        this.currentState = STATE_ERROR;
        downloadStatus.postValue(new AbstractMap.SimpleEntry<>(this.currentState, this.progress));
        resetEntryFile();
    }

    void onCompleted() {
        this.currentState = STATE_COMPLETED;
        this.progress = 100;
        downloadStatus.postValue(new AbstractMap.SimpleEntry<>(this.currentState, this.progress));
        resetEntryFile();
    }

    private void resetEntryFile() {
        try {
            FileOutputStream opt = new FileOutputStream(entryFile);
            opt.write(String.format(FILE_FORMAT, this.currentState, this.progress).getBytes());
            opt.flush();
            opt.close();
        } catch (Exception e) {
            SLog.w(TAG, "resetEntryFile Exception" + entryFile.getAbsolutePath(), e);
        }
    }

    public int getCurrentState() {
        return currentState;
    }

    public int getProgress() {
        return progress;
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    public void observeDownloadState(LifecycleOwner owner, Observer<Map.Entry<Integer, Integer>> observer) {
        downloadStatus.observe(owner, observer);
    }

    public void removeDownloadStateObserver(Observer<Map.Entry<Integer, Integer>> observer) {
        downloadStatus.removeObserver(observer);
    }

    File getSaveDir() {
        return saveDir;
    }

    void onDelete() {
        this.currentState = STATE_DELETED;
        this.progress = 0;
        downloadStatus.postValue(new AbstractMap.SimpleEntry<>(this.currentState, this.progress));
        entryFile.delete();
    }

    public String getChannelId() {
        return channelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadEntry entry = (DownloadEntry) o;
        return channelId.equals(entry.channelId);
    }

    @Override
    public int hashCode() {
        return channelId.hashCode();
    }

    @Override
    public String toString() {
        return "DownloadEntry{" +
                "channelId='" + channelId + '\'' +
                ", entryFile=" + entryFile +
                ", currentState=" + currentState +
                '}';
    }
}

package com.streambus.commonmodule.download;

import android.content.Context;

import com.streambus.basemodule.utils.SLog;
import com.streambus.basemodule.utils.SimpleCall;
import com.streambus.commonmodule.api.RequestApi;
import com.streambus.commonmodule.bean.ChannelVodBean;
import com.streambus.commonmodule.bean.LinksBean;
import com.streambus.commonmodule.login.MyAppLogin;
import com.streambus.commonmodule.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.schedulers.SingleScheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/7/30
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class VideoDownloadManager {
    private static final String TAG = "VideoDownloadManager";
    private static VideoDownloadManager INSTANCE;
    private static int sMaxDownloadSize = 1;
    private volatile int mDownloadCount;

    private static Scheduler SINGLE = new SingleScheduler();

    private Context mContext;
    private final File mRootDir;

    private Map<String,DownloadEntry> mDownloadEntryMap = new LinkedHashMap<>();
    private LinkedList<DownloadEntry> mWaitDownloadEntryList = new LinkedList<>();

    public static VideoDownloadManager getInstance() {
        return INSTANCE;
    }

    public static void setup(Context application) {
        INSTANCE = new VideoDownloadManager(application);
    }

    private VideoDownloadManager(Context application) {
        mContext = application;
        mRootDir = new File(mContext.getExternalCacheDir(), "VideoDownload");
        mRootDir.mkdirs();
        checkDownload();
    }

    private void checkDownload() {
        SINGLE.scheduleDirect(new Runnable() {
            @Override
            public void run() {
                TreeSet<String> names = new TreeSet<>(Arrays.asList(mRootDir.list()));
                for (String name : names) {
                    File dir = new File(mRootDir, name);
                    DownloadEntry downloadEntry = new DownloadEntry(dir);
                    if (downloadEntry.getDownloadInfo() == null) {
                        FileUtils.deleteFiles(dir);
                    } else {
                        mDownloadEntryMap.put(downloadEntry.getChannelId(), downloadEntry);
                    }
                }
            }
        });
    }


    public List<DownloadEntry> getDownloadChannel() {
        return new ArrayList<>(mDownloadEntryMap.values());
    }

    public Single<Boolean> addDownloadVideo(ChannelVodBean bean, LinksBean link) {
        return Single.just(0).map(new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) throws Exception {
                String url = link.getHttpurl();
                String relativePath = url.substring(url.lastIndexOf("/") + 1);
                File saveDir = new File(mRootDir, MyAppLogin.getInstance().getUtcTime() + "_" + relativePath);
                saveDir.mkdirs();
                DownloadEntry downloadEntry = new DownloadEntry(saveDir, new DownloadInfo(bean, link, saveDir.getAbsolutePath() + "/" + relativePath));
                if (downloadEntry.getDownloadInfo() == null) {
                    FileUtils.deleteFiles(saveDir);
                    return false;
                }
                mDownloadEntryMap.put(downloadEntry.getChannelId(), downloadEntry);
                mWaitDownloadEntryList.add(downloadEntry);
                drain();
                return true;
            }
        }).subscribeOn(SINGLE).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> removeDownloadVideo(DownloadEntry entry) {
        return Single.just(0).map(new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) throws Exception {
                mWaitDownloadEntryList.remove(entry);
                if (entry.disposable != null) {
                    entry.disposable.dispose();
                }
                entry.onDelete();
                mDownloadEntryMap.remove(entry.getChannelId());
                return true;
            }
        }).subscribeOn(SINGLE).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> removeAllDownloadVideo() {
        return Single.just(0).map(new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) throws Exception {
                mWaitDownloadEntryList.clear();
                for (DownloadEntry entry : mDownloadEntryMap.values()) {
                    if (entry.disposable != null) {
                        entry.disposable.dispose();
                    }
                    entry.onDelete();
                }
                mDownloadEntryMap.clear();
                return true;
            }
        }).subscribeOn(SINGLE).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> pauseDownloadVideo(DownloadEntry entry) {
        return Single.just(0).map(new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) throws Exception {
                mWaitDownloadEntryList.remove(entry);
                if (entry.disposable != null) {
                    entry.disposable.dispose();
                }
                entry.onPause();
                return true;
            }
        }).subscribeOn(SINGLE).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> resumeDownloadVideo(DownloadEntry entry) {
        return Single.just(0).map(new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer integer) throws Exception {
                entry.onWait();
                mWaitDownloadEntryList.add(entry);
                drain();
                return true;
            }
        }).subscribeOn(SINGLE).observeOn(AndroidSchedulers.mainThread());
    }

    private void drain() {
        SINGLE.scheduleDirect(new Runnable() {
            @Override
            public void run() {
                while (mDownloadCount < sMaxDownloadSize && !mWaitDownloadEntryList.isEmpty()) {
                    DownloadEntry downloadEntry = mWaitDownloadEntryList.removeFirst();
                    downloadEntry.disposable = downloadVideo(downloadEntry)
                            .observeOn(SINGLE)
                            .doOnSubscribe(disposable -> {
                                mDownloadCount++;
                                downloadEntry.onDownloading(downloadEntry.getProgress());
                            })
                            .doFinally(() -> {
                                mDownloadCount--;
                                drain();
                            })
                            .subscribe(new Consumer<Integer>() {
                                @Override
                                public void accept(Integer progress) throws Exception {
                                    SLog.i(TAG, "downloadVideo progress=>" + progress);
                                    if (downloadEntry.getCurrentState() == DownloadEntry.STATE_DOWNING) {
                                        if (progress < 100) {
                                            downloadEntry.onDownloading(progress);
                                        } else {
                                            downloadEntry.onCompleted();
                                        }
                                    }
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    SLog.e(TAG, "downloadVideo throwable", throwable);
                                    if (downloadEntry.getCurrentState() == DownloadEntry.STATE_DOWNING) {
                                        downloadEntry.onError();
                                    }
                                }
                            }, new Action() {
                                @Override
                                public void run() throws Exception {
                                    SLog.i(TAG, "downloadVideo completed CurrentState=>" + downloadEntry.getCurrentState());
                                    if (downloadEntry.getCurrentState() != DownloadEntry.STATE_COMPLETED) {
                                        downloadEntry.onCompleted();
                                    }
                                }
                            });
                }
            }
        });
    }

    private Observable<Integer> downloadVideo(DownloadEntry downloadEntry) {
        return Observable.defer(new Callable<ObservableSource<? extends Integer>>() {
            @Override
            public ObservableSource<? extends Integer> call() throws Exception {
                if (M3u8DownloadUtils.sDomainMapping != null) {
                    return downloadVideoObservable;
                }
                return RequestApi.requestDownloadDomainMapping().flatMap(new Function<Map<String, String>, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(Map<String, String> stringStringMap) throws Exception {
                        M3u8DownloadUtils.sDomainMapping = stringStringMap;
                        return downloadVideoObservable;
                    }
                });
            }

            Observable<Integer> downloadVideoObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
                private volatile boolean isCancel;
                private SimpleCall call;

                private void cancel() throws Exception {
                    synchronized (this) {
                        isCancel = true;
                        if (call != null) {
                            call.cancel();
                        }
                    }
                }

                @Override
                public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                    emitter.setCancellable(this::cancel);
                    String url = downloadEntry.getDownloadInfo().getLinkBean().getHttpurl();
                    int protocol = downloadEntry.getDownloadInfo().getLinkBean().protocolVersion();
                    SLog.d(TAG, "start addDownloadVideo url=>" + url);
                    File saveDir = downloadEntry.getSaveDir();
                    synchronized (this) {
                        if (isCancel) {
                            throw new CancellationException("downloadVideo");
                        }
                        call = M3u8DownloadUtils.newParsM3u8Call(saveDir, url, protocol);
                    }
                    List<Map.Entry<String, String>> downloadTsList = ((SimpleCall<List<Map.Entry<String, String>>>) call).call();
                    Collections.sort(downloadTsList, new Comparator<Map.Entry<String, String>>() {
                        @Override
                        public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                            String key1 = o1.getKey();
                            String index1 = key1.substring(key1.lastIndexOf("_") + 1, key1.lastIndexOf("."));
                            String key2 = o2.getKey();
                            String index2 = key2.substring(key2.lastIndexOf("_") + 1, key2.lastIndexOf("."));
                            return Integer.valueOf(index1).compareTo(Integer.valueOf(index2));
                        }
                    });
                    int count = 0;
                    for (Map.Entry<String, String> entry : downloadTsList) {
                        File file = new File(saveDir, entry.getKey());
                        synchronized (this) {
                            if (isCancel) {
                                throw new CancellationException("downloadVideo");
                            }
                            call = M3u8DownloadUtils.newDownloadCall(file, entry.getValue(), protocol, true);
                        }
                        if (!((SimpleCall<Boolean>) call).call()) {
                            throw new IllegalStateException("downloadTsList failed，entry=>" + entry);
                        }
                        emitter.onNext(++count * 100 / downloadTsList.size());
                    }
                    emitter.onComplete();
                }
            }).subscribeOn(Schedulers.io());
        });
    }

    public boolean hasDownload(String id) {
        return mDownloadEntryMap.containsKey(id);
    }
}

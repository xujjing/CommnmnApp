package com.streambus.commonmodule.upgrade2;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.streambus.basemodule.utils.PreferencesUtils;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.upgrade2.bean.UpgradeBean2;
import com.streambus.commonmodule.upgrade2.bean.UploadInfo2;
import com.streambus.commonmodule.upgrade2.view.UpgradeDialog2;
import com.streambus.commonmodule.utils.AESUtil;
import com.streambus.commonmodule.utils.AppUtil;
import com.streambus.commonmodule.utils.GsonHelper;
import com.streambus.commonmodule.utils.Md5Util;
import com.streambus.requestapi.OkHttpHelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/2/25
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class UpgradeManager {
    private static final String Check_Upgrade_PATH = "https://appupd.aegis-corporate.com/app_manager/check_upgrade";
    public static final String Get_Appinfo_PATH = "https://appupd.aegis-corporate.com/app_manager/appinfo";
    private static final String TAG = "UpgradeManager";
    private static UpgradeManager sUpgradeManager;
    private final Application mContext;
    private final UploadInfo2 mUploadInfo;
    private final File mUpgradeDir;
    private Disposable mCheckUpgradeDisposable;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Map.Entry<String, DownloadTask> mApkDownLoadEntry;
    private PublishSubject<UpgradeBean2> mApkUpgradeSubject = PublishSubject.create();
    public static UpgradeManager getInstance() {
        return sUpgradeManager;
    }

    public static void setup(Application context) {
        sUpgradeManager = new UpgradeManager(context);
    }

    private UpgradeManager(Application context) {
        mContext = context;
        mUploadInfo = new UploadInfo2(context);
        mUpgradeDir = new File(context.getExternalCacheDir(), "/ApkUpgrade");
        if (mUpgradeDir.exists() && mUpgradeDir.isFile()) {
            mUpgradeDir.delete();
        }
        if (!mUpgradeDir.exists()) {
            mUpgradeDir.mkdirs();
        }
    }

    private Runnable mCheckUpgradeRunnable = new Runnable() {
        @Override
        public void run() {
            checkUpgrade(false);
        }
    };


    public Maybe<UpgradeBean2> subjectApkUpgrade() {
        return mApkUpgradeSubject.firstElement();
    }

    public void checkUpgrade(boolean cleanBehavior){
        if (cleanBehavior) {
            mUploadInfo.setVersion(AppUtil.getAppVersionName(mContext));
            mUploadInfo.setVersionCode(AppUtil.getVersionCode(mContext));
            PreferencesUtils.put(Constants.KEY_UPGRADE_IGNORE_VERSION_CODE, (Serializable) null);
        }
        if (mCheckUpgradeDisposable != null && !mCheckUpgradeDisposable.isDisposed()) {
            mCheckUpgradeDisposable.dispose();
        }
        mHandler.removeCallbacks(mCheckUpgradeRunnable);
        mCheckUpgradeDisposable = Observable.create(new ObservableOnSubscribe<UpgradeBean2>() {
            @Override
            public void subscribe(ObservableEmitter<UpgradeBean2> emitter) throws Exception {
                String json = GsonHelper.toJson(mUploadInfo.updateAccountInfo());
                String encodeData = AESUtil.encryptBase64(json, Constants.PPMGR_PERFCOS_AES_KEY);
                SLog.d(TAG, "requestUpgrade request json=>" + json + "   encodeData=>" + encodeData);
                Request request = new Request.Builder().addHeader(OkHttpHelper.HEADER_NO_ENCRYPTION, OkHttpHelper.HEADER_NO_ENCRYPTION)
                        .url(Check_Upgrade_PATH).post(RequestBody.create(MediaType.parse("text/plain"), encodeData)).build();
                Call call = OkHttpHelper.getCmsClient().newCall(request);
                emitter.setCancellable(() -> call.cancel());
                Response response = call.execute();
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    String decodeData = AESUtil.decryptBase64(data, Constants.PPMGR_PERFCOS_AES_KEY);
                    SLog.d(TAG, "requestUpgrade response data=>" + data + "   decodeData=>" + decodeData);
                    emitter.onNext(GsonHelper.toType(decodeData, UpgradeBean2.class));
                    emitter.onComplete();
                } else {
                    emitter.onError(new IllegalStateException("requestUpgrade http response code=" + response.code()));
                }
            }}) .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<UpgradeBean2>() {
                    @Override
                    public void accept(UpgradeBean2 bean) throws Exception {
                        mApkUpgradeSubject.onNext(bean);
                        handleUpgrade(bean);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        SLog.e(TAG, "checkUpgrade throwable", throwable);
                        mApkUpgradeSubject.onError(throwable);
                        mHandler.postDelayed(mCheckUpgradeRunnable, 10*60*1000);
                    }
                });
    }

    public void remindCheckIgnore(UpgradeBean2 bean, boolean ignore) {
        if (!ignore) {
            startApkCommonUpgrade(bean);
            UpgradeDialog2.showUpgrade(bean, UpgradeDialog2.VALUE_ACTION_APK_COMMON);
            return;
        }
        PreferencesUtils.put(Constants.KEY_UPGRADE_IGNORE_VERSION_CODE, bean.getVersionCode());
        mHandler.postDelayed(mCheckUpgradeRunnable, 30*60*1000);
    }

    public DownloadTask getDownloadTask(String md5) {
        if (mApkDownLoadEntry != null && mApkDownLoadEntry.getKey().equals(md5)) {
            return mApkDownLoadEntry.getValue();
        }
        return null;
    }

    private void handleUpgrade(UpgradeBean2 bean) {
        if (bean.getResult() == 1) {
            if (bean.getType() == 0) { //处理APK更新
                if (bean.getUpgradeType() == 1) {
                    startApkFocusUpgrade(bean);
                    return;
                }
                if (bean.getUpgradeType() == 2) {
                    startApkCommonUpgrade(bean);
                    return;
                }
                int ignoreVersion = PreferencesUtils.get(Constants.KEY_UPGRADE_IGNORE_VERSION_CODE, 0);
                if (ignoreVersion != bean.getVersionCode()) {
                    UpgradeDialog2.showUpgrade(bean, UpgradeDialog2.VALUE_ACTION_APK_REMIND);
                    return;
                }
            } else if (bean.getType() == 1) {//处理补丁更新
                //TODO
            }
        }
        //如果没有处理，隔30分钟再次进行更新检查
        mHandler.postDelayed(mCheckUpgradeRunnable, 30*60*1000);
    }

    /**
     * 启动强制升级
     * @param bean
     */
    private void startApkFocusUpgrade(UpgradeBean2 bean) {
        downloadApk(bean);
        UpgradeDialog2.showUpgrade(bean, UpgradeDialog2.VALUE_ACTION_APK_FOCUS);
    }

    /**
     * 启动常规升级
     * @param bean
     */
    private void startApkCommonUpgrade(UpgradeBean2 bean) {
        downloadApk(bean);
        mApkDownLoadEntry.getValue().statusLiveData.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == DownloadTask.SUCCESS) {
                    UpgradeDialog2.showUpgrade(bean, UpgradeDialog2.VALUE_ACTION_APK_COMMON);
                }
            }
        });
        if (mApkUpgradeSubject.hasObservers()) {
            UpgradeDialog2.showUpgrade(bean, UpgradeDialog2.VALUE_ACTION_APK_COMMON);
        }
    }


    /**
     * 下载APK文件
     * @param bean
     */
    public void downloadApk(UpgradeBean2 bean) {
        if (mApkDownLoadEntry != null && mApkDownLoadEntry.getKey() == bean.getMd5()) {
            DownloadTask downloadTask = mApkDownLoadEntry.getValue();
            if (downloadTask.statusLiveData.getValue() != DownloadTask.ERROR) {
                return;
            }
        }
        if (mApkDownLoadEntry != null) {
            mApkDownLoadEntry.getValue().disposable.dispose();
        }
        DownloadTask downloadTask = new DownloadTask(mUpgradeDir, bean.getUrl(), bean.getMd5());
        downloadTask.statusLiveData.observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == DownloadTask.SUCCESS) {
                    mUploadInfo.setVersion(bean.getVersion());
                    mUploadInfo.setVersionCode(bean.getVersionCode());
                    mHandler.postDelayed(mCheckUpgradeRunnable, 30*60*1000);
                } else if (integer == DownloadTask.ERROR) {
                    mHandler.postDelayed(mCheckUpgradeRunnable, 10*60*1000);
                }
            }
        });
        downloadTask.startDownLoad();
        mApkDownLoadEntry = new AbstractMap.SimpleEntry<>(bean.getMd5(), downloadTask);
    }

    public static class DownloadTask{
        public static final int START = 1;
        public static final int DOING = 2;
        public static final int SUCCESS = 3;
        public static final int ERROR = 4;
        public MutableLiveData<Integer> statusLiveData = new MutableLiveData();

        public File downLoadFile;
        private File upgradeDir;
        private String url;
        private String md5;
        private Disposable disposable;

        public long contentLength;
        public long downloadLength = 0;

        private DownloadTask(File upgradeDir, String url, String md5) {
            this.upgradeDir = upgradeDir;
            this.url = url;
            this.md5 = md5;
        }

        public void startDownLoad() {
            disposable = Schedulers.newThread().scheduleDirect(runnable);
        }

        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    downLoadFile = new File(upgradeDir, "apk_" + md5 + ".apk");
                    if (downLoadFile.exists() && md5.equals(Md5Util.getFileMD5String(downLoadFile))) {
                        statusLiveData.postValue(SUCCESS);
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                downLoadFile.delete();
                statusLiveData.postValue(START);
                File temFile = new File(upgradeDir, "tem_apk_" + md5 + ".apk");
                Response response = null;
                OutputStream outputStream = null;
                try {
                    Request request = new Request.Builder().addHeader("FILE", "FILE").url(url).build();
                    response = OkHttpHelper.getOkHttpClient().newCall(request).execute();
                    if (response.isSuccessful()) {
                        contentLength = response.body().contentLength(); //获取文件总大小
                        if (!"bytes".equals(response.header("Accept-Ranges"))) {//如果不支持断点下载
                            temFile.delete();
                        } else if (temFile.exists()) {//如果支持断点下载
                            response.close();//关闭之前文件长度的请求，重新新开从断点请求
                            downloadLength = temFile.length();
                            request = new Request.Builder().url(url).addHeader("FILE", "FILE").addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength).build();
                            response = OkHttpHelper.getOkHttpClient().newCall(request).execute();
                            if (!response.isSuccessful()) {
                                throw new IllegalStateException("");
                            }
                        }
                        statusLiveData.postValue(DOING);
                        InputStream ips = response.body().byteStream();
                        outputStream = new BufferedOutputStream(new FileOutputStream(temFile, true));
                        int len;
                        byte[] buff = new byte[1024];
                        while ((len = ips.read(buff)) != -1) {
                            outputStream.write(buff, 0, len);
                            downloadLength += len;
                        }
                        response.close();
                        response = null;
                        outputStream.flush();
                        outputStream.close();
                        outputStream = null;
                        if (md5.equals(Md5Util.getFileMD5String(temFile))) {
                            temFile.renameTo(downLoadFile);
                        } else {
                            temFile.delete();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (response != null) {
                        response.close();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        if (downLoadFile.exists() && md5.equals(Md5Util.getFileMD5String(downLoadFile))){
                            statusLiveData.postValue(SUCCESS);
                        } else {
                            downLoadFile.delete();
                            statusLiveData.postValue(ERROR);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }



    private UpgradeBean2 mUpgradeBean2;
    private PublishSubject<UpgradeBean2> mAppInfoSubject = PublishSubject.create();
    public Observable<String> getAppDownUrl() {
        UpgradeBean2 upgradeBean2 = mUpgradeBean2;
        if (upgradeBean2 != null && !TextUtils.isEmpty(upgradeBean2.getUrl())) {
            upLoadAppInfo();
            return Observable.just(upgradeBean2.getUrl());
        }
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Disposable disposable = mAppInfoSubject.subscribe(new Consumer<UpgradeBean2>() {
                    @Override
                    public void accept(UpgradeBean2 upgradeBean2) throws Exception {
                        emitter.onNext(upgradeBean2.getUrl());
                        emitter.onComplete();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        emitter.onError(throwable);
                    }
                });
                emitter.setDisposable(disposable);
                upLoadAppInfo();
            }
        });
//         .onErrorResumeNext(new Function<Throwable, ObservableSource<String>>() {
//            @Override
//            public ObservableSource<String> apply(Throwable throwable) throws Exception {
//                SLog.w(TAG, "getAppDownUrl", throwable);
//                return Observable.just("https://default/download");
//            }
//        });
    }

    private Disposable mUpLoadAppInfoDisposable;
    public void upLoadAppInfo() {
        if (mUpLoadAppInfoDisposable != null) {
            return;
        }
        mUpLoadAppInfoDisposable = Maybe.create(new MaybeOnSubscribe<UpgradeBean2>() {
            @Override
            public void subscribe(MaybeEmitter<UpgradeBean2> emitter) throws Exception {
                //packageName ...将来盒子版本和手机版本不是同一个apk
                UploadInfo2 updateInfo = new UploadInfo2(mContext);
                String json = GsonHelper.toJson(updateInfo.updateAccountInfo());
                String encodeData = AESUtil.encryptBase64(json, Constants.PPMGR_PERFCOS_AES_KEY);
                SLog.d(TAG, "requestUpgrade request json=>" + json + "   encodeData=>" + encodeData);
                Request request = new Request.Builder().addHeader(OkHttpHelper.HEADER_NO_ENCRYPTION, OkHttpHelper.HEADER_NO_ENCRYPTION)
                        .url(Get_Appinfo_PATH).post(RequestBody.create(MediaType.parse("text/plain"), encodeData)).build();
                Call call = OkHttpHelper.getCmsClient().newCall(request);
                Response response = call.execute();
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    SLog.d(TAG, "upLoadAppInfo data=>" + data);
                    String decodeData = AESUtil.decryptBase64(data, Constants.PPMGR_PERFCOS_AES_KEY);
                    SLog.d(TAG, "getUpgradeBeanBlock response data=>" + data + "   decodeData=>" + decodeData);
                    emitter.onSuccess(GsonHelper.toType(decodeData, UpgradeBean2.class));
                } else {
                    SLog.d(TAG, "getUpgradeBeanBlock response response.code=>" + response.code());
                    emitter.onError(new IllegalStateException("response.code=" + response.code()));
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        mUpLoadAppInfoDisposable = null;
                    }
                })
                .subscribe(new Consumer<UpgradeBean2>() {
                    @Override
                    public void accept(UpgradeBean2 upgradeBean2) throws Exception {
                        mUpgradeBean2 = upgradeBean2;
                        mAppInfoSubject.onNext(upgradeBean2);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        SLog.w(TAG, "upLoadAppInfo", throwable);
                        mAppInfoSubject.onError(new IllegalStateException("upLoadAppInfo=>" + throwable.getMessage()));
                    }
                });

    }


    private Map<String, BehaviorSubject<UpgradeBean2>> mAppInfoSubjectMap = new HashMap<>();
    public Single<String> getOthersAppDownUrl(String packageName) {
        BehaviorSubject<UpgradeBean2> subject = mAppInfoSubjectMap.get(packageName);
        if (subject == null) {
            BehaviorSubject<UpgradeBean2> finalSubject = BehaviorSubject.create();
            mAppInfoSubjectMap.put(packageName, subject = finalSubject);
            Disposable disposable = Single.just(packageName)
                    .map(new Function<String, UpgradeBean2>() {
                        @Override
                        public UpgradeBean2 apply(String packageName) throws Exception {
                            UploadInfo2 updateInfo = new UploadInfo2(mContext);
                            updateInfo.setPkg(packageName); updateInfo.setVersion("0"); updateInfo.setVersionCode(0);
                            String json = GsonHelper.toJson(updateInfo.updateAccountInfo());
                            String encodeData = AESUtil.encryptBase64(json, Constants.PPMGR_PERFCOS_AES_KEY);
                            SLog.d(TAG, "requestUpgrade request json=>" + json + "   encodeData=>" + encodeData);
                            Request request = new Request.Builder().addHeader(OkHttpHelper.HEADER_NO_ENCRYPTION, OkHttpHelper.HEADER_NO_ENCRYPTION)
                                    .url(Check_Upgrade_PATH).post(RequestBody.create(MediaType.parse("text/plain"), encodeData)).build();
                            Call call = OkHttpHelper.getCmsClient().newCall(request);
                            Response response = call.execute();
                            if (!response.isSuccessful()) {
                                throw new IllegalStateException("requestUpgrade http response code=" + response.code());
                            }
                            String data = response.body().string();
                            String decodeData = AESUtil.decryptBase64(data, Constants.PPMGR_PERFCOS_AES_KEY);
                            SLog.d(TAG, "requestUpgrade response data=>" + data + "   decodeData=>" + decodeData);
                            return GsonHelper.toType(decodeData, UpgradeBean2.class);
                        }
                    }).subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<UpgradeBean2>() {
                        @Override
                        public void accept(UpgradeBean2 upgradeBean2) throws Exception {
                            finalSubject.onNext(upgradeBean2);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            finalSubject.onError(throwable);
                        }
                    });
        }
        BehaviorSubject<UpgradeBean2> finalSubject = subject;
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                Disposable disposable = finalSubject.subscribe(new Consumer<UpgradeBean2>() {
                    @Override
                    public void accept(UpgradeBean2 upgradeBean2) throws Exception {
                        emitter.onSuccess(upgradeBean2.getUrl());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mAppInfoSubjectMap.remove(packageName);
                        emitter.onError(throwable);
                    }
                });
            }
        });
    }
}

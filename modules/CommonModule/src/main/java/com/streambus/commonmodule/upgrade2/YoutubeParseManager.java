//package com.streambus.commonmodule.upgrade2;
//
//import android.app.Application;
//import android.text.TextUtils;
//
//import com.streambus.basemodule.utils.PreferencesUtils;
//import com.streambus.basemodule.utils.SLog;
//import com.streambus.commonmodule.Constants;
//import com.streambus.commonmodule.upgrade2.bean.UpgradeBean2;
//import com.streambus.commonmodule.upgrade2.bean.UploadInfo2;
//import com.streambus.commonmodule.utils.AESUtil;
//import com.streambus.commonmodule.utils.FileUtils;
//import com.streambus.commonmodule.utils.GsonHelper;
//import com.streambus.commonmodule.utils.Md5Util;
//import com.streambus.commonmodule.utils.YoutubeParseUtils;
//import com.streambus.requestapi.OkHttpHelper;
//
//import org.json.JSONObject;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.Serializable;
//import java.lang.reflect.Method;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.concurrent.Callable;
//import java.util.concurrent.Future;
//
//import dalvik.system.DexClassLoader;
//import io.reactivex.Maybe;
//import io.reactivex.MaybeEmitter;
//import io.reactivex.MaybeOnSubscribe;
//import io.reactivex.MaybeSource;
//import io.reactivex.Observable;
//import io.reactivex.ObservableEmitter;
//import io.reactivex.ObservableOnSubscribe;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.functions.Cancellable;
//import io.reactivex.functions.Consumer;
//import io.reactivex.functions.Predicate;
//import io.reactivex.schedulers.Schedulers;
//import okhttp3.Call;
//import okhttp3.MediaType;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
///**
// * ================================================
// * 作    者：xujianjing
// * 版    本：v1.0
// * 创建日期：2021/6/18
// * 描    述:
// * 修订历史：
// * ================================================
// */
//public class YoutubeParseManager {
//    private static final String Check_Upgrade_PATH = "https://appupd.aegis-corporate.com/app_manager/check_upgrade";
//    private static final String TAG = "YoutubeParseManager";
//    private static final String KEY_YOUTUBEPARSE_VERSION = "key_youtubeparse_version";
//    private static final String KEY_YOUTUBEPARSE_VERSIONCODE = "key_youtubeparse_versioncode";
//    private final Application mContext;
//    private final UploadInfo2 mUploadInfo;
//
//    private final Object mDexLimitLock = new Object();
//
//
//    private static YoutubeParseManager INSTANCE;
//    private final File mDexUpgradeDir;
//    private final File mYoutubeDexDir;
//
//    public static YoutubeParseManager getInstance() {
//        return INSTANCE;
//    }
//
//    public static void setup(Application context) {
//        INSTANCE = new YoutubeParseManager(context);
//    }
//
//    private static final File makeDir(File dir) {
//        if (dir.exists() && dir.isFile()) {
//            dir.delete();
//        }
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//        return dir;
//    }
//    private YoutubeParseManager(Application context) {
//        mContext = context;
//        mUploadInfo = new UploadInfo2(context);
//        mUploadInfo.setChannelCode("default");
//        mDexUpgradeDir = makeDir(new File(context.getCacheDir(), "/TemUpgrade"));
//        mYoutubeDexDir = makeDir(new File(context.getFilesDir(),"YoutubeParse"));
//    }
//
//    public void startLoad() {
//        SLog.d(TAG,"startLoad");
//        File dexDir = new File(mYoutubeDexDir, "V" + PreferencesUtils.get(KEY_YOUTUBEPARSE_VERSIONCODE, 0));
//        for (File temDir : mYoutubeDexDir.listFiles()) {
//            if (!temDir.equals(dexDir)) {
//                FileUtils.deleteFiles(temDir);
//            }
//        }
//        if (dexDir.exists() && !tryLoadDex(dexDir)) {
//            FileUtils.deleteFiles(dexDir);
//            SLog.d(TAG,"startLoad deleteFiles");
//        }
//        if (mDexDir == null) {
//            PreferencesUtils.put(KEY_YOUTUBEPARSE_VERSION, (Serializable) null);
//            PreferencesUtils.put(KEY_YOUTUBEPARSE_VERSIONCODE, (Serializable) null);
//        }
//
//        mUploadInfo.setPkg("com.streambus.ytblib");
//        mUploadInfo.setVersion(PreferencesUtils.get(KEY_YOUTUBEPARSE_VERSION, "0"));
//        mUploadInfo.setVersionCode(PreferencesUtils.get(KEY_YOUTUBEPARSE_VERSIONCODE, 0));
//        checkUpYoutubeParse();
//    }
//
//    private Disposable mCheckUpgradeDisposable;
//    private void checkUpYoutubeParse() {
//        SLog.d(TAG,"checkUpYoutubeParse mCheckUpgradeDisposable mCheckUpgradeDisposable");
//        if (mCheckUpgradeDisposable != null && !mCheckUpgradeDisposable.isDisposed()) {
//            return;
//        }
//        mCheckUpgradeDisposable = Observable.create(new ObservableOnSubscribe<UpgradeBean2>() {
//                    @Override
//                    public void subscribe(ObservableEmitter<UpgradeBean2> emitter) throws Exception {
//                        String json = GsonHelper.toJson(mUploadInfo.updateAccountInfo());
//                        String encodeData = AESUtil.encryptBase64(json, Constants.PPMGR_PERFCOS_AES_KEY);
//                        SLog.d(TAG, "checkUpYoutubeParse request json=>" + json + "   encodeData=>" + encodeData);
//                        Request request = new Request.Builder().addHeader(OkHttpHelper.HEADER_NO_ENCRYPTION, OkHttpHelper.HEADER_NO_ENCRYPTION)
//                                .url(Check_Upgrade_PATH).post(RequestBody.create(MediaType.parse("text/plain"), encodeData)).build();
//                        Call call = OkHttpHelper.getCmsClient().newCall(request);
//                        emitter.setCancellable(() -> call.cancel());
//                        Response response = call.execute();
//                        if (response.isSuccessful()) {
//                            String data = response.body().string();
//                            String decodeData = AESUtil.decryptBase64(data, Constants.PPMGR_PERFCOS_AES_KEY);
//                            SLog.d(TAG, "checkUpYoutubeParse response data=>" + data + "   decodeData=>" + decodeData);
//                            emitter.onNext(GsonHelper.toType(decodeData, UpgradeBean2.class));
//                            emitter.onComplete();
//                        } else {
//                            emitter.onError(new IllegalStateException("checkUpYoutubeParse http response code=" + response.code()));
//                        }
//                }}).filter(new Predicate<UpgradeBean2>() {
//                    @Override
//                    public boolean test(UpgradeBean2 bean) throws Exception {
//                        SLog.i(TAG, "checkUpYoutubeParse filter UpgradeBean2 getResult=" + bean.getResult() + "  getType=" + bean.getType());
//                        return bean.getResult() == 1 && bean.getType() == 0;
//                    }
//                }).subscribeOn(Schedulers.io())
//                .subscribe(new Consumer<UpgradeBean2>() {
//                    @Override
//                    public void accept(UpgradeBean2 upgradeBean2) throws Exception {
//                        SLog.i(TAG, "checkUpYoutubeParse subscribe start downLoad file");
//                        File downFile = new DownloadTask(mDexUpgradeDir, upgradeBean2.getUrl(), upgradeBean2.getMd5()).downLoad();
//                        SLog.i(TAG, "checkUpYoutubeParse subscribe downLoad file=" + downFile);
//                        if (downFile == null) {
//                            throw new NullPointerException("downLoad file is null");
//                        }
//                        mUploadInfo.setVersion(upgradeBean2.getVersion());
//                        mUploadInfo.setVersionCode(upgradeBean2.getVersionCode());
//
//                        File dexDir = makeDir(new File(mYoutubeDexDir, "V" + upgradeBean2.getVersionCode()));
//                        if (!downFile.renameTo(new File(dexDir, "classes.dex"))) {
//                            throw new IllegalStateException("downFile.renameTo dexFile failed");
//                        }
//                        if (tryLoadDex(dexDir)) {
//                            PreferencesUtils.put(KEY_YOUTUBEPARSE_VERSION, upgradeBean2.getVersion());
//                            PreferencesUtils.put(KEY_YOUTUBEPARSE_VERSIONCODE, upgradeBean2.getVersionCode());
//                        } else {
//                            FileUtils.deleteFiles(dexDir);
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        SLog.e(TAG, "checkUpYoutubeParse throwable", throwable);
//                    }
//                });
//    }
//
//    private File mDexDir;
//    private Class mClazz;
//    private Method mParseIDfMethod;
//    private Method mParseUrlMethod;
//    private boolean tryLoadDex(File dexDir) {
//        SLog.i(TAG, "tryLoadDex dexDir=" + dexDir);
//        Class clazz = null;
//        Method parseIDfMethod = null;
//        Method parseUrlMethod = null;
//        try {
//            DexClassLoader cl = new DexClassLoader(new File(dexDir, "classes.dex").getAbsolutePath(), dexDir.getAbsolutePath(),
//                    dexDir.getAbsolutePath(), mContext.getClassLoader());
//            // 载入JarLoader类， 并且通过反射构建JarLoader对象， 然后调用sayHi方法
//            clazz = cl.loadClass("com.streambus.ytblib.YoutubeParse");  //这里要用类的完整名称
//            parseIDfMethod = clazz.getMethod("parseIDfromVideoUrl", String.class);
//            parseUrlMethod = clazz.getMethod("parseUrl", String.class, boolean.class, String[].class);
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
//        if (clazz != null && parseIDfMethod != null && parseUrlMethod != null) {
//            synchronized (mDexLimitLock) {
//                mClazz = clazz;
//                mParseIDfMethod = parseIDfMethod;
//                mParseUrlMethod = parseUrlMethod;
//                SLog.i(TAG, "tryLoadDex mDexDir=" + mDexDir);
//                if (mDexDir != null) {
//                    FileUtils.deleteFiles(mDexDir);
//                }
//                mDexDir = dexDir;
//            }
//            return true;
//        }
//        return false;
//    }
//
//    private static class DownloadTask{
//        private File downLoadFile;
//        private File upgradeDir;
//        private String url;
//        private String md5;
//
//        public long contentLength;
//        public long downloadLength = 0;
//
//        private DownloadTask(File upgradeDir, String url, String md5) {
//            this.upgradeDir = upgradeDir;
//            this.url = url;
//            this.md5 = md5;
//        }
//
//        public File downLoad() throws Exception{
//            try {
//                downLoadFile = new File(upgradeDir, "dex_" + md5 + ".dex");
//                if (downLoadFile.exists() && md5.equals(Md5Util.getFileMD5String(downLoadFile))) {
//                    return downLoadFile;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            downLoadFile.delete();
//            File temFile = new File(upgradeDir, "tem_dex_" + md5 + ".dex");
//            Response response = null;
//            OutputStream outputStream = null;
//            try {
//                Request request = new Request.Builder().addHeader("FILE", "FILE").url(url).build();
//                response = OkHttpHelper.getOkHttpClient().newCall(request).execute();
//                if (response.isSuccessful()) {
//                    contentLength = response.body().contentLength(); //获取文件总大小
//                    if (!"bytes".equals(response.header("Accept-Ranges"))) {//如果不支持断点下载
//                        temFile.delete();
//                    } else if (temFile.exists()) {//如果支持断点下载
//                        response.close();//关闭之前文件长度的请求，重新新开从断点请求
//                        downloadLength = temFile.length();
//                        request = new Request.Builder().url(url).addHeader("FILE", "FILE").addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength).build();
//                        response = OkHttpHelper.getOkHttpClient().newCall(request).execute();
//                        if (!response.isSuccessful()) {
//                            throw new IllegalStateException("");
//                        }
//                    }
//                    InputStream ips = response.body().byteStream();
//                    outputStream = new BufferedOutputStream(new FileOutputStream(temFile, true));
//                    int len;
//                    byte[] buff = new byte[1024];
//                    while ((len = ips.read(buff)) != -1) {
//                        outputStream.write(buff, 0, len);
//                        downloadLength += len;
//                    }
//                    response.close();
//                    response = null;
//                    outputStream.flush();
//                    outputStream.close();
//                    outputStream = null;
//                    if (md5.equals(Md5Util.getFileMD5String(temFile))) {
//                        temFile.renameTo(downLoadFile);
//                    } else {
//                        temFile.delete();
//                    }
//                }
//            } catch (IOException e) {
//                SLog.w(TAG, "downLoad", e);
//            } finally {
//                if (response != null) {
//                    response.close();
//                }
//                if (outputStream != null) {
//                    try {
//                        outputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                try {
//                    if (downLoadFile.exists() && md5.equals(Md5Util.getFileMD5String(downLoadFile))) {
//                        return downLoadFile;
//                    } else {
//                        downLoadFile.delete();
//                        throw new IllegalStateException("downLoadFile fail");
//                    }
//                } catch (IOException e) {
//                    throw e;
//                }
//            }
//        }
//    }
//
//
//    public String parseIDfromVideoUrl(String path) {
//        //因为我们这里也有接口文件，所有就能直接点出方法来
//        synchronized (mDexLimitLock) {
//            try {
//                if (mParseUrlMethod == null) {
//                    checkUpYoutubeParse();
//                }
//                return (String) mParseIDfMethod.invoke(mClazz, path);
//            } catch (Exception e) {
//                SLog.w(TAG, "parseIDByDex Exception", e);
//            }
//        }
//        return YoutubeParseUtils.parseIDfromVideoUrl(path);
//    }
//
//
//    private HashMap<String, String> mYoutubeUrlMap = new HashMap<>();
//    private final Object mYoutubeUrlMapLock = new Object();
//
//    public void removeYoutubeUrl(String playUrl) {
//        synchronized (mYoutubeUrlMapLock) {
//            Iterator<Map.Entry<String, String>> iterator = mYoutubeUrlMap.entrySet().iterator();
//            while (iterator.hasNext()) {
//                if (playUrl.equals(iterator.next().getValue())) {
//                    iterator.remove();
//                }
//            }
//        }
//    }
//
//    public Maybe<String> requestYoutubeUrl(String videoId, boolean haveAudio, String qualitys) {
//        return Maybe.defer(new Callable<MaybeSource<String>>() {
//            public Maybe<String> call() throws Exception {
//                return Maybe.create(new MaybeOnSubscribe<String>() {
//                    Future<String> future = null;
//
//                    @Override
//                    public void subscribe(MaybeEmitter<String> emitter) throws Exception {
//                        //优先采用缓存
//                        String playUrl;
//                        synchronized (mYoutubeUrlMapLock) {
//                            playUrl = mYoutubeUrlMap.get(videoId+haveAudio);
//                        }
//                        if (!TextUtils.isEmpty(playUrl)) {
//                            emitter.onSuccess(playUrl);
//                            return;
//                        }
//                        //先用动态的Dex来加载
//                        synchronized (mDexLimitLock) {
//                            try {
//                                if (mParseUrlMethod == null) {
//                                    checkUpYoutubeParse();
//                                }
//                                future = (Future<String>) mParseUrlMethod.invoke(mClazz, videoId, haveAudio, qualitys);
//                                emitter.setCancellable(new Cancellable() {
//                                    @Override
//                                    public void cancel() throws Exception {
//                                        future.cancel(true);
//                                    }
//                                });
//                                if (future != null && !future.get().contains("Exception")) {
//                                    emitter.onSuccess(future.get());
//                                    return;
//                                }
//                            } catch (Exception e) {
//                                SLog.w(TAG, "parseUrlByDex Exception", e);
//                            }
//                        }
//                        SLog.e(TAG, "YoutubeParseUtils future ,Thread.name=" + Thread.currentThread().getName());
//                        //如果发生了parseUrlByDex Exception，则采用自己的方法
//                        future = YoutubeParseUtils.parseUrl(videoId, haveAudio, qualitys);
//                        emitter.setCancellable(new Cancellable() {
//                            @Override
//                            public void cancel() throws Exception {
//                                future.cancel(true);
//                            }
//                        });
//                        emitter.onSuccess(future.get());
//                    }
//                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnSuccess(new Consumer<String>() {
//                    @Override
//                    public void accept(String playUrl) throws Exception {
//                        synchronized (mYoutubeUrlMapLock) {
//                            mYoutubeUrlMap.put(videoId+haveAudio, playUrl);
//                        }
//                    }
//                });
//            }
//        });
//    }
//
//}

package com.streambus.tinkerlib.update;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.streambus.tinkerlib.bean.ApkUpgradeBean;
import com.streambus.tinkerlib.bean.FixUpgradeBean;
import com.streambus.tinkerlib.bean.UploadInfo;
import com.streambus.tinkerlib.util.Md5Util;
import com.streambus.tinkerlib.util.TinkerManager;
import com.tencent.tinker.lib.util.TinkerLog;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/10/29
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class UpgradeHelper {
    private static final String TAG = "UpgradeManage";

    private static final String Check_APK_PATH = "https://pptvmsqvt.iplaymario.com/aicms/app/api/checkupdate";
    private static final String CHECK_FIX_PATH = "https://pptvmsqvt.iplaymario.com/aicms/app/api/fixcheck";

    private final Context mContext;
    private final UploadInfo mUploadData;

    private File mApkUpgradeDir;
    private File mFixUpgradeDir;
    private File mTemUpgradeDir;
    private OkHttpClient mOkHttpClient;

    private CheckupDateObservable mCheckupDateObservable;

    UpgradeHelper(Context context, OkHttpClient httpClient) {
        mContext = context;
        mUploadData = TinkerManager.getUploadData();
        mUploadData.setVersionCode("1");
        init(httpClient);
    }

    private void init(OkHttpClient httpClient) {
        mApkUpgradeDir = new File(mContext.getExternalCacheDir(), "/ApkUpgrade");
        if (mApkUpgradeDir.exists() && mApkUpgradeDir.isFile()) {
            mApkUpgradeDir.delete();
        }
        if (!mApkUpgradeDir.exists()) {
            mApkUpgradeDir.mkdirs();
        }

        mFixUpgradeDir = new File(mContext.getExternalCacheDir(), "/FixUpgrade");
        if (mFixUpgradeDir.exists() && mFixUpgradeDir.isFile()) {
            mFixUpgradeDir.delete();
        }
        if (!mFixUpgradeDir.exists()) {
            mFixUpgradeDir.mkdirs();
        }

        mTemUpgradeDir = new File(mContext.getExternalCacheDir(), "/TemUpgrade");
        if (mTemUpgradeDir.exists() && mTemUpgradeDir.isFile()) {
            mTemUpgradeDir.delete();
        }
        if (!mTemUpgradeDir.exists()) {
            mTemUpgradeDir.mkdirs();
        }
        if (httpClient != null) {
            mOkHttpClient = httpClient;
        } else {
            mOkHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request()
                                    .newBuilder()
                                    .addHeader("mode", "1")
                                    .addHeader("resultType", "1")
                                    .removeHeader("User-Agent")
                                    .addHeader("User-Agent", "MavisAgent/4.1")
                                    .build();
                            return chain.proceed(request);}})
                    .build();
        }
    }

    public Disposable checkUpdate(ICheckupCallBack callBack) {
        if (mCheckupDateObservable != null) {
            mCheckupDateObservable.dispose();
        }
        mCheckupDateObservable = new CheckupDateObservable();
        return mCheckupDateObservable.subscribe(callBack);
    }

    private class CheckupDateObservable implements Disposable {

        private final ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        private volatile boolean isDispose;
        private Call apkRequestCall;
        private Call fixRequestCall;
        private ICheckupCallBack callBack;

        @Override
        public void dispose() {
            if (!isDispose) {
                isDispose = true;
            }
            if (apkRequestCall != null) {
                apkRequestCall.cancel();
            }
            if (fixRequestCall != null) {
                fixRequestCall.cancel();
            }
            if (mCheckupDateObservable == this) {
                mCheckupDateObservable = null;
            }
        }
        @Override
        public boolean isDisposed() {
            return isDispose;
        }

        private void queryApkUpdate() {
            String apkUlr = Check_APK_PATH + "?packageName=" +mUploadData.getPackageName() + "&versionCode=" + mUploadData.getVersionCode();
            TinkerLog.d(TAG, "queryApkUpdate apkUlr=>" + apkUlr);
            apkRequestCall = mOkHttpClient.newCall(new Request.Builder().url(apkUlr).build());
            apkRequestCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    try {
                        ByteArrayOutputStream opt = new ByteArrayOutputStream(16 * 1024);
                        PrintWriter printWriter = new PrintWriter(opt);
                        e.printStackTrace(printWriter);
                        TinkerLog.e(TAG, opt.toString());
                    } catch (Exception e1) {
                        e.printStackTrace();
                    }
                    if (!isDispose) {
                        postQueryApk(null);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject jobject = new JSONObject(response.body().string());
                            TinkerLog.d(TAG, "queryApkUpdate jobject=>" + jobject);
                            ApkUpgradeBean upgradeBean = new ApkUpgradeBean(jobject);
                            if (!isDispose) {
                                postQueryApk(upgradeBean);
                            }
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!isDispose) {
                        postQueryApk(null);
                    }
                }
            });
        }

        private void queryFixUpdate() {
            String fixUlr = CHECK_FIX_PATH + "?packageName=" + mUploadData.getPackageName() + "&versionCode=" + mUploadData.getVersionCode() + "&patchVersion=" + mUploadData.getPatchVersion();
            TinkerLog.d(TAG, "queryFixUpdate fixUlr=>" + fixUlr);
            fixRequestCall = mOkHttpClient.newCall(new Request.Builder().url(fixUlr).build());
            fixRequestCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    if (!isDispose) {
                        postQueryFix(null);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject jobject = new JSONObject(response.body().string());
                            TinkerLog.d(TAG, "queryFixUpdate jobject=>" + jobject);
                            FixUpgradeBean upgradeBean = new FixUpgradeBean(jobject);
                            if (!isDispose) {
                                postQueryFix(upgradeBean);
                            }
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!isDispose) {
                        postQueryFix(null);
                    }
                }
            });
        }

        private void postQueryApk(final ApkUpgradeBean upgradeBean) {
            mSingleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (!isDispose) {
                        callBack.apkUpgrade(upgradeBean);
                    }
                }
            });
        }

        private void postQueryFix(final FixUpgradeBean upgradeBean) {
            mSingleThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (!isDispose) {
                        callBack.fixUpgrade(upgradeBean);
                    }
                }
            });
        }

        private Disposable subscribe(ICheckupCallBack callBack) {
            this.callBack = callBack;
            queryApkUpdate();
            queryFixUpdate();
            return this;
        }
    }

    private class DownLoadCallBack{
        private final IUpgradeListener listener;
        private final Handler handler;
        private long progress;
        private long total;
        private String tcpSpeed;
        private boolean isStart;
        public DownLoadCallBack(IUpgradeListener listener) {
            this.listener = listener;
            handler = new Handler(Looper.getMainLooper());
        }

        private void start(long progress, long total) {
            if (!isStart) {
                isStart = true;
                this.progress = progress;
                this.total = total;
                tcpSpeed = "0KB/S";
                handler.post(mPostDownLoadRunnable);
            }
        }

        private void progress(long progress, long total) {
            this.progress = progress;
            this.total = total;
        }

        private void postSuccess(final File file) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(file);
                    handler.removeCallbacksAndMessages(null);
                }
            });
        }

        private void postFailed() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailed();
                    handler.removeCallbacksAndMessages(null);
                }
            });
        }

        private Runnable mPostDownLoadRunnable = new Runnable() {
            private long lastProgress;
            private long lastPostTime;
            @Override
            public void run() {
                long uptimeMillis = SystemClock.uptimeMillis();
                long upProgress = progress;
                if (lastPostTime != 0) {
                    long speed = (upProgress - lastProgress) / (uptimeMillis - lastPostTime);
                    tcpSpeed = speedFormat(speed);
                }
                lastProgress = upProgress;
                lastPostTime = uptimeMillis;
                listener.onProgress(upProgress, total, tcpSpeed);

                handler.postDelayed(this, 500);
            }
            private String speedFormat(long speed) {
                String result;
                if (speed > 1024) {
                    long partA = speed / 1024;
                    long partB = (speed - partA * 1024) / 100;
                    result = partA + "." + partB + "MB/S";
                } else {
                    result = speed + "KB/S";
                }
                return result;
            }

        };

    }

    /************************************* - downLoadAPk  - ****************************************/
    public File downLoadAPk(ApkUpgradeBean apkUpgradeBean, IUpgradeListener listener) {
        DownLoadCallBack downLoadCallBack =  new DownLoadCallBack(listener);
        File file = inspectExitApk(apkUpgradeBean.getMd5());
        if (file == null) {
            file = downLoadApkFile(apkUpgradeBean, downLoadCallBack);
        }
        if (file != null) {
            try {
                if (apkUpgradeBean.getMd5().equals(Md5Util.getFileMD5String(file))) {
                    downLoadCallBack.postSuccess(file);
                    mUploadData.setVersionCode(apkUpgradeBean.getVersioncode());
                    return file;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            file.delete();
        }
        downLoadCallBack.postFailed();
        return null;
    }

    private File inspectExitApk(String md5) {
        File[] exitFiles = mApkUpgradeDir.listFiles();
        File apkFile = null;
        for (File file  : exitFiles) {
            if (file.getName().equals("apk_" + md5 + ".apk")) {
                apkFile = file;
            } else {
                file.delete();
            }
        }
        if (apkFile != null) {
            try {
                if (md5.equals(Md5Util.getFileMD5String(apkFile))) {
                    return apkFile;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            apkFile.delete();
        }
        return null;
    }

    private File downLoadApkFile(ApkUpgradeBean apkUpgradeBean, DownLoadCallBack callBack) {
        File temFile = new File(mTemUpgradeDir, "apk_" + apkUpgradeBean.getMd5() + ".apk");
        long downloadLength = 0;
        long contentLength;
        List<String> urls = new ArrayList<>();
        if (!TextUtils.isEmpty(apkUpgradeBean.getUrl())) urls.add(apkUpgradeBean.getUrl());
        if (!TextUtils.isEmpty(apkUpgradeBean.getUrl2())) urls.add(apkUpgradeBean.getUrl2());
        Iterator<String> iterator = urls.iterator();
        while (iterator.hasNext()) {
            String url = iterator.next();iterator.remove();
            Response response;
            try {
                Request request = new Request.Builder().addHeader("FILE", "FILE").url(url).build();
                response = mOkHttpClient.newCall(request).execute();
                if (!"bytes".equals(response.header("Accept-Ranges"))) {
                    if (iterator.hasNext()) { //如果不支持断点下载，并且如果有下一个地址切换下一个地址
                        break;
                    }
                    temFile.delete();
                }
                contentLength = response.body().contentLength();
            } catch (IOException e) {
                e.printStackTrace();
                break;//请求异常，切换下一个地址从新来
            }

            if (temFile.exists()) {//如果断点文件存在
                downloadLength = temFile.length();
                response.close();//关闭之前文件长度的请求，重新新开从断点请求
                Request request = new Request.Builder().url(url).addHeader("FILE", "FILE").addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength).build();
                try {
                    response = mOkHttpClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    //break;//请求异常，切换下一个地址从新来, 如果发生异常则response为null，程序直接跳转到finally，上一次下载的断点文件可能是整个完整的文件
                }
            }
            try {
                if (response.isSuccessful()) {
                    if (callBack != null) {
                        callBack.start(downloadLength, contentLength);
                    }
                    InputStream ips = response.body().byteStream();
                    FileOutputStream ops = new FileOutputStream(temFile, true);
                    int len;
                    byte[] buff = new byte[1024];
                    while ((len = ips.read(buff)) != -1) {
                        ops.write(buff, 0, len);
                        downloadLength += len;
                        if (callBack != null) {
                            callBack.progress(downloadLength, contentLength);
                        }
                    }
                    ops.flush();
                    ops.close();
                    ips.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if (temFile.exists() && temFile.length() >= contentLength) {
                    File apkFile = new File(mApkUpgradeDir, "apk_" + apkUpgradeBean.getMd5() + ".apk");
                    temFile.renameTo(apkFile);
                    return apkFile;
                }
            }
        }
        return null;
    }


    /************************************* - downLoadFix - ****************************************/
    public File downLoadFix(FixUpgradeBean fixUpgradeBean, IUpgradeListener listener) {
        DownLoadCallBack downLoadCallBack =  new DownLoadCallBack(listener);
        File file = inspectExitFix(fixUpgradeBean.getMd5());
        if (file == null) {
            file = downLoadFixFile(fixUpgradeBean, downLoadCallBack);
        }
        if (file != null) {
            try {
                if (fixUpgradeBean.getMd5().equals(Md5Util.getFileMD5String(file))) {
                    downLoadCallBack.postSuccess(file);
                    mUploadData.setPatchVersion(fixUpgradeBean.getPatchVersion());
                    return file;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            file.delete();
        }
        downLoadCallBack.postFailed();
        return null;
    }

    private File inspectExitFix(String md5) {
        File[] exitFiles = mFixUpgradeDir.listFiles();
        File fixFile = null;
        for (File file  : exitFiles) {
            if (file.getName().equals("fix_" + md5 + ".apk")) {
                fixFile = file;
            } else {
                file.delete();
            }
        }
        if (fixFile != null) {
            try {
                if (md5.equals(Md5Util.getFileMD5String(fixFile))) {
                    return fixFile;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            fixFile.delete();
        }
        return null;
    }

    private File downLoadFixFile(FixUpgradeBean fixUpgradeBean, DownLoadCallBack callBack) {
        File fixFile = new File(mFixUpgradeDir, "fix_" + fixUpgradeBean.getMd5() + ".apk");
        long downloadLength = 0;
        long contentLength = 0;
        try {
            Request request = new Request.Builder().addHeader("FILE", "FILE").url(fixUpgradeBean.getUrl()).build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                contentLength = response.body().contentLength();
                if (callBack != null) {
                    callBack.start(downloadLength, contentLength);
                }
                InputStream ips = response.body().byteStream();
                FileOutputStream ops = new FileOutputStream(fixFile);
                int len;
                byte[] buff = new byte[1024];
                while ((len = ips.read(buff)) != -1) {
                    ops.write(buff, 0, len);
                    downloadLength += len;
                    if (callBack != null) {
                        callBack.progress(downloadLength, contentLength);
                    }
                }
                ops.flush();
                ops.close();
                ips.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (fixFile.exists() && fixFile.length() >= contentLength) {
                return fixFile;
            }
        }
        return null;
    }




    public interface ICheckupCallBack {
        void apkUpgrade(ApkUpgradeBean upgradeBean);
        void fixUpgrade(FixUpgradeBean upgradeBean);
    }

}

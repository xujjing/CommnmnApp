package com.streambus.commonmodule.api;

import android.net.TrafficStats;
import android.text.TextUtils;
import android.util.Xml;

import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.bean.FeedbackInfo;
import com.streambus.commonmodule.utils.AESUtil;
import com.streambus.commonmodule.utils.Md5Util;
import com.streambus.commonmodule.utils.TraceRoute;
import com.streambus.requestapi.OkHttpHelper;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/7
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class FeedBackUtil {
    private static final String TAG = "SendFeedBackUtil";

    public static void autoSendFeedback(final FeedbackInfo feedbackInfo) {
        Disposable subscribe = Observable.just(feedbackInfo).map(new Function<FeedbackInfo, Boolean>() {
            @Override
            public Boolean apply(FeedbackInfo feedbackInfo) throws Exception {
                feedbackInfo.setPingInfo(getPingInfo(feedbackInfo.getVideoURL()));
                String buildXml = buildFeedbackInfoXml(feedbackInfo);
                SLog.i(TAG, "buildFeedbackInfoXml=" + buildXml);
                SLog.i(TAG, "Md5Util.md5DigestAsHex=" + buildXml);
                buildXml = AESUtil.encryptHex(Constants.LOG_AES_KEY, buildXml);
                SLog.i(TAG, "AESUtil.encryptHex=" + buildXml);
                String md5 = Md5Util.md5DigestAsHex(buildXml);
                Request request = new Request.Builder()
                        .url("http://feedback.dajljp29sd.com/collect/fbcollector.do?md5=" + md5)
                        .addHeader("NoEncryption", "NoEncryption")
                        .post(RequestBody.create(MediaType.parse("text/plain"), buildXml))
                        .build();

                Response response = OkHttpHelper.getOkHttpClient().newCall(request).execute();
                if (response.isSuccessful()) {
                    SLog.i(TAG, "sendFeedback_onSuccess success. response=>" + response.body().string());
                    return true;
                } else {
                    SLog.e(TAG, "sendFeedback_onSuccess failed. http_code=>" + response.code());
                    return false;
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                SLog.i(TAG,"autoSendFeedback Success");
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                SLog.e(TAG,"autoSendFeedback throwable", throwable);
            }
        });
    }

    public static Observable<Boolean> sendFeedback(final FeedbackInfo feedbackInfo) {
        return Observable.just(feedbackInfo).map(new Function<FeedbackInfo, Boolean>() {
            @Override
            public Boolean apply(FeedbackInfo feedbackInfo) throws Exception {
                String buildXml = buildFeedbackInfoXml(feedbackInfo);
                SLog.i(TAG, "buildFeedbackInfoXml=" + buildXml);
                SLog.i(TAG, "Md5Util.md5DigestAsHex=" + buildXml);
                buildXml = AESUtil.encryptHex(Constants.LOG_AES_KEY, buildXml);
                SLog.i(TAG, "AESUtil.encryptHex=" + buildXml);
                String md5 = Md5Util.md5DigestAsHex(buildXml);
                Request request = new Request.Builder()
                        .url("http://feedback.dajljp29sd.com/collect/fbcollector.do?md5=" + md5)
                        .addHeader("NoEncryption", "NoEncryption")
                        .post(RequestBody.create(MediaType.parse("text/plain"), buildXml))
                        .build();

                Response response = OkHttpHelper.getOkHttpClient().newCall(request).execute();
                if (response.isSuccessful()) {
                    SLog.i(TAG, "sendFeedback_onSuccess success. response=>" + response.body().string());
                    return true;
                } else {
                    SLog.e(TAG, "sendFeedback_onSuccess failed. http_code=>" + response.code());
                    return false;
                }
            }
        });
    }

    /**
     * 获取这个节目对应IP的ping结果
     */
    public static String getPingInfo(String videoURL) {
        String ip;
        if (!TextUtils.isEmpty(videoURL)) {
            if (videoURL.contains("http")) {
                int beg = videoURL.indexOf("//");
                int end = videoURL.indexOf("/", beg + 2);
                ip = videoURL.substring(beg + 2, end);
            } else {//p2p的节目默认ping的IP :104.148.36.58
                ip = "104.148.36.58";
            }
            SLog.d(TAG, "getPingInfo videoURL=" + videoURL + "  ip=" + ip);
            TraceRoute traceroute = TraceRoute.instance();
            return traceroute.pingIP(ip).replace("\n", "<br />");
        } else {
            return "";
        }
    }


    /**
     * 计算APP的网络数据的速率
     */
    private static String getNetworkSpeed(long startByte, long startTime) {
        long divideValue = System.currentTimeMillis() - startTime;
        long speed = 0;
        if (divideValue != 0) {
            speed = (TrafficStats.getUidRxBytes(android.os.Process.myUid()) - startByte) / divideValue / 1000;
        }
        return (speed / 1024L) + " Kb/s";
    }

    private static  String buildFeedbackInfoXml(FeedbackInfo feedbackInfo) {
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        String result = null;
        try {
            serializer.setOutput(writer);
            serializer.startDocument("utf-8", true);
            serializer.startTag(null, "feedback");

            serializer.startTag(null, "model");
            serializer.text(feedbackInfo.getModel());
            serializer.endTag(null, "model");

            serializer.startTag(null, "hardware");
            serializer.text(feedbackInfo.getCPUInfo());
            serializer.endTag(null, "hardware");

            serializer.startTag(null, "ram_memroy");
            serializer.text(feedbackInfo.getRamMemroy());
            serializer.endTag(null, "ram_memroy");

            serializer.startTag(null, "vm_size");
            serializer.text(feedbackInfo.getVmHeapSize());
            serializer.endTag(null, "vm_size");

            serializer.startTag(null, "screen");
            serializer.text(feedbackInfo.getScreenDisplay());
            serializer.endTag(null, "screen");

            serializer.startTag(null, "os_version");
            serializer.text(feedbackInfo.getSDKReleaseVersion());
            serializer.endTag(null, "os_version");

            serializer.startTag(null, "sdk_number");
            serializer.text(feedbackInfo.getSDKVersion());
            serializer.endTag(null, "sdk_number");

            serializer.startTag(null, "app_package");
            serializer.text(feedbackInfo.getAppPackageName());
            serializer.endTag(null, "app_package");

            serializer.startTag(null, "app_version");
            serializer.text(feedbackInfo.getAppVersionName());
            serializer.endTag(null, "app_version");

            serializer.startTag(null, "mac");
            serializer.text(feedbackInfo.getMAC());
            serializer.endTag(null, "mac");

            serializer.startTag(null, "Id");
            serializer.text(feedbackInfo.getID());
            serializer.endTag(null, "Id");

            serializer.startTag(null, "code");
            serializer.text(feedbackInfo.getCODE());
            serializer.endTag(null, "code");

            serializer.startTag(null, "validity");
            serializer.text(feedbackInfo.getValidity());
            serializer.endTag(null, "validity");

            serializer.startTag(null, "type");
            serializer.text(feedbackInfo.getVideoType());
            serializer.endTag(null, "type");

            serializer.startTag(null, "category");
            serializer.text(dealEmptyStr(feedbackInfo.getCategory()));
            serializer.endTag(null, "category");
            serializer.startTag(null, "channel_name");
            serializer.text(dealEmptyStr((feedbackInfo.getChannelName())));
            serializer.endTag(null, "channel_name");

            serializer.startTag(null, "exception_Type");
            serializer.text(feedbackInfo.getExceptionType());
            serializer.endTag(null, "exception_Type");

            serializer.startTag(null, "description");
            serializer.text(dealEmptyStr(feedbackInfo.getExceptionDescription()));
            serializer.endTag(null, "description");

            serializer.startTag(null, "decoder");
            serializer.text(feedbackInfo.getDecoder());
            serializer.endTag(null, "decoder");

            serializer.startTag(null, "video_url");
            serializer.text(dealEmptyStr(feedbackInfo.getVideoURL()));
            serializer.endTag(null, "video_url");

            serializer.startTag(null, "status");
            serializer.text(feedbackInfo.getPlayerState());
            serializer.endTag(null, "status");

            serializer.startTag(null, "speed");
            serializer.text(dealEmptyStr(feedbackInfo.getNetworkSpeed()));
            serializer.endTag(null, "speed");

            serializer.startTag(null, "ping_info");
            serializer.text(dealEmptyStr(feedbackInfo.getPingInfo()));
            serializer.endTag(null, "ping_info");

            serializer.endTag(null, "feedback");
            serializer.endDocument();
            result = writer.toString();
        } catch (Exception e) {
            SLog.i("Test12", "Exception:" + e.toString());
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                SLog.i("Test12", "Exception:" + e.toString());
            }
        }
        return result;
    }

    private static String dealEmptyStr(String str) {
        if (str == null) {
            str = "";
        } else {
            str = str.trim();
        }
        return str;
    }


}

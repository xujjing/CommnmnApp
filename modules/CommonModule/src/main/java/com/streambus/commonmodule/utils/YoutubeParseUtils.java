package com.streambus.commonmodule.utils;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.api.GzipRequestInterceptor;
import com.streambus.commonmodule.api.RequestApi;
import com.streambus.commonmodule.bean.CategoryListBean;
import com.streambus.commonmodule.bean.ResultBean;
import com.streambus.commonmodule.bean.RootDataBean;
import com.streambus.requestapi.OkHttpHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.functions.Cancellable;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/6/17
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class YoutubeParseUtils {
    private static final String TAG = "YoutubeParseUtils";

    private static final String VIDEO_ID_START_EMBED = "embed/";//有两种格式，尾部可能带有preview
    private static final String VIDEO_ID_START_NORMAL = "?v=";
    private static final String VIDEO_ID_START_SHORT = "youtu.be/";

    //在youtobe的连接中截取视频的ID
    public static String parseIDfromVideoUrl(String videoUrl){
        if(TextUtils.isEmpty(videoUrl)) {
            Log.i(TAG, "videoUrl is null");
            return "";
        }
        int startIndex = videoUrl.indexOf(VIDEO_ID_START_NORMAL);
        int prefixLength = VIDEO_ID_START_NORMAL.length();
        if(startIndex <= 0){
            startIndex = videoUrl.indexOf(VIDEO_ID_START_EMBED);
            prefixLength = VIDEO_ID_START_EMBED.length();
        }
        if(startIndex <= 0){
            startIndex = videoUrl.indexOf(VIDEO_ID_START_SHORT);
            prefixLength = VIDEO_ID_START_SHORT.length();
        }
        Log.i(TAG,"startIndex=="+startIndex);
        if(startIndex != -1){
            startIndex = startIndex + prefixLength;
            int endIndex = 0;//有些url后面会带参数，不能把参数当id
            if(prefixLength == VIDEO_ID_START_NORMAL.length()){//如果当前是普通类型的url
                endIndex = videoUrl.indexOf("&");
            } else {//embed或embed/xxx/preview或short  模式
                endIndex = videoUrl.indexOf("?");
            }
            if (endIndex == -1 && videoUrl.endsWith("preview")) {
                endIndex = videoUrl.indexOf("/", startIndex);
            }
            if(endIndex == -1) {
                endIndex = videoUrl.length();
            }
            Log.i(TAG,"startIndex::"+startIndex+"   end=="+endIndex);
            if(startIndex < endIndex) {
                return videoUrl.substring(startIndex,endIndex);
            }
        }else {
            Log.i(TAG,"不能解析视频的ID");
        }
        return "";
    }

    private static final OkHttpClient sOkHttpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(new GzipRequestInterceptor())
            .retryOnConnectionFailure(false)
            .build();

    private static HashMap<String, String> mYoutubeUrlMap = new HashMap<>();

    public static void removeYoutubeUrl(String playUrl) {
        synchronized (mYoutubeUrlMap) {
            Iterator<Map.Entry<String, String>> iterator = mYoutubeUrlMap.entrySet().iterator();
            while (iterator.hasNext()) {
                if (playUrl.equals(iterator.next().getValue())) {
                    iterator.remove();
                }
            }
        }
    }

    public static Maybe<String> parseUrl(final String videoId, final boolean haveAudio, final String qualitys){
        synchronized (mYoutubeUrlMap) {
            String playUrl = mYoutubeUrlMap.get(videoId+haveAudio);
            if (!TextUtils.isEmpty(playUrl)) {
                return Maybe.just(playUrl);
            }
        }
        return Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(MaybeEmitter<String> emitter) throws Exception {
                emitter.setCancellable(() -> cancel());
                String playUrl = get();
                if (!TextUtils.isEmpty(playUrl)) {
                    synchronized (mYoutubeUrlMap) {
                        mYoutubeUrlMap.put(videoId+haveAudio, playUrl);
                    }
                }
                emitter.onSuccess(playUrl);
            }

            private boolean isCancel;
            private Call call;
            private boolean cancel() {
                synchronized (this) {
                    if (call != null) {
                        call.cancel();
                    }
                    isCancel = true;
                    return true;
                }
            }

            private String get() {
                //-----请求youtube网站-------------------------------------------------------
                synchronized (this) {
                    if (isCancel) {
                        throw new CancellationException();
                    }
                    String urlPath = "https://www.youtube.com/watch?v=" + videoId + "&ei=" + getRandomString(21);
                    Log.d(TAG, "requestYoutubeUrl subscribe urlPath=>" + urlPath);
                    Request request = new Request.Builder().url(urlPath)
                            .removeHeader("User-Agent")
                            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Mobile Safari/537.36")
                            .get().build();
                    call = sOkHttpClient.newCall(request);
                }
                //------先采用本地解析--------------------------------------------------------------
                byte[] responseBytes;
                do {
                    try {
                        Response response = call.execute();
                        if (!response.isSuccessful()) {
                            throw new IllegalStateException("request youtube response.code=>" + response.code());
                        }
                        responseBytes = response.body().bytes();
                    } catch (Exception e) {
                        throw new RuntimeException("request youtube Exception:", e);
                    }
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(responseBytes)));
                        String playUrl = parseResponse(reader, haveAudio, qualitys);
                        SLog.d(TAG, "request local parseUrl=>" + playUrl);
                        if (!TextUtils.isEmpty(playUrl)) {
                            return playUrl;
                        }
                    } catch (Exception e) {
                        SLog.w(TAG,"local parseUrl Exception:", e);
                    }
                } while (false);

                //-------------请求服务器解析------------------------------------
                synchronized (this) {
                    if (isCancel) {
                        throw new CancellationException();
                    }
                    String urlPath = RequestApi.CMS_MAVIS_URL + "/client/youtube/parse";
                    String[] clientInfo = OkHttpHelper.getClientInfo(urlPath);
                    Request request = new Request.Builder().url(urlPath)
                            .addHeader("Client-ID", clientInfo[0]).addHeader("Client-MID", clientInfo[1]).addHeader("Access-Token", clientInfo[2])
                            .addHeader("Video-Id", videoId)
                            .addHeader("Video-haveAudio", haveAudio + "")
                            .addHeader("Video-qualitys", qualitys)
                            .header("Content-Encoding", "gzip")
                            .post(RequestBody.create(MediaType.parse("text/plain"), responseBytes)).build();
                    call = sOkHttpClient.newCall(request);
                }
                try {
                    Response response = call.execute();
                    if (!response.isSuccessful()) {
                        throw new IllegalStateException("request remote response.code=>" + response.code());
                    }
                    JSONObject jobject = new JSONObject(response.body().string());
                    String playUrl = jobject.getString("url");
                    SLog.d(TAG, "request remote parseUrl=>" + playUrl);
                    return playUrl;
                } catch (Exception e) {
                    throw new RuntimeException("request remote parseUrl Exception:", e);
                }
            }
        });
    }

    //length用户要求产生字符串的长度
    private static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    private static String parseResponse(BufferedReader reader, boolean haveAudio, String qualitys) throws Exception{
        String line;
        String url = null;
        while ((line = reader.readLine()) != null) {
            url = analyzePlayUrl(line, haveAudio, qualitys);
            if (!TextUtils.isEmpty(url)) {
                break;
            }
        }
        reader.close();
        return url;
    }

    private static String analyzePlayUrl(String line, boolean haveAudio, String qualitys) throws Exception{
        int index = line.indexOf("streamingData");
        if (index != -1) {
            Log.d(TAG,"index1=>" + index);
            index = line.indexOf("{", index);
            Log.d(TAG,"index2=>" + index);
            int beg = getPreJsonIndex(index, line);
            int end = getJsonEndIndex(beg, line);
            Log.d(TAG,"beg=>" + beg);
            Log.d(TAG,"end=>" + end);
            JSONObject responseData = new JSONObject(line.substring(beg, end));
            JSONObject streamingData = responseData.getJSONObject("streamingData");
            String url = null;
            if (!haveAudio) {// 解析adaptiveFormats, 只有图像，没有声音
                List<JSONObject> videoFormats = new ArrayList<>();
                JSONArray adaptiveFormats = streamingData.optJSONArray("adaptiveFormats");
                if (adaptiveFormats != null) {
                    for (int i = 0; i < adaptiveFormats.length(); i++) {
                        JSONObject adaptiveFormat = adaptiveFormats.getJSONObject(i);
                        if (adaptiveFormat.has("url")) {
                            if (adaptiveFormat.getString("mimeType").startsWith("video/mp4;")) {
                                videoFormats.add(adaptiveFormat);
                            }
                        }
                    }
                }
                url = preferredFormatUrl(videoFormats, qualitys);
            }
            if (TextUtils.isEmpty(url)) {// 解析formats
                List<JSONObject> channelFormats = new ArrayList<>();
                JSONArray formats = streamingData.optJSONArray("formats");
                if (formats != null) {
                    for (int i = 0; i < formats.length(); i++) {
                        JSONObject channelFormat = formats.getJSONObject(i);
                        if (channelFormat.has("url")) {
                            channelFormats.add(channelFormat);
                        }
                    }
                }
                url = preferredFormatUrl(channelFormats, qualitys);
            }
            return url;
        }
        return "";
    }

    private static int getJsonEndIndex(int beg, String buff) {
        int index;
        ArrayList<Character> list = new ArrayList<>();
        for (index = beg; index < buff.length(); index++) {
            char c = buff.charAt(index);
            if (c == '{' || c == '[') {
                if (index >= 0 && buff.charAt(index - 1) == '\\') {
                    continue;
                }
                list.add(c);
            } else if (c == '}' || c == ']') {
                if (index  >= 0 && buff.charAt(index - 1) == '\\') {
                    continue;
                }
                list.remove(list.size() - 1);
                if (list.size() == 0) {
                    if (buff.charAt(index) == '}') {
                        break;
                    }
                    return -1;
                }
            }
        }
        if (list.size() == 0 && buff.charAt(index) == '}') {
            return ++index;
        }
        return -1;
    }

    private static int getPreJsonIndex(int beg, String buff) {
        if (buff.charAt(beg) != '{') {
            return -1;
        }
        int index;
        ArrayList<Character> list = new ArrayList<>();
        for (index = beg-1; index >0; index--) {
            char c = buff.charAt(index);
            if (c == '}' || c == ']') {
                if (index >= 0 && buff.charAt(index - 1) == '\\') {
                    continue;
                }
                list.add(c);
            } else if (c == '{' || c == '[') {
                if (index > 0 && buff.charAt(index - 1) == '\\') {
                    continue;
                }
                if (list.size() > 0) {
                    list.remove(list.size() - 1);
                } else {
                    if (c == '{') {
                        break;
                    }
                    return -1;
                }
            }
        }
        if (list.size() == 0 && index >=0 && buff.charAt(index) == '{') {
            return index;
        }
        return -1;
    }

    private static String preferredFormatUrl(List<JSONObject> list, String qualityString) {
        if (list != null && !list.isEmpty()) {
            String[] qualitys = null;
            if (qualityString != null) {
                qualitys = qualityString.split("\\|");
            }
            if (qualitys == null || qualitys.length == 0) {
                qualitys = new String[]{"720p"};
            }
            for (String quality : qualitys) {
                for (JSONObject channelFormat : list) {
                    if (quality.equals(channelFormat.optString("qualityLabel"))) {
                        return channelFormat.optString("url");
                    }
                }
            }
            return list.get(0).optString("url");
        }
        return null;
    }
}

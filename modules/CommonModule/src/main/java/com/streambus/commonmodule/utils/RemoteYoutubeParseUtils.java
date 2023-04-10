package com.streambus.commonmodule.utils;

import android.text.TextUtils;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/6/17
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class RemoteYoutubeParseUtils {
    private static final String TAG = "RemoteYoutubeParseUtils";

    /**
     * 解析Youtube播放链接
     * @param reader 数据流
     * @param haveAudio 是否带音频
     * @param qualitys 视频质量
     * @return
     * @throws Exception
     */
    public static String parseResponse(BufferedReader reader, boolean haveAudio, String qualitys) throws Exception{
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

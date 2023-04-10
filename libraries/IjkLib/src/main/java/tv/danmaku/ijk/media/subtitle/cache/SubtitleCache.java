/*
 *                       Copyright (C) of Avery
 *
 *                              _ooOoo_
 *                             o8888888o
 *                             88" . "88
 *                             (| -_- |)
 *                             O\  =  /O
 *                          ____/`- -'\____
 *                        .'  \\|     |//  `.
 *                       /  \\|||  :  |||//  \
 *                      /  _||||| -:- |||||-  \
 *                      |   | \\\  -  /// |   |
 *                      | \_|  ''\- -/''  |   |
 *                      \  .-\__  `-`  ___/-. /
 *                    ___`. .' /- -.- -\  `. . __
 *                 ."" '<  `.___\_<|>_/___.'  >'"".
 *                | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *                \  \ `-.   \_ __\ /__ _/   .-` /  /
 *           ======`-.____`-.___\_____/___.-`____.-'======
 *                              `=- -='
 *           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *              Buddha bless, there will never be bug!!!
 */

package tv.danmaku.ijk.media.subtitle.cache;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import tv.danmaku.ijk.media.DebugLog;
import tv.danmaku.ijk.media.subtitle.model.Subtitle;

/**
 * @author AveryZhong.
 */

public class SubtitleCache {
    private static final String TAG = "SubtitleCache";
    private HashMap<String, List<Subtitle>> mCache = new HashMap<>();

    public synchronized void put(String key, List<Subtitle> subtitles) {
        String md5Key = getMD5(key);
        if (md5Key == null) {
            return;
        }
        mCache.put(md5Key, subtitles);
    }

    @Nullable
    public List<Subtitle> get(String key) {
        String md5Key = getMD5(key);

        if (md5Key == null) {
            return null;
        }
        return mCache.get(md5Key);
    }

    private static String getMD5(String str) {
        if (str == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            DebugLog.e(TAG, "getMD5", e);
            return null;
        }
    }

}

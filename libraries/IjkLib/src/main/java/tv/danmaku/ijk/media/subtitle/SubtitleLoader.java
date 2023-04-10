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

package tv.danmaku.ijk.media.subtitle;

import android.text.TextUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import tv.danmaku.ijk.media.DebugLog;
import tv.danmaku.ijk.media.subtitle.exception.FatalParsingException;
import tv.danmaku.ijk.media.subtitle.format.FormatASS;
import tv.danmaku.ijk.media.subtitle.format.FormatSRT;
import tv.danmaku.ijk.media.subtitle.format.FormatSTL;
import tv.danmaku.ijk.media.subtitle.model.TimedTextObject;

/**
 * @author AveryZhong.
 */

public class SubtitleLoader {
    private static final String TAG = SubtitleLoader.class.getSimpleName();

    private SubtitleLoader() {
        throw new AssertionError("No instance for you.");
    }

    public static void setLoaderModule(LoaderModule sLoaderModule) {
        SubtitleLoader.sLoaderModule = sLoaderModule;
    }

    private static LoaderModule sLoaderModule = new LoaderModule() {
        @Override
        public TimedTextObject loadSubtitle(String path) {
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            try {
                if (path.startsWith("http://")
                        || path.startsWith("https://")) {
                    return loadAndParse(new URL(path).openStream(), path);
                } else {
                    return loadAndParse(new FileInputStream(path), path);
                }
            } catch (Exception e) {
                DebugLog.e(TAG, "loadSubtitle", e);
            }
            return null;
        }
    };
    public static abstract class LoaderModule{
        public abstract TimedTextObject loadSubtitle(String path);

        public final TimedTextObject loadAndParse(InputStream is, String filePath) throws IOException, FatalParsingException {
            return SubtitleLoader.loadAndParse(is, filePath);
        }
    }

    public static TimedTextObject loadSubtitle(String path) {
        return sLoaderModule.loadSubtitle(path);
    }


    private static TimedTextObject loadAndParse(final InputStream is, final String filePath)
            throws IOException, FatalParsingException {
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        String ext = fileName.substring(fileName.lastIndexOf("."));
        DebugLog.d(TAG, "parse: name = " + fileName + ", ext = " + ext);
        if (".srt".equalsIgnoreCase(ext)) {
            return new FormatSRT().parseFile(fileName, is);
        } else if (".ass".equalsIgnoreCase(ext)) {
            return new FormatASS().parseFile(fileName, is);
        } else if (".stl".equalsIgnoreCase(ext)) {
            return new FormatSTL().parseFile(fileName, is);
        } else if (".ttml".equalsIgnoreCase(ext)) {
            return new FormatSTL().parseFile(fileName, is);
        }
        return new FormatSRT().parseFile(fileName, is);
    }

    public interface Callback {
        void onSuccess(TimedTextObject timedTextObject);

        void onError(Exception exception);
    }
}

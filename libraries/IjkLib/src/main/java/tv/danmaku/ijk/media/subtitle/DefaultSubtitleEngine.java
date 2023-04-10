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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import androidx.annotation.Nullable;
import tv.danmaku.ijk.media.DebugLog;
import tv.danmaku.ijk.media.subtitle.cache.SubtitleCache;
import tv.danmaku.ijk.media.subtitle.model.Subtitle;
import tv.danmaku.ijk.media.subtitle.model.TimedTextObject;
import tv.danmaku.ijk.media.subtitle.runtime.AppTaskExecutor;

/**
 * @author AveryZhong.
 */

public class DefaultSubtitleEngine implements ISubtitleEngine {
    private static final String TAG = DefaultSubtitleEngine.class.getSimpleName();
    private static final int MSG_REFRESH = 0x888;
    private static final int REFRESH_INTERVAL = 100;

    @Nullable
    private HandlerThread mHandlerThread;
    @Nullable
    private Handler mWorkHandler;
    @Nullable
    private List<Subtitle> mSubtitles;
    private UIRenderTask mUIRenderTask;
    private ISubtitlePlayer mMediaPlayer;
    private SubtitleCache mCache;
    private OnSubtitlePreparedListener mOnSubtitlePreparedListener;
    private OnSubtitleChangeListener mOnSubtitleChangeListener;

    public DefaultSubtitleEngine() {
        mCache = new SubtitleCache();

    }

    @Override
    public void bindToMediaPlayer(final ISubtitlePlayer mediaPlayer) {
        mMediaPlayer = mediaPlayer;
    }

    @Override
    public void setSubtitlePath(final String path) {
        DebugLog.d(TAG, "setSubtitlePath=>" + path);
        doOnSubtitlePathSet();
        if (TextUtils.isEmpty(path)) {
            DebugLog.w(TAG, "loadSubtitleFromRemote: path is null.");
            return;
        }
        mSubtitles = mCache.get(path);
        if (mSubtitles != null && !mSubtitles.isEmpty()) {
            DebugLog.d(TAG, "from cache.");
            notifyPrepared();
            return;
        }
        AppTaskExecutor.deskIO().execute(new Runnable() {
            @Override
            public void run() {
                TimedTextObject timedTextObject = SubtitleLoader.loadSubtitle(path);
                if (timedTextObject == null) {
                    DebugLog.d(TAG, "onSuccess: timedTextObject is null.");
                    return;
                }
                final TreeMap<Integer, Subtitle> captions = timedTextObject.captions;
                if (captions == null) {
                    DebugLog.d(TAG, "onSuccess: captions is null.");
                    return;
                }
                mSubtitles = new ArrayList<>(captions.values());
                notifyPrepared();
                mCache.put(path, new ArrayList<>(captions.values()));
            }
        });
    }

    private void doOnSubtitlePathSet() {
        reset();
        createWorkThread();
    }

    @Override
    public void reset() {
        stopWorkThread();
        mSubtitles = null;
        mUIRenderTask = null;

    }

    @Override
    public void start() {
        DebugLog.d(TAG, "start: ");
        if (mMediaPlayer == null) {
            DebugLog.w(TAG, "MediaPlayer is not bind, You must bind MediaPlayer to "
                    + ISubtitleEngine.class.getSimpleName()
                    + " before start() method be called,"
                    + " you can do this by call " +
                    "bindToMediaPlayer(MediaPlayer mediaPlayer) method.");
            return;
        }
        if (mWorkHandler != null) {
            mWorkHandler.removeMessages(MSG_REFRESH);
            mWorkHandler.sendEmptyMessageDelayed(MSG_REFRESH, REFRESH_INTERVAL);
        }

    }

    @Override
    public void pause() {
        if (mWorkHandler != null) {
            mWorkHandler.removeMessages(MSG_REFRESH);
        }
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public void stop() {
        if (mWorkHandler != null) {
            mWorkHandler.removeMessages(MSG_REFRESH);
        }
    }

    @Override
    public void destroy() {
        DebugLog.d(TAG, "destroy: ");
        stopWorkThread();
        mSubtitles = null;
        mUIRenderTask = null;

    }

    private void createWorkThread() {
        mHandlerThread = new HandlerThread("SubtitleFindThread");
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(final Message msg) {
                try {
                    long delay = REFRESH_INTERVAL;
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        long position = mMediaPlayer.getCurrentPosition();
                        Subtitle subtitle = SubtitleFinder.find(position, mSubtitles);
                        notifyRefreshUI(subtitle);
                        if (subtitle != null) {
                            delay = subtitle.end.mseconds - position;
                        }

                    }
                    if (mWorkHandler != null) {
                        mWorkHandler.sendEmptyMessageDelayed(MSG_REFRESH, delay);
                    }
                } catch (Exception e) {
                    DebugLog.w(TAG, "createWorkThread handleMessage", e);
                }
                return true;
            }
        });
    }

    private void stopWorkThread() {
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        if (mWorkHandler != null) {
            mWorkHandler.removeCallbacksAndMessages(null);
            mWorkHandler = null;
        }
    }

    private void notifyRefreshUI(final Subtitle subtitle) {
        if (mUIRenderTask == null) {
            mUIRenderTask = new UIRenderTask(mOnSubtitleChangeListener);
        }
        mUIRenderTask.execute(subtitle);
    }

    private void notifyPrepared() {
        if (mOnSubtitlePreparedListener != null) {
            DebugLog.d(TAG, "notifyPrepared=>" + mSubtitles.size());
            mOnSubtitlePreparedListener.onSubtitlePrepared(mSubtitles);
        }
    }

    @Override
    public void setOnSubtitlePreparedListener(final OnSubtitlePreparedListener listener) {
        mOnSubtitlePreparedListener = listener;
    }

    @Override
    public void setOnSubtitleChangeListener(final OnSubtitleChangeListener listener) {
        mOnSubtitleChangeListener = listener;
    }

}

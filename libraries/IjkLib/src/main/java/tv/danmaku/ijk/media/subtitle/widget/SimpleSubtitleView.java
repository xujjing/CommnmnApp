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

package tv.danmaku.ijk.media.subtitle.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import tv.danmaku.ijk.media.subtitle.DefaultSubtitleEngine;
import tv.danmaku.ijk.media.subtitle.ISubtitleEngine;
import tv.danmaku.ijk.media.subtitle.ISubtitlePlayer;
import tv.danmaku.ijk.media.subtitle.model.Subtitle;
import tv.danmaku.ijk.media.widget.media.IMediaPlayer;

/**
 * @author AveryZhong.
 */

@SuppressLint("AppCompatCustomView")
public class SimpleSubtitleView extends TextView {

    private static final String EMPTY_TEXT = "";

    private ISubtitleEngine mSubtitleEngine;
    private IMediaPlayer mMediaPlayer;
    private long mDiffTime;

    public SimpleSubtitleView(final Context context) {
        super(context);
        init();
    }

    public SimpleSubtitleView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleSubtitleView(final Context context, final AttributeSet attrs,
                              final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSubtitleEngine = new DefaultSubtitleEngine();
        mSubtitleEngine.setOnSubtitlePreparedListener(new ISubtitleEngine.OnSubtitlePreparedListener() {
            @Override
            public void onSubtitlePrepared(@Nullable List<Subtitle> subtitles) {
                if (mMediaPlayer != null) {
                    start();
                }
            }
        });
        mSubtitleEngine.setOnSubtitleChangeListener(new ISubtitleEngine.OnSubtitleChangeListener() {
            @Override
            public void onSubtitleChanged(@Nullable Subtitle subtitle) {
                if (subtitle == null) {
                    setText(EMPTY_TEXT);
                    return;
                }
                setText(constructSpan(Html.fromHtml(subtitle.content)));
            }
        });
    }

    public void setSubtitlePath(final String path) {
        mSubtitleEngine.setSubtitlePath(path);
        setText(EMPTY_TEXT);
    }

    public void reset() {
        mSubtitleEngine.reset();
        setText(EMPTY_TEXT);
    }

    public void start() {
        mSubtitleEngine.start();
    }

    public void pause() {
        mSubtitleEngine.pause();
    }

    public void resume() {
        mSubtitleEngine.resume();
    }

    public void stop() {
        mSubtitleEngine.stop();
    }

    public void destroy() {
        mSubtitleEngine.destroy();
    }

    public void bindToMediaPlayer(IMediaPlayer mediaPlayer) {
        mMediaPlayer = mediaPlayer;
        if (mediaPlayer == null) {
            mSubtitleEngine.bindToMediaPlayer(null);
        } else {
            mSubtitleEngine.bindToMediaPlayer(mSubtitlePlayer);
        }
    }

    public void adjustDiffTime(long diffTime) {
        mDiffTime = diffTime;
    }

    public long getDiffTime() {
        return mDiffTime;
    }

    private final ISubtitlePlayer mSubtitlePlayer = new ISubtitlePlayer() {
        @Override
        public boolean isPlaying() {
            if (mMediaPlayer == null) {
                return false;
            }
            return mMediaPlayer.isPlaying();
        }

        @Override
        public long getCurrentPosition() {
            if (mMediaPlayer != null) {
                long currentPosition = mMediaPlayer.getCurrentPosition();
                if (currentPosition > 0) {
                    currentPosition += mDiffTime;
                    if (currentPosition > 0) {
                        return currentPosition;
                    }
                }
            }
            return 0;
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        destroy();
        super.onDetachedFromWindow();
    }

    private BackgroundColorSpan backgroundColorSpan;
    public void setBackgroundSpan(@ColorInt int colorRes) {
        backgroundColorSpan = new BackgroundColorSpan(colorRes);
    }

    private SpannableStringBuilder constructSpan(CharSequence text) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();
        if (!TextUtils.isEmpty(text)) {
            spannableBuilder.append(text);
            if (null != backgroundColorSpan) {
                spannableBuilder.setSpan(backgroundColorSpan, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        return spannableBuilder;
    }
}

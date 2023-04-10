/*
 * Copyright (C) 2013-2014 Bilibili
 * Copyright (C) 2013-2014 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.widget.media;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractMediaPlayer implements IMediaPlayer {
    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnTimedTextListener mOnTimedTextListener;
    private IMediaPlayer.OnRecordingStatusListener mOnRecordingStatusListener;
    public IMediaPlayer.OnRecordingProgressListener mOnRecordingProgressListener;
    public IMediaPlayer.OnPullStreamListener mOnPullStreamListener;
    public IMediaPlayer.OnLineDetectionListener mOnLineDetectionListener;

    @Override
    public final void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    @Override
    public final void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    @Override
    public final void setOnBufferingUpdateListener(
            OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    @Override
    public final void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    @Override
    public final void setOnVideoSizeChangedListener(
            OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    @Override
    public final void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    @Override
    public final void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    @Override
    public final void setOnTimedTextListener(OnTimedTextListener listener) {
        mOnTimedTextListener = listener;
    }

    @Override
    public final void setOnRecordingStatusListener(IMediaPlayer.OnRecordingStatusListener listener) {
        mOnRecordingStatusListener = listener;
    }

    public void resetListeners() {
        mOnPreparedListener = null;
        mOnBufferingUpdateListener = null;
        mOnCompletionListener = null;
        mOnSeekCompleteListener = null;
        mOnVideoSizeChangedListener = null;
        mOnErrorListener = null;
        mOnInfoListener = null;
        mOnTimedTextListener = null;
        mOnRecordingStatusListener = null;
        mOnRecordingProgressListener = null;
        mOnPullStreamListener = null;
    }

    protected final void notifyOnPrepared() {
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(this);
        }
    }

    protected final void notifyOnCompletion() {
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(this);
        }
    }

    protected final void notifyOnBufferingUpdate(int percent) {
        if (mOnBufferingUpdateListener != null) {
            mOnBufferingUpdateListener.onBufferingUpdate(this, percent);
        }
    }

    protected final void notifyOnSeekComplete() {
        if (mOnSeekCompleteListener != null) {
            mOnSeekCompleteListener.onSeekComplete(this);
        }
    }

    protected final void notifyOnVideoSizeChanged(int width, int height,
                                                  int sarNum, int sarDen) {
        if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener.onVideoSizeChanged(this, width, height,
                    sarNum, sarDen);
        }

    }

    protected final boolean notifyOnError(int what, int extra) {
        return mOnErrorListener != null && mOnErrorListener.onError(this, what, extra);
    }

    protected final boolean notifyOnInfo(int what, int extra) {
        return mOnInfoListener != null && mOnInfoListener.onInfo(this, what, extra);
    }

    protected final void notifyOnTimedText(IjkTimedText text) {
        if (mOnTimedTextListener != null) {
            mOnTimedTextListener.onTimedText(this, text);
        }
    }

    protected final void notifyRecordingStatus(int what, int extra) {
        if (mOnRecordingStatusListener != null) {
            mOnRecordingStatusListener.onRecordingStatus((IMediaPlayer) this, what, extra);
        }
    }

    protected final void notifyRecordingProgress(int count) {
        if (mOnRecordingProgressListener != null) {
            mOnRecordingProgressListener.OnRecordingProgress((IMediaPlayer) this, count);
        }
    }

    protected final void notifyPullStream() {
        if (mOnPullStreamListener != null) {
            mOnPullStreamListener.OnPullStream(this);
        }
    }

    protected final void notifyLineDetection(int playId, String optimizeLine) {
        if (mOnLineDetectionListener != null) {
            mOnLineDetectionListener.OnLineDetection((IMediaPlayer) this, playId, optimizeLine);
        }
    }

    @Override
    public void setDataSource(IMediaDataSource mediaDataSource) {
        throw new UnsupportedOperationException();
    }
}

/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
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

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import tv.danmaku.ijk.media.DebugLog;
import tv.danmaku.ijk.media.Pragma;
import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.widget.media.opengl.GLSurfaceRenderView;
import tv.danmaku.ijk.media.widget.preference.Settings;


public class IjkVideoView extends FrameLayout implements MediaController.MediaPlayerControl, IMediaController.MediaPlayerControl {
    private String TAG = "IjkVideoView";
    // settable by the client
    private Uri mUri;
    private Map<String, String> mHeaders = new HashMap<>();

    // all possible internal states
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    public static final int MSG_ERROR_CATON = -10001;//-10001,1//卡顿消息

    // All the stuff we need for playing and showing a video
    private IRenderView.ISurfaceHolder mSurfaceHolder = null;
    private IMediaPlayer mMediaPlayer = null;
    // private int         mAudioSession;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mVideoRotationDegree;
    private IMediaController mMediaController;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private IMediaPlayer.OnRecordingStatusListener mOnRecordingStatusListener;
    private IMediaPlayer.OnRecordingProgressListener mOnRecordingProgressListener;
    private IMediaPlayer.OnPullStreamListener mOnPullStreamListener;
    private int mCurrentBufferPercentage;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mCanPause = true;
    private boolean mCanSeekBack = true;
    private boolean mCanSeekForward = true;
    private int videoSizeType = IRenderView.AR_ASPECT_FIT_PARENT;

    private Context mAppContext;
    private IRenderView mRenderView;
    private int mVideoSarNum;
    private int mVideoSarDen;
    private int playerType, decodeType;
    private IjkMediaPlayer mIjkMediaPlayer;

    private PowerError mPowerError;
    private int mRenderType;

    public void setmPlayError(PowerError playError) {
        this.mPowerError = playError;
    }

    public IjkVideoView(Context context) {
        this(context, null);
    }

    public IjkVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadLibraries();
        initVideoView(context);
    }

    private void loadLibraries() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

    }

    // REMOVED: onMeasure
    // REMOVED: onInitializeAccessibilityEvent
    // REMOVED: onInitializeAccessibilityNodeInfo
    // REMOVED: resolveAdjustedSize

    private void initVideoView(Context context) {
        mAppContext = context.getApplicationContext();
        playerType = PLAYER_IjkMediaPlayer;
        decodeType = 1;
        initDefRender();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public static final int RENDER_NONE = 0;
    public static final int RENDER_SURFACE_VIEW = 1;
    public static final int RENDER_TEXTURE_VIEW = 2;
    public static final int RENDER_GLVIDEO_VIEW = 3;

    public void initDefRender() {
        if (mRenderType != RENDER_TEXTURE_VIEW || mRenderType != RENDER_SURFACE_VIEW) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setRender(RENDER_TEXTURE_VIEW);
            } else {
                setRender(RENDER_SURFACE_VIEW);
            }
        }
    }

    public void init3DRender() {
        if (mRenderType != RENDER_GLVIDEO_VIEW) {
            setRender(RENDER_GLVIDEO_VIEW);
        }
    }

    private void setRender(int render) {
        mRenderType = render;
        switch (render) {
            case RENDER_NONE:
                setRenderView(null);
                break;
            case RENDER_TEXTURE_VIEW: {
                TextureRenderView renderView = new TextureRenderView(getContext());
                if (mMediaPlayer != null) {
                    renderView.getSurfaceHolder().bindToMediaPlayer(mMediaPlayer);
                    renderView.setVideoSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                    renderView.setVideoSampleAspectRatio(mMediaPlayer.getVideoSarNum(), mMediaPlayer.getVideoSarDen());
                    renderView.setAspectRatio(videoSizeType);
                }
                setRenderView(renderView);
                break;
            }
            case RENDER_SURFACE_VIEW: {
                SurfaceRenderView renderView = new SurfaceRenderView(getContext());
                setRenderView(renderView);
                break;
            }
            case RENDER_GLVIDEO_VIEW: {
                GLSurfaceRenderView renderView = new GLSurfaceRenderView(getContext());
                setRenderView(renderView);
                break;
            }
            default:
//                DebugLog.e(TAG, String.format(Locale.getDefault(), "invalid render %d\n", render));
                break;
        }
    }


    public void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(null);
            }

            View renderUIView = mRenderView.getView();
            mRenderView.removeRenderCallback(mSHCallback);
            mRenderView = null;
            removeView(renderUIView);
        }

        if (renderView == null) {
            return;
        }
        mRenderView = renderView;
        renderView.setAspectRatio(videoSizeType);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            renderView.setVideoSize(mVideoWidth, mVideoHeight);
        }
        if (mVideoSarNum > 0 && mVideoSarDen > 0) {
            renderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
        }

        View renderUIView = mRenderView.getView();
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        renderUIView.setLayoutParams(lp);
        addView(renderUIView);

        mRenderView.addRenderCallback(mSHCallback);
        mRenderView.setVideoRotation(mVideoRotationDegree);
    }


    /**
     * 自己测试
     * 重新设置render渲染目标，该方法能达到抹去之前视频最后一帧的效果<br>
     * 一般在stopPlayBack后，设置新播放源之前调用。
     */
    public void refreshRender() {
        setRender(mRenderType);
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
       DebugLog.e("TAG","setVideoPath");
        setVideoURI(Uri.parse(path));
    }

    /**
     * 根据播放器的类型和解码类型来播放
     *
     * @param path
     * @param playerType
     * @param decodeType
     */
    public void setVideoPath(String path, int playerType, int decodeType) {
        this.playerType = playerType;
        this.decodeType = decodeType;
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        mUri = uri;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public Uri getVideoURI() {
        return mUri;
    }

    /**
     * 改变编码方式
     *
     * @param decodeType
     */
    public void changeDecodeType(int decodeType) {
        this.decodeType = decodeType;
        openVideo();
    }

    /**
     * Sets specific headers.
     * 需在setVideoPath之前调用
     *
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    public void setHeaders(Map<String, String> headers) {
        mHeaders = headers;
    }

    // REMOVED: addSubtitleSource
    // REMOVED: mPendingSubtitleTracks

    public void stopPlayback() {
       DebugLog.e("TAG","mMediaPlayer is "+mMediaPlayer);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mUri = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    public boolean isMediaPlayer(){
        return mMediaPlayer == null;
    }


    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            return;
        }
        release(false);
        AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = createPlayer(playerType, decodeType);
                attachMediaController();
            }
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnRecordingStatusListener(mOnRecordingListener);
            mMediaPlayer.setOnRecordingProgressListener(mOnRecProgressListener);
            mMediaPlayer.setOnPullStreamListener(onPullStreamListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(getContext(), mUri);
            bindSurfaceHolder(mMediaPlayer, mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
        } catch (Exception ex) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }




    }

    public void setMediaController(IMediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ?
                    (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
              DebugLog.i(TAG, "mSizeChangedListener");
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoSarNum = mp.getVideoSarNum();
            mVideoSarDen = mp.getVideoSarDen();
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                if (decodeType == 1) {
                    ///硬解情况下，分辨率异常无法播放
                    if ((mVideoWidth / mVideoHeight) >= 3) {
                        ///异常处理
                        if (mPowerError != null) {
                            mPowerError.powerError();
                        }
                        stopPlayback();
                    } else {
                        if (mRenderView != null) {
                            mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                            mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                        }
                        requestLayout();
                    }
                } else {
                    if (mRenderView != null) {
                        mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                        mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                    }
                    requestLayout();
                }
                // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            }
        }
    };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            //  DebugLog.i(TAG, "mPreparedListener");
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
            // REMOVED: Metadata

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            DebugLog.e("toRePlay"," toRePlay seek is "+seekToPosition);
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
                // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mRenderView != null) {
                    mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                    mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                    if (!mRenderView.shouldWaitForResize() || mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                        // We didn't actually change the size (it was already at the size
                        // we need), so we won't get a "surface changed" callback, so
                        // start the video here instead of in the callback.
                        if (mTargetState == STATE_PLAYING) {
                            start();
                            if (mMediaController != null) {
                                mMediaController.show();
                            }
                        } else if (!isPlaying() &&
                                (seekToPosition != 0 || getCurrentPosition() > 0)) {
                            if (mMediaController != null) {
                                // Show the media controls when we're paused into a video and make 'em stick.
                                mMediaController.show(0);
                            }
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
               DebugLog.i(TAG, "mCompletionListener");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mMediaController != null) {
                mMediaController.hide();
            }
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };

    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
            DebugLog.i(TAG, "mInfoListener " + arg1 + "," + arg2);
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, arg1, arg2);
            }
            switch (arg1) {
                case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    // DebugLog.i(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                case IMediaPlayer.MEDIA_INFO_VIDEO_SEEK_RENDERING_START:
                case IMediaPlayer.MEDIA_INFO_AUDIO_SEEK_RENDERING_START:
                    //  DebugLog.i(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                    mCurrentState=STATE_PLAYING;   //改变状态
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:  //开始缓冲
                    //  DebugLog.i(TAG, "MEDIA_INFO_BUFFERING_START:");
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:     //结束缓冲
                    // DebugLog.i(TAG, "MEDIA_INFO_BUFFERING_END:");
                    break;
                case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:   // 显示下载速度
                    //Log.i(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                    break;
                case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                    //Log.i(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                    break;
                case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    //Log.i(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                    break;
                case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    //  DebugLog.i(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                    break;
                case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                    // DebugLog.i(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                    break;
                case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                    // DebugLog.i(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    mVideoRotationDegree = arg2;
                    //  DebugLog.i(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);
                    if (mRenderView != null) {
                        mRenderView.setVideoRotation(arg2);
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    // DebugLog.i(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                    break;

            }
            return true;
        }
    };

    private IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            DebugLog.i(TAG, "OnErrorListener: " + framework_err + "," + impl_err);
            if (framework_err == MSG_ERROR_CATON) {// -10001,1//卡顿消息
                //网络拉流快慢不稳定 卡顿一下 不能作为一个错误状态的记录
                //影响 isInPlaybackState() --> isPlaying()判断
                //长时间卡顿拉不到流 使用->onPullStreamListener
                mCurrentState = STATE_ERROR;
            }
            mTargetState = STATE_ERROR;
            if (mMediaController != null) {
                mMediaController.hide();
            }
            /* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }
            return true;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            DebugLog.i(TAG, "mBufferingUpdateListener: " + "  percent:" + percent);
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null) {
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
            }
        }
    };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            DebugLog.i(TAG, "onSeekComplete:");
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.onSeekComplete(mp);
            }
        }
    };
    private IMediaPlayer.OnRecordingStatusListener mOnRecordingListener = new IMediaPlayer.OnRecordingStatusListener() {
        @Override
        public void onRecordingStatus(IMediaPlayer mp, int what, int extra) {
            DebugLog.i(TAG, "mOnRecordingStatusListener: " + what + "##extra:" + extra);
            if (mOnRecordingStatusListener != null) {
                mOnRecordingStatusListener.onRecordingStatus(mp, what, extra);
            }
        }
    };
    private IMediaPlayer.OnRecordingProgressListener mOnRecProgressListener = new IMediaPlayer.OnRecordingProgressListener() {
        @Override
        public void OnRecordingProgress(IMediaPlayer mp, int count) {
            DebugLog.i(TAG, "OnRecordingProgress: " + count);
            if (mOnRecordingProgressListener != null) {
                mOnRecordingProgressListener.OnRecordingProgress(mp, count);
            }
        }

    };
    private IMediaPlayer.OnPullStreamListener onPullStreamListener = new IMediaPlayer.OnPullStreamListener() {
        @Override
        public void OnPullStream(IMediaPlayer mp) {
            if (mOnPullStreamListener != null) {
                mOnPullStreamListener.OnPullStream(mp);
            }
        }
    };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param mOnPreparedListener The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener mOnPreparedListener) {
        this.mOnPreparedListener = mOnPreparedListener;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param mOnCompletionListener The callback that will be run
     */
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener mOnCompletionListener) {
        this.mOnCompletionListener = mOnCompletionListener;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param mOnErrorListener The callback that will be run
     */
    public void setOnErrorListener(IMediaPlayer.OnErrorListener mOnErrorListener) {
        this.mOnErrorListener = mOnErrorListener;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param mInfoListener The callback that will be run
     */
    public void setOnInfoListener(IMediaPlayer.OnInfoListener mInfoListener) {
        this.mOnInfoListener = mInfoListener;
    }

    public void setmOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener) {
        this.mOnSeekCompleteListener = mOnSeekCompleteListener;
    }

    public void setmOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener) {
        this.mOnBufferingUpdateListener = mOnBufferingUpdateListener;
    }

    public void setmOnRecordingStatusListener(IMediaPlayer.OnRecordingStatusListener recordingStatusListener) {
        mOnRecordingStatusListener = recordingStatusListener;
    }

    public void setmOnRecordingProgressListener(IMediaPlayer.OnRecordingProgressListener listener) {
        mOnRecordingProgressListener = listener;
    }

    public void setmOnPullStreamListener(IMediaPlayer.OnPullStreamListener listener) {
        mOnPullStreamListener = listener;
    }

    // REMOVED: mSHCallback
    private void bindSurfaceHolder(IMediaPlayer mp, IRenderView.ISurfaceHolder holder) {
        if (mp == null) {
            return;
        }

        if (holder == null) {
            mp.setDisplay(null);
            return;
        }

        holder.bindToMediaPlayer(mp);
    }

    IRenderView.IRenderCallback mSHCallback = new IRenderView.IRenderCallback() {
        @Override
        public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int w, int h) {
            if (holder.getRenderView() != mRenderView) {
               DebugLog.e(TAG, "onSurfaceChanged: unmatched render callback\n");
                return;
            }

            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = !mRenderView.shouldWaitForResize() || (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {
            if (holder.getRenderView() != mRenderView) {
               DebugLog.e(TAG, "onSurfaceCreated: unmatched render callback\n");
                return;
            }

            mSurfaceHolder = holder;
            if (mMediaPlayer != null) {
                bindSurfaceHolder(mMediaPlayer, holder);
            } else {
                openVideo();
            }
        }

        @Override
        public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {
            if (holder.getRenderView() != mRenderView) {
               DebugLog.e(TAG, "onSurfaceDestroyed: unmatched render callback\n");
                return;
            }

            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            // REMOVED: if (mMediaController != null) mMediaController.hide();
            // REMOVED: release(true);
            releaseWithoutStop();
        }
    };


    public void releaseWithoutStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(null);
        }
    }

    /*
     * release the media player in any state
     */
    public void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            // REMOVED: mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    private boolean mPlay = true;

    @Override
    public void start() {
        if (isInPlaybackState()) {
             DebugLog.i(TAG, "mCurrentState  mMediaPlayer.start()\n");
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        Log.d(TAG,"退出Home键没有暂停, isInPlaybackState=" + isInPlaybackState());
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                Log.d(TAG,"退出Home键暂停了");
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }


    @Override
    public int getStartTime() {
        return 0;
    }


    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int toggleAspectRatio(int aspectRatio) {
        return 0;
    }

    @Override
    public int togglePlayer(@Settings.PlayerType int playerType) {
        return 0;
    }

    @Override
    public int getRecordSec() {
        return 0;
    }

    @Override
    public boolean isStatTimeShift() {
        return false;
    }

    public int getTargetState() {
        return mTargetState;
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }


    public boolean isInPlaybackState() {
       DebugLog.e(TAG,"mCurrentState is "+mCurrentState);
        //播放错误状态或者异常状态 STATE_ERROR==-1 默认状态STATE_IDLE==0 初始化状态STATE_PREPARING==1；
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    /**
     *
     * @param isVolume  true=关闭声音，false =打开
     */
    public void setVolume(boolean isVolume){
        if(mMediaPlayer!=null){
            if(isVolume){
                mMediaPlayer.setVolume(0,0);
            }else{
                AudioManager audioManager=(AudioManager)mAppContext.getSystemService(Service.AUDIO_SERVICE);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
                mMediaPlayer.setVolume(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM), audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
            }
        }
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public static final int PLAYER_IjkMediaPlayer = 1;
    public static final int PLAYER_AndroidMediaPlayer = 2;
    public static final int PLAYER_IjkExoMediaPlayer = 3;

    private int mLogLevel = IjkMediaPlayer.IJK_LOG_SILENT;

    public void setLogLevel(int level) {
        mLogLevel = level;
    }

    /**
     * 根据类型和编码方式来创建播放器
     *
     * @return
     */
    public IMediaPlayer createPlayer(int playerType, int encodeType) {
        IMediaPlayer mediaPlayer = null;
        switch (playerType) {
            case PLAYER_IjkExoMediaPlayer:
                IjkExoMediaPlayer IjkExoMediaPlayer = new IjkExoMediaPlayer(mAppContext);
                mediaPlayer = IjkExoMediaPlayer;
                break;
            case PLAYER_AndroidMediaPlayer:
                AndroidMediaPlayer androidMediaPlayer = new AndroidMediaPlayer();
                mediaPlayer = androidMediaPlayer;
                break;
            case PLAYER_IjkMediaPlayer:
                IjkMediaPlayer ijkMediaPlayer;
                if (mUri != null) {
                    ijkMediaPlayer = new IjkMediaPlayer();
                    if (Pragma.IJK_NATIVE_DEBUG) {
                        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
                        if (Pragma.IJK_LOG_FILE) {
                            //设置日志写入位置  0表示jkplayer写日志方式到文件根目录中, 1表示ijk日志写入跟java日志一起.
                            ijkMediaPlayer.setOption(IjkMediaPlayer.FFP_OPT_CATEGORY_PLAYER, "log-mode", 1);
                        }
                    } else {
                        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
                    }
                    if (encodeType == 1) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1);
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
                    }
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);  //1默认设置音频延迟,0不延迟
//                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", 0);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probsize", 1024 * 10);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "addrinfo_timeout", 3000000);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "scan_all_pmts", 0);
                    // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max-buffer-size", 1024);  //改变缓冲大小
                    if (TextUtils.isEmpty(mLocalHostIp)) {
                        mLocalHostIp = "127.0.0.1";
                    }
                    ijkMediaPlayer.setOption(2018003, mLocalHostIp, 0);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.FFP_OPT_CATEGORY_PLAYER, "play-type", mPlayType);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.FFP_OPT_CATEGORY_PLAYER, "is-new-pop", 1);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.FFP_OPT_CATEGORY_PLAYER, "soundtouch", 1);
                    mediaPlayer = ijkMediaPlayer;
                    mIjkMediaPlayer = ijkMediaPlayer;
                    break;
                }
        }
        return mediaPlayer;
    }

    /**
     * 三个参数只用前两个，第一个符合2018000 ~2018002 时，会丢弃第三个输入的参数
     *
     * @param path 缓存路径
     * @param name 缓存名称
     * @param time 录制时间
     */
    public void startRecording(String path, String name, String time) {
        if (mIjkMediaPlayer != null) {
            mIjkMediaPlayer.setOption(2018000, path, 0);
            mIjkMediaPlayer.setOption(2018001, name, 0);
            mIjkMediaPlayer.setOption(2018002, time, 0);
        }
    }

    public void stopRecording() {
        if (mIjkMediaPlayer != null) {
            mIjkMediaPlayer.setOption(2018002, "0", 0);
        }
    }

    /**
     * 1表示硬解;非1表示软解
     *
     * @param decodeType
     */
    public void setDecodeType(int decodeType) {
        this.decodeType = decodeType;
        if (decodeType == 1) {
            playerType = PLAYER_IjkMediaPlayer;   //硬解
        } else {
            playerType = PLAYER_IjkMediaPlayer;   //软解
        }
    }

    public int getmCurrentState() {
        return mCurrentState;
    }

    /**
     * 设置视频比例
     *
     * @param type
     * @return
     */
    public boolean setVideoSize(int type) {
        if (mRenderView != null) {
            mRenderView.setAspectRatio(type);
            videoSizeType = type;
            return true;
        }
        return false;
    }

    public void setSpeed(float speed) {
        if (mIjkMediaPlayer != null) {
            mIjkMediaPlayer.setSpeed(speed);
        }
    }

    private String mLocalHostIp = "";

    public void setLocalHostIp(String localHostIp) {
        mLocalHostIp = localHostIp;
    }

    private int mPlayType = IjkMediaPlayer.PLAY_TYPE_VOD;

    /**
     * 设备播放模式的类型
     * LIVE {@link IjkMediaPlayer#PLAY_TYPE_LIVE}
     * LIVE_PLAYBACK {@link IjkMediaPlayer#PLAY_TYPE_LIVE_PLAYBACK}
     * LIVE_TIMESHIFT {@link IjkMediaPlayer#PLAY_TYPE_LIVE_TIMESHIFT}
     * <p>
     * VOD {@link IjkMediaPlayer#PLAY_TYPE_VOD}
     *
     * @param playType
     */
    public void setPlayType(int playType) {
        mPlayType = playType;
    }

    public void setPlayerType(int playerType) {
        this.playerType = playerType;
    }

    public int getDecodeType() {
        return decodeType;
    }

//    public void setVolume(boolean isVolume) {
//        //TODO 代码忘了提交
//    }

    /**
     * 分辨率不支持
     */
    public interface PowerError {
        public void powerError();
    }

    public IjkMediaPlayer getIjkMediaPlayer() {
        return mIjkMediaPlayer;
    }
}

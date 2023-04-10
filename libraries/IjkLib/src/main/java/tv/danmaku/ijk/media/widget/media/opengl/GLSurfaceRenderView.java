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

package tv.danmaku.ijk.media.widget.media.opengl;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import tv.danmaku.ijk.media.DebugLog;
import tv.danmaku.ijk.media.widget.media.IMediaPlayer;
import tv.danmaku.ijk.media.widget.media.IRenderView;
import tv.danmaku.ijk.media.widget.media.ISurfaceTextureHolder;
import tv.danmaku.ijk.media.widget.media.MeasureHelper;


public class GLSurfaceRenderView extends GLSurfaceView implements IRenderView {
    private MeasureHelper mMeasureHelper;

    public GLSurfaceRenderView(Context context) {
        super(context);
        initView(context);
    }

    public GLSurfaceRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);

        mMeasureHelper = new MeasureHelper(this);
        mSurfaceCallback = new SurfaceCallback(this, new VideoDrawer());
        setRenderer(mSurfaceCallback);
        //noinspection deprecation
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean shouldWaitForResize() {
        return true;
    }

    //--------------------
    // Layout & Measure
    //--------------------
    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            getHolder().setFixedSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    @Override
    public void setVideoRotation(int degree) {
       DebugLog.e("", "SurfaceView doesn't support rotation (" + degree + ")!\n");
    }

    @Override
    public void setAspectRatio(int aspectRatio) {
        mMeasureHelper.setAspectRatio(aspectRatio);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }

    //--------------------
    // SurfaceViewHolder
    //--------------------

    private static final class InternalSurfaceHolder implements ISurfaceHolder {
        private GLSurfaceRenderView mGLRenderView;
        private SurfaceTexture mSurfaceTexture;
        public InternalSurfaceHolder(@NonNull GLSurfaceRenderView glRenderView,
                                     @Nullable SurfaceTexture surfaceTexture) {
            mGLRenderView = glRenderView;
            mSurfaceTexture = surfaceTexture;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void bindToMediaPlayer(IMediaPlayer mp) {
            if (mp != null) {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) &&
                        (mp instanceof ISurfaceTextureHolder)) {
                    ISurfaceTextureHolder textureHolder = (ISurfaceTextureHolder) mp;
                    textureHolder.setSurfaceTexture(null);
                }
                mp.setSurface(openSurface());
            }
        }

        @NonNull
        @Override
        public IRenderView getRenderView() {
            return mGLRenderView;
        }

        @Nullable
        @Override
        public SurfaceHolder getSurfaceHolder() {
            return null;
        }

        @Nullable
        @Override
        public SurfaceTexture getSurfaceTexture() {
            return mSurfaceTexture;
        }

        @Nullable
        @Override
        public Surface openSurface() {
            if (mSurfaceTexture == null) {
                return null;
            }
            return new Surface(mSurfaceTexture);
        }
    }

    //-------------------------
    // SurfaceHolder.Callback
    //-------------------------

    @Override
    public void addRenderCallback(IRenderCallback callback) {
        mSurfaceCallback.addRenderCallback(callback);
    }

    @Override
    public void removeRenderCallback(IRenderCallback callback) {
        mSurfaceCallback.removeRenderCallback(callback);
    }

    private SurfaceCallback mSurfaceCallback;

    private static final class SurfaceCallback implements Renderer {
        private IDrawer mDrawer;
        private SurfaceTexture mSurfaceTexture;
        private boolean mIsFormatChanged;
        private int mWidth;
        private int mHeight;

        private WeakReference<GLSurfaceRenderView> mWeakSurfaceView;
        private Map<IRenderCallback, Object> mRenderCallbackMap = new ConcurrentHashMap<IRenderCallback, Object>();

        public SurfaceCallback(@NonNull GLSurfaceRenderView surfaceView, IDrawer drawer) {
            mWeakSurfaceView = new WeakReference<GLSurfaceRenderView>(surfaceView);
            mDrawer = drawer;
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                }
                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    onSurfaceDestroyed(holder);
                }
            });
        }

        public void addRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.put(callback, callback);

            ISurfaceHolder surfaceHolder = null;
            if (mSurfaceTexture != null) {
                if (surfaceHolder == null)
                    surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceTexture);
                callback.onSurfaceCreated(surfaceHolder, mWidth, mHeight);
            }

            if (mIsFormatChanged) {
                if (surfaceHolder == null) {
                    surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceTexture);
                }
                callback.onSurfaceChanged(surfaceHolder, 0, mWidth, mHeight);
            }
        }

        public void removeRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.remove(callback);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mSurfaceTexture = mDrawer.onCreate(config);
            mIsFormatChanged = false;
            mWidth = 0;
            mHeight = 0;
            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceTexture);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceCreated(surfaceHolder, 0, 0);
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mIsFormatChanged = true;
            mWidth = width;
            mHeight = height;
            mDrawer.onSizeChanged(width, height);
            // mMeasureHelper.setVideoSize(width, height);

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceTexture);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceChanged(surfaceHolder, 0, width, height);
            }
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mDrawer.doDraw();
        }

        public void onSurfaceDestroyed(SurfaceHolder holder) {
            mIsFormatChanged = false;
            mWidth = 0;
            mHeight = 0;
            mDrawer.release();
            mSurfaceTexture = null;
            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakSurfaceView.get(), mSurfaceTexture);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceDestroyed(surfaceHolder);
            }
        }
    }

    //--------------------
    // Accessibility
    //--------------------

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(GLSurfaceRenderView.class.getName());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            info.setClassName(GLSurfaceRenderView.class.getName());
        }
    }
}

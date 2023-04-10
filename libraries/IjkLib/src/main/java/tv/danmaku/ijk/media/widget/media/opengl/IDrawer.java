package tv.danmaku.ijk.media.widget.media.opengl;

import android.graphics.SurfaceTexture;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/9/16
 * 描    述:
 * 修订历史：
 * ================================================
 */
public interface IDrawer {
    SurfaceTexture onCreate(EGLConfig config);
    void onSizeChanged(int width, int height);
    void doDraw();
    void release();
}

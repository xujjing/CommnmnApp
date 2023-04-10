package tv.danmaku.ijk.media.widget.media.opengl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/9/18
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class VideoDrawer implements IDrawer {

    private final float[] vertexCoors = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f,  1.0f,
            1.0f,  1.0f
    };
    private final float[] textureCoors = {
            0.0f,  1.0f,
            1.0f,  1.0f,
            0.0f,  0.0f,
            1.0f,  0.0f
    };

    private final String vertexShaderSource =
            "attribute vec4 aPosition;" +
            "attribute vec2 aCoordinate;" +
            "varying vec2 vCoordinate;" +
            "void main() {" +
            "  gl_Position = aPosition;" +
            "  vCoordinate = aCoordinate;" +
            "}";

    private final String fragmentShaderSource =
            //一定要加换行"\n"，否则会和下一行的precision混在一起，导致编译出错
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES uTexture;" +
            "varying vec2 vCoordinate;" +

            "vec4 tmp(vec3 colorrbg){" +
            "   float brightness = 50.0;" +
            "   float contrast = 100.0;" +
            "   float PI = 3.1415926;" +
            "	float B = brightness / 255.0;" +
            "	float c = contrast / 255.0;" +
            "	float k = tan((45.0 + 44.0 * c) / 180.0 * PI);" +
            "	colorrbg = ((colorrbg*255.0 - 127.5 * (1.0 - B)) * k + 127.5 * (1.0 + B)) / 255.0;" +
            "    return vec4(colorrbg,1.0);" +
            "}" +

            "void main() {" +
            "  float vc1X = 0.5 * vCoordinate.x;" +
            "  float vc1Y = vCoordinate.y;" +
            "  vec2 vc1 = vec2(vc1X, vc1Y);" +
            "  vec4 color1 = texture2D(uTexture, vc1);" +

            "  float vc2X = 0.49875 +  0.5 * vCoordinate.x;" +
            "  float vc2Y = vCoordinate.y;" +
            "  vec2 vc2 = vec2(vc2X, vc2Y);" +
            "  vec4 color2 = texture2D(uTexture, vc2);" +

            "  float gray2 = (color2.r + color2.g + color2.b)/3.0;" +
            "  float clg2 = color2.g * 2.0/3.0;" +
            "  float clb2 = color2.b * 2.0/3.0;" +
            "  vec3 tmpColor = vec3(color1.r, clg2, clb2);" +

            "  gl_FragColor = tmp(tmpColor);" +
            "}";

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private int mProgram;
    private int mVertexPosHandler;
    private int mTextureHandler;
    private int mTexturePosHandler;


    @Override
    public SurfaceTexture onCreate(EGLConfig config) {
        mTextureId = createTextureId();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        //初始化顶点数据
        initPos();
        //初始化GL渲染程序
        initGLPrg();
        return mSurfaceTexture;
    }

    private int createTextureId() {
        int[] ids = new int[1];
        GLES20.glGenTextures(1, ids, 0);
        return ids[0];
    }

    private void initPos() {
        ByteBuffer b1 = ByteBuffer.allocateDirect(vertexCoors.length * 4);
        b1.order(ByteOrder.nativeOrder());
        mVertexBuffer = b1.asFloatBuffer();
        mVertexBuffer.put(vertexCoors);
        mVertexBuffer.position(0);

        ByteBuffer b2 = ByteBuffer.allocateDirect(textureCoors.length * 4);
        b2.order(ByteOrder.nativeOrder());
        mTextureBuffer = b2.asFloatBuffer();
        mTextureBuffer.put(textureCoors);
        mTextureBuffer.position(0);
    }

    private void initGLPrg() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);

        //创建OpenGL ES程序，注意：需要在OpenGL渲染线程中创建，否则无法渲染
        mProgram = GLES20.glCreateProgram();
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);

        mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTexturePosHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate");
        mTextureHandler = GLES20.glGetUniformLocation(mProgram, "uTexture");
    }


    @Override
    public void onSizeChanged(int width, int height) {

    }

    @Override
    public void doDraw() {
        //使用程序
        GLES20.glUseProgram(mProgram);
        //激活并绑定纹理单元
        activateTexture();
        //更新纹理
        mSurfaceTexture.updateTexImage();

        //启用顶点的句柄
        GLES20.glEnableVertexAttribArray(mVertexPosHandler);
        GLES20.glEnableVertexAttribArray(mTexturePosHandler);
        //设置着色器参数， 第二个参数表示一个顶点包含的数据数量，这里为xy，所以为2
        GLES20.glVertexAttribPointer(mVertexPosHandler, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mTexturePosHandler, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }


    private void activateTexture() {
        //激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定纹理ID到纹理单元
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        //将激活的纹理单元传递到着色器里面
        GLES20.glUniform1i(mTextureHandler, 0);
        //配置边缘过渡参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }


    @Override
    public void release() {
        GLES20.glDisableVertexAttribArray(mVertexPosHandler);
        GLES20.glDisableVertexAttribArray(mTexturePosHandler);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        GLES20.glDeleteProgram(mProgram);
    }


    private int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        return shader;
    }
}

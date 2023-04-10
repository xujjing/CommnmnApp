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

package tv.danmaku.ijk.media.widget.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import tv.danmaku.ijk.media.playerLib.R;


public class Settings {
    private Context mAppContext;
    private SharedPreferences mSharedPreferences;

    public static final int PV_PLAYER__AndroidMediaPlayer = 1;
    public static final int PV_PLAYER__IjkMediaPlayer = 2;
    public static final int PV_PLAYER__IjkExoMediaPlayer = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            PV_PLAYER__AndroidMediaPlayer,
            PV_PLAYER__IjkMediaPlayer,
            PV_PLAYER__IjkExoMediaPlayer
    })
    public @interface PlayerType {
    }

    public static final int RENDER_NONE = 0;
    public static final int RENDER_SURFACE_VIEW = 1;
    public static final int RENDER_TEXTURE_VIEW = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            RENDER_NONE,
            RENDER_SURFACE_VIEW,
            RENDER_TEXTURE_VIEW
    })
    public @interface RenderType {
    }


    public Settings(Context context) {
        mAppContext = context.getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }

    public int getPlayerType() {
        String key = mAppContext.getString(R.string.pref_key_player);
        return mSharedPreferences.getInt(key, PV_PLAYER__IjkMediaPlayer);
    }

    public void setPlayerType(@PlayerType int playerType) {
        String key = mAppContext.getString(R.string.pref_key_player);
        mSharedPreferences.edit().putInt(key, playerType).apply();
    }

    public int getRenderType() {
        String key = mAppContext.getString(R.string.pref_key_render);
        return mSharedPreferences.getInt(key, RENDER_SURFACE_VIEW);
    }

    public boolean setRenderType(@RenderType int renderType) {
        String key = mAppContext.getString(R.string.pref_key_render);
        if (renderType == RENDER_TEXTURE_VIEW && (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
            return false;
        }
        mSharedPreferences.edit().putInt(key, renderType).apply();
        return true;
    }

    public boolean getUsingMediaCodec() {
        String key = mAppContext.getString(R.string.pref_key_using_media_codec);
        return mSharedPreferences.getBoolean(key, true);
    }

    public boolean getUsingMediaCodecAutoRotate() {
        String key = mAppContext.getString(R.string.pref_key_using_media_codec_auto_rotate);
        return mSharedPreferences.getBoolean(key, true);
    }

    public boolean getMediaCodecHandleResolutionChange() {
        String key = mAppContext.getString(R.string.pref_key_media_codec_handle_resolution_change);
        return mSharedPreferences.getBoolean(key, true);
    }

    public boolean getUsingOpenSLES() {
        String key = mAppContext.getString(R.string.pref_key_using_opensl_es);
        return mSharedPreferences.getBoolean(key, false);
    }

    public String getPixelFormat() {
        String key = mAppContext.getString(R.string.pref_key_pixel_format);
        return mSharedPreferences.getString(key, "");
    }

    public boolean getEnableDetachedSurfaceTextureView() {
        String key = mAppContext.getString(R.string.pref_key_enable_detached_surface_texture);
        return mSharedPreferences.getBoolean(key, false);
    }

    public boolean getUsingMediaDataSource() {
        String key = mAppContext.getString(R.string.pref_key_using_mediadatasource);
        return mSharedPreferences.getBoolean(key, false);
    }

    public String getLastDirectory() {
        String key = mAppContext.getString(R.string.pref_key_last_directory);
        return mSharedPreferences.getString(key, "/storage/emulated/0/");
    }

    public void setLastDirectory(String path) {
        String key = mAppContext.getString(R.string.pref_key_last_directory);
        mSharedPreferences.edit().putString(key, path).apply();
    }
}

package com.streambus.basemodule.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;

import androidx.core.content.SharedPreferencesCompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2018/2/26
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class PreferencesUtils {

    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;

    public static void init(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sp.edit();
    }

    public static void put(String key, Parcelable parcelable){
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        String value = Base64.encodeToString(parcel.marshall(), Base64.DEFAULT);
        put(key, value);
    }


    public static <T extends Parcelable> T get(String key, Parcelable.Creator<T> creator) {
        String value = get(key, "");
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        Parcel parcel = Parcel.obtain();
        byte[] decode = Base64.decode(value, Base64.DEFAULT);
        parcel.unmarshall(decode, 0, decode.length);
        parcel.setDataPosition(0);//这句话非常重要
        return creator.createFromParcel(parcel);
    }

    public static void put(String key, Serializable object) {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key is empty");
        }
        if (object == null){
            if (sp.contains(key)){
                editor.remove(key);
                editor.apply();
            }
            return;
        }
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            ByteArrayOutputStream outputStream;
            ObjectOutputStream objectOutputStream = null;
            String value = "";
            try {
                outputStream = new ByteArrayOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(object);
                value = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                editor.putString(key, value);
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        editor.apply();
    }


    public static <T extends Serializable> T get(String key, T defaut) {
        if (TextUtils.isEmpty(key)) {
            return defaut;
        }
        T value = defaut;
        if (defaut instanceof String) {
            value = (T) sp.getString(key, (String) defaut);
        } else if (defaut instanceof Integer) {
            value = (T) new Integer(sp.getInt(key, (Integer) defaut));
        } else if (defaut instanceof Boolean) {
            value = (T) new Boolean(sp.getBoolean(key, (Boolean) defaut));
        } else if (defaut instanceof Float) {
            value = (T) new Float(sp.getFloat(key, (Float) defaut));
        } else if (defaut instanceof Long) {
            value = (T) new Long(sp.getLong(key, (Long) defaut));
        } else {
            String str = sp.getString(key, "");
            if (TextUtils.isEmpty(str)) {
                return defaut;
            }
            ByteArrayInputStream inputStream;
            ObjectInputStream objectInputStream = null;
            try {
                inputStream = new ByteArrayInputStream(Base64.decode(str, Base64.DEFAULT));
                objectInputStream = new ObjectInputStream(inputStream);
                value = (T) objectInputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return value;
    }


    public static void clear() {
        editor.clear();
        editor.commit();
    }

}

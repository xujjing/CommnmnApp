package com.streambus.basemodule.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;

import androidx.core.content.ContextCompat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2018/2/26
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class SecurePreferences {
    private static final String TAG = "SecurePreferences";
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private static final String HEX = "0123456789ABCDEF";
    private static String CHECK_MD5;
    private static String AES = "iQKBgQCvXe+pNk";
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCvXe+pNk/oqMRL5wdjG5CWPxAK0lNoHqanS2NsGyej2SgO6yD6MtUFmrhdNnhe0rlGE9U5zrEEHwjiLPVE+SQ9atmMo0GTZwsI9drBkm0vSYjYIv5c7Uy5c0HZCcjCxGvQDPU6MmhtA4f4GUOD0XWYhqWO+U0spkh8uZGVq7CIXQIDAQAB";


    public static void init(Context context) {
        String androidId = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        AES = keyMd5(PUBLIC_KEY + androidId);
        AES = keyMd5(androidId + getSecureModified(context, keyMd5(androidId)));
        sp = context.getSharedPreferences(context.getPackageName() + "_" + md5(AES.getBytes()), Context.MODE_PRIVATE);
        editor = sp.edit();
        saveAesKey();
    }

    public static void initAfter(Context context) {
        if (!TextUtils.isEmpty(CHECK_MD5)) {
            File checkFile = null;
            try {
                SLog.d(TAG, "initAfter properties");
                String absolutePath = context.getExternalCacheDir().getAbsolutePath();
                String anDir = absolutePath.substring(0, absolutePath.lastIndexOf("data"));
                checkFile = new File(anDir + ".android_baoluter.properties");
                FileOutputStream ops = new FileOutputStream(checkFile, true);
                ops.write((CHECK_MD5 + '\n').getBytes());
                ops.close();
                CHECK_MD5 = null;
            } catch (Exception e) {
                SLog.e(TAG, "initAfter Exception", e);
                if (checkFile != null) {
                    checkFile.delete();
                }
            }
        }
    }

    private static String getSecureModified(Context context,String keyMd5) {
        SLog.d(TAG, "init getSecureModified");
        File file = new File(context.getFilesDir(), "aboulter");
        int rate = 0;
        if (file.exists() && file.isFile()) {
            try {
                FileInputStream ips = new FileInputStream(file);
                byte[] buff = new byte[4096];
                int len = ips.read(buff); ips.close();
                String str1 = new String(buff, 0, len);
                rate = 1;
                byte[] decrypt = decrypt(str1, keyMd5);
                rate = 2;
                long readModified = Long.valueOf(new String(decrypt));
                long lastModified = file.lastModified();
                if (Math.abs(lastModified - readModified) < 2000) {
                    rate = 3;
                    int permissionResult = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                        rate = 4;
                        String absolutePath = context.getExternalCacheDir().getAbsolutePath();
                        String anDir = absolutePath.substring(0, absolutePath.lastIndexOf("data"));
                        File checkFile = new File(anDir + ".android_baoluter.properties");
                        if (checkFile.exists() && checkFile.isFile()) {
                            rate = 5;
                            BufferedReader reader = new BufferedReader(new FileReader(checkFile));
                            String str1Md5 = keyMd5(str1);
                            boolean isCheck = false;
                            String str2;
                            while ((str2 = reader.readLine()) != null) {
                                if (str1Md5.equals(str2)) {
                                    isCheck = true;
                                    break;
                                }
                            }
                            reader.close();
                            if (isCheck) {
                                return String.valueOf(readModified);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                SLog.e(TAG, "getSecureModified read rate=>" + rate, e);
            }
        }
        SLog.w(TAG, "getSecureModified read rate=>" + rate);
        try {
            file.delete();
            long writeModified = System.currentTimeMillis();
            String encrypt = encrypt(String.valueOf(writeModified).getBytes(), keyMd5);
            FileOutputStream ops = new FileOutputStream(file);
            ops.write(encrypt.getBytes());
            ops.close();
            file.setLastModified(writeModified);
            CHECK_MD5 = keyMd5(encrypt);
            return String.valueOf(writeModified);
        } catch (Exception e) {
            SLog.e(TAG, "getSecureModified write", e);
        }
        return keyMd5;
    }

    private static void saveAesKey(){
        if (sp.contains("AES_KEY")) {
            return;
        }
        try {
            // 得到公钥对象
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(PUBLIC_KEY, Base64.NO_WRAP));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            // 加密数据
            Cipher cp = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cp.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] bytes = cp.doFinal(AES.getBytes("UTF-8"));
            editor.putString("AES_KEY", new String(Base64.encode(bytes, Base64.NO_WRAP), "UTF-8"));
            editor.apply();
        } catch (Exception e) {
            SLog.e(TAG, "saveAesKey", e);
        }
    }

    public static void put(String key, Parcelable parcelable){
        String keyMd5;
        if ((keyMd5 = checkPutError(key, parcelable)) == null) {
            return;
        }
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        byte[] value = parcel.marshall();
        String encrypt = encrypt(value, keyMd5);
        if (encrypt != null) {
            editor.putString(keyMd5, encrypt);
            editor.apply();
        }
    }


    public static <T extends Parcelable> T get(String key, Parcelable.Creator<T> creator) {
        String keyMd5;
        if (key!=null && (keyMd5 = keyMd5(key)) != null) {
            String str = sp.getString(keyMd5, "");
            if (!TextUtils.isEmpty(str)) {
                byte[] decrypt = decrypt(str, keyMd5);
                Parcel parcel = Parcel.obtain();
                if (decrypt != null) {
                    parcel.unmarshall(decrypt, 0, decrypt.length);
                    parcel.setDataPosition(0);//这句话非常重要
                    return creator.createFromParcel(parcel);
                }
            }
        }
        return null;
    }

    public static void put(String key, Serializable object) {
        String keyMd5;
        byte[] value;
        if ((keyMd5 = checkPutError(key, object)) == null) {
            return;
        }
        if (object instanceof String || object instanceof Integer || object instanceof Boolean || object instanceof Float || object instanceof Long) {
            String str = object.toString();
            if (TextUtils.isEmpty(str)) {
                if (sp.contains(keyMd5)) {
                    editor.remove(keyMd5);
                    editor.apply();
                }
                return;
            }
            value = str.getBytes();
        }else {
            value = objToByte(object);
            if (value == null) {
                if (sp.contains(keyMd5)) {
                    editor.remove(keyMd5);
                    editor.apply();
                }
                return;
            }
        }
        String encrypt = encrypt(value, keyMd5);
        if (encrypt != null) {
            editor.putString(keyMd5, encrypt);
            editor.apply();
        }
    }

    public static <T extends Serializable> T get(String key, T defaut) {
        String keyMd5;
        if (key!=null && (keyMd5 = keyMd5(key)) != null) {
            String str = sp.getString(keyMd5, "");
            if (!TextUtils.isEmpty(str)) {
                byte[] decrypt = decrypt(str, keyMd5);
                if (decrypt != null) {
                    T value;
                    if (defaut instanceof String) {
                        value = (T) new String(decrypt);
                    } else if (defaut instanceof Integer) {
                        value = (T) new Integer(new String(decrypt));
                    } else if (defaut instanceof Boolean) {
                        value = (T) new Boolean(new String(decrypt));
                    } else if (defaut instanceof Float) {
                        value = (T) new Float(new String(decrypt));
                    } else if (defaut instanceof Long) {
                        value = (T) new Long(new String(decrypt));
                    } else {
                        value = objFromByte(decrypt);
                    }
                    return value;
                }
            }
        }
        return defaut;
    }



    /************************************* - Private - ****************************************/

    private static String keyMd5(String key) {
        return md5((key + AES).getBytes());
    }

    private static String md5(byte[] datasource) {
        try {
            byte[] buf = MessageDigest.getInstance("MD5").digest(datasource);
            StringBuffer sb = new StringBuffer(2 * buf.length);
            for (int i = 0; i < buf.length; i++) {
                byte b = buf[i];
                sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String checkPutError(String key, Object value) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        String keyMd5 = keyMd5(key);
        if (keyMd5 != null && value == null) {
            if (sp.contains(keyMd5)) {
                editor.remove(keyMd5);
                editor.apply();
            }
            return null;
        }
        return keyMd5;
    }

    private static String encrypt(byte[] value, String keyMd5){
        try {
            String aesKey = md5((AES + keyMd5).getBytes());
            SecretKeySpec skeySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypt = cipher.doFinal(value);
            String valueMd5 = md5(value);
            String data = Base64.encodeToString(encrypt, Base64.DEFAULT);
            //SLog.d(TAG, "encrypt aesKey=>" + aesKey + " valueMd5=>" + valueMd5 + "  data=>" + data);
            return valueMd5 + data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decrypt(String str, String keyMd5){
        try {
            String aesKey = md5((AES + keyMd5).getBytes());
            String valueMd5 = str.substring(0, 32);
            String data = str.substring(32);
            byte[] encrypt = Base64.decode(data, Base64.DEFAULT);

            //SLog.d(TAG, "decrypt aesKey=>" + aesKey + " valueMd5=>" + valueMd5 + "  data=>" + data);

            SecretKeySpec skeySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] value = cipher.doFinal(encrypt);
            if (md5(value).equals(valueMd5)) {
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] objToByte(Serializable object) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T extends Serializable> T objFromByte(byte[] bytes) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            return  (T) object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

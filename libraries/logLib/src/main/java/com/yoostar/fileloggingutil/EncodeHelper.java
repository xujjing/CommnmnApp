package com.yoostar.fileloggingutil;

import android.util.Base64;
import android.util.Log;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/9/17
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class EncodeHelper {
    private static final String TAG = "EncodeHelper";
    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCvXe+pNk/oqMRL5wdjG5CWPxAK0lNoHqanS2NsGyej2SgO6yD6MtUFmrhdNnhe0rlGE9U5zrEEHwjiLPVE+SQ9atmMo0GTZwsI9drBkm0vSYjYIv5c7Uy5c0HZCcjCxGvQDPU6MmhtA4f4GUOD0XWYhqWO+U0spkh8uZGVq7CIXQIDAQAB";
    private final IEncodeStateChanger mEncodeStateChanger;
    private Cipher mEnCipher;

    private String mDecodeAESKey;
    private String mRandomKey;

    private int mGenerateAESCount;
    private static final int MAX_GENERATEAESCOUNT = 10;

    public EncodeHelper(IEncodeStateChanger iEncodeStateChanger) {
        mEncodeStateChanger = iEncodeStateChanger;
        generateKey();
    }

    public interface IEncodeStateChanger{
        void onEncodeState(boolean isEncode);
    }

    public String getDecodeAESKey() {
        return mDecodeAESKey;
    }

    public byte[] encodeAES(byte[] content) {
        do {
            try {
                byte[] bytes = mEnCipher.doFinal(content);
                return Base64.encode(bytes, Base64.NO_WRAP);
            } catch (Exception ignore) {
            }
        } while (tryGenerateAES());

        return content;
    }

    private void generateKey() {
        int tryCount = 0;
        do {
            try {
                mRandomKey = getRandomString(32);
                byte[] bytes = encryptByPublicKey(mRandomKey.getBytes("UTF-8"), Base64.decode(PUBLIC_KEY, Base64.NO_WRAP));
                mEnCipher = generateAESCipher(mRandomKey);
                mDecodeAESKey = new String(Base64.encode(bytes, Base64.NO_WRAP), "UTF-8");
                Log.i(TAG,"init encode success");
                mEncodeStateChanger.onEncodeState(true);
                return;
            } catch (Exception e) {
               Log.e(TAG,"init encode error", e);
                mEncodeStateChanger.onEncodeState(false);
            }
        } while (tryCount++ > 3);
    }

    private boolean tryGenerateAES() {
        try {
            if (mGenerateAESCount++ < MAX_GENERATEAESCOUNT) {
                mEnCipher = generateAESCipher(mRandomKey);
                return true;
            } else {
                mEncodeStateChanger.onEncodeState(false);
            }
        } catch (Exception e) {
           Log.e(TAG, "encodeAES generateAESCipher error", e);
        }
        return false;
    }



    /**
     * 随机生成字符串
     *
     * @param length
     * @return
     */
    private static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    private static Cipher generateAESCipher(String password) throws Exception {
        byte[] rawKey = MessageDigest.getInstance("MD5").digest(password.getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher;
    }


    /**
     * RSA算法
     */
    private static final String RSA_ANDROID = "RSA/ECB/PKCS1Padding";

    /**
     * 使用公钥加密
     */
    private static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) throws Exception {
        // 得到公钥对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        // 加密数据
        Cipher cp = Cipher.getInstance(RSA_ANDROID);
        cp.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] bytes = cp.doFinal(data);
        return bytes;
    }

}

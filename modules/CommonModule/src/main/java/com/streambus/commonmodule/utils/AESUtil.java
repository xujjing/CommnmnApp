package com.streambus.commonmodule.utils;


import android.util.Base64;

import com.streambus.basemodule.utils.SLog;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    private static final String TAG = "AESUtil";

    /**
     * AES加密
     *
     * @param dataSource 需要加密的数据
     * @param password   秘钥
     * @return 加密后的密文(返回str类型)
     */
    public static String encryptHex(String password, String dataSource)
            throws Exception {
        byte[] rawKey = getRawKey(password);
        byte[] result = encrypt(rawKey, dataSource.getBytes("UTF-8"));
        return toHex(result);
    }


    /**
     * AES解密
     *
     * @param datasource 需要解密的数据
     * @param password   秘钥
     * @return 解密后的明文
     */
    public static String decryptHex(String password, String datasource)
            throws Exception {
        byte[] rawKey = getRawKey(password);
        byte[] enc = toByte(datasource);
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }

    private static byte[] getRawKey(String password) throws Exception {
        return MessageDigest.getInstance("MD5").digest(password.getBytes("UTF-8"));
    }

    private static byte[] encrypt(byte[] rawKey, byte[] datasource) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(datasource);
    }

    private static byte[] decrypt(byte[] rawKey, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(encrypted);
    }

    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    private static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }



    /************************************* - BASE64 Cipher - ****************************************/
    private static final String ENCODING = "UTF-8";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    /**
     * AES 加密操作
     */
    public static String encryptBase64(String content, String key) {
        return encryptBase64(content, key, Base64.NO_WRAP);
    }
    /**
     * AES 解密操作
     */
    public static String decryptBase64(String content, String key) {
        return decryptBase64(content, key, Base64.NO_WRAP);
    }
    /**
     * AES 加密操作
     */
    public static String encryptBase64(String content, String key, int base64Frag) {
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            byte[] byteContent = content.getBytes(ENCODING);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key));
            byte[] result = cipher.doFinal(byteContent);
            String toHex = toHex(result);
            SLog.e(TAG, "encryptBase64, toHex=" + toHex);
            return Base64.encodeToString(result, base64Frag);
        } catch (Exception e) {
            SLog.e(TAG, "encrypt Exception", e);
        }
        return null;
    }
    /**
     * AES 解密操作
     */
    public static String decryptBase64(String content, String key, int base64Frag) {
        try {
            //实例化
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key));
            byte[] encryptContent = Base64.decode(content, base64Frag);
            //执行操作
            byte[] result = cipher.doFinal(encryptContent);
            return new String(result, ENCODING);
        } catch (Exception e) {
            SLog.e(TAG, "decrypt Exception", e);
        }
        return null;
    }
    /**
     * 生成加密秘钥
     */
    private static SecretKeySpec getSecretKey(final String key) throws Exception {
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        byte[] hash = MessageDigest.getInstance("MD5").digest(key.getBytes(ENCODING));
        return new SecretKeySpec(hash, "AES");
    }

    //========刘明 接口加密使用 begin==========================//
    /**
     * 加密
     *
     * @param content
     * @param encryptKey
     * @return
     * @throws Exception
     */
    public static String aesEncryptForLiuMing(String content, String encryptKey) throws Exception {
        return base64Encode(aesEncryptToBytes(content, encryptKey));
    }

    public static String base64Encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.URL_SAFE|Base64.NO_WRAP/*Base64.DEFAULT*/);//.encodeBase64String(bytes);
    }

    public static byte[] aesEncryptToBytes(String content, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
        return cipher.doFinal(content.getBytes("utf-8"));
    }
    //========end 刘明 接口加密使用==========================//
}

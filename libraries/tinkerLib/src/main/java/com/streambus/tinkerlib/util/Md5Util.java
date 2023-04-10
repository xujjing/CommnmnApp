package com.streambus.tinkerlib.util;

import android.annotation.SuppressLint;
import android.util.Log;

import com.tencent.tinker.lib.util.TinkerLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by huanglu on 2017/5/23.
 */

public class Md5Util {
    private static final String TAG = "Md5Util";
    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    protected static MessageDigest mMessageDigest = null;
    static {
        try {
            mMessageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    /**
     * 生成文件的md5校验值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(File file) throws IOException {
        InputStream fis;
        fis = new FileInputStream(file);
        byte[] mBuffer = new byte[1024*1024];
        int numRead = 0;
        while ((numRead = fis.read(mBuffer)) > 0) {
            mMessageDigest.update(mBuffer, 0, numRead);
        }
        fis.close();
        return bufferToHex(mMessageDigest.digest());
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    /**
     *  md5校验
     * @param filePath 文件完成路径
     * @param verifyMd5 对比的MD5
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static boolean validateMD5(String filePath, String verifyMd5) {
        TinkerLog.i(TAG, "filePath = " + filePath);
        if (filePath == null || filePath == "" || verifyMd5 == null
                || verifyMd5 == "") {
            return false;
        }
        verifyMd5 = verifyMd5.toLowerCase().trim();
        boolean ret = false;
        try {
            File file = new File(filePath);
            Md5Util md5Util = new Md5Util();
            String fileMD5 = md5Util.getFileMD5String(file);
            TinkerLog.i(TAG, "verifyMd5 = " + verifyMd5 + ", fileMD5 = " + fileMD5);
            if (fileMD5 != null && fileMD5.equals(verifyMd5))
                ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 计算一个字符串的md5
     *
     * @param input
     * @return
     */
    public static String stringMD5(String input) {
        try {
            // 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 输入的字符串转换成字节数组
            byte[] inputByteArray = input.getBytes();
            // inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(inputByteArray);
            // 转换并返回结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            // 字符数组转换成字符串返回
            return bufferToHex(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    /**
     * 计算字符串的MD5值，结果转为16进制
     *@param str 待计算的字符串
     *@return 计算出的MD5值 */

    public static String md5DigestAsHex(String str) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public final static String MD5(String s) {
        s = s.toUpperCase();
       Log.d("IPTV_sc", "s:" + s);
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < md.length; i++) {
                String hx = Integer.toHexString(md[i] & 0xFF);
                if (hx.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hx);
            }
            System.out.println(stringBuilder.toString());

            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

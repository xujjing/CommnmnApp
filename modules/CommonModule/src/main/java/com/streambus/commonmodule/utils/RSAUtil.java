package com.streambus.commonmodule.utils;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/1/27
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class RSAUtil {

    /**
     * RSA算法
     */
    private static final String RSA_ANDROID = "RSA/ECB/PKCS1Padding";


    /**
     * 使用公钥加密
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) throws Exception {
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

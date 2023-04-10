package com.streambus.commonmodule.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/3/2
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class StreamUtils {
    public static final boolean copyStream(InputStream ips, OutputStream ops) {
        try {
            byte[] buff = new byte[4096];
            int len;
            while ((len = ips.read(buff)) != -1) {
                ops.write(buff, 0, len);
            }
            return true;
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                ips.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ops.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

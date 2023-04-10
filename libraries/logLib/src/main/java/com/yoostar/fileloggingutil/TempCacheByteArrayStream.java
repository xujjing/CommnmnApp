package com.yoostar.fileloggingutil;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/9/21
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class TempCacheByteArrayStream {
    private byte mBuffer[];

    private int in = 0;

    private boolean isFull;

    private final int SIZE_N = 2;

    public TempCacheByteArrayStream(int size) {
        mBuffer = new byte[size];
    }

    public void write(String key, byte[] data){
        synchronized (this) {
            try {
                byte[] keyBytes = key.getBytes("UTF-8");
                ByteBuffer buf = ByteBuffer.allocate(keyBytes.length + data.length + SIZE_N);
                buf.put(keyBytes).put(data).put("\n".getBytes("UTF-8"));
                buf.flip();
                while (buf.position() < buf.limit()) {
                    mBuffer[in++] = buf.get();
                    if (in == mBuffer.length) {
                        isFull = true;
                        in = 0;
                    }
                }
            } catch (Exception ignore) {

            }
        }
    }

    public byte[] toByteArray() {
        synchronized (this) {
            if (in == 0 && !isFull) {
                return "".getBytes();
            }
            if (!isFull) {
                return Arrays.copyOfRange(mBuffer, 0, in);
            }else{
                byte[] copy;
                if (in == 0) {
                    copy = Arrays.copyOfRange(mBuffer, 0, mBuffer.length);
                } else {
                    byte[] bytes1 = Arrays.copyOfRange(mBuffer, in, mBuffer.length);
                    byte[] bytes2 = Arrays.copyOfRange(mBuffer, 0, in);
                    copy = new byte[bytes1.length + bytes2.length];
                    System.arraycopy(bytes1, 0, copy, 0, bytes1.length);
                    System.arraycopy(bytes2, 0, copy, bytes1.length, bytes2.length);
                }
                isFull = false;
                in = 0;
                return copy;
            }
        }
    }
}

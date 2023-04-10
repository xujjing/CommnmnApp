package com.streambus.commonmodule.api;

import android.util.Base64;

import com.streambus.basemodule.utils.PreferencesUtils;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.ObjectHelper;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/7/21
 * 描    述:
 * 修订历史：
 * ================================================
 */

public class LruDiskCache implements ICacheApi {
    private static final String TAG = "LruDiskCache";
    private static final String CACHE_ASE_KEY = "CACHE_ASE_KEY";
    private static final String KEY_LRUDISKCACHE_VERSION = "key_lrudiskcache_version";
    final Map<String, Map<String, Entry>> mCacheMap;
    private String mCacheDirPath;
    private long mMaxSize;
    private long mMaxCount;
    private volatile long mCurrentSize;
    private volatile long mCurrentCount;

    private static LruDiskCache INSTANCE;

    public static void setup(String cacheDirPath, long maxSize, int maxCount) {
        INSTANCE = new LruDiskCache(cacheDirPath, maxSize, maxCount);
    }
    public static LruDiskCache getInstance() {
        return INSTANCE;
    }


    public LruDiskCache(String cacheDirPath, long maxSize, int maxCount) {
        mCacheDirPath = cacheDirPath;
        mMaxSize = maxSize;// 100M
        mMaxCount = maxCount;  // 2000 条缓存数据
        mCacheMap = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, Entry>>(0, 0.75f, true));

        if (RequestApi.APP_VERSION_CODE != PreferencesUtils.get(KEY_LRUDISKCACHE_VERSION, 0)) {
            cleanAllCache();
            PreferencesUtils.put(KEY_LRUDISKCACHE_VERSION, RequestApi.APP_VERSION_CODE);
        }else {
            init();
        }
    }


    public void cleanAllCache() {
        mCacheMap.clear();
        try {
            File[] files = new File(mCacheDirPath).listFiles();
            for (File file : files) {
                FileUtils.deleteFiles(file);
            }
        } catch (Exception e) {
            SLog.w(TAG, "cleanAllCache Exception", e);
        }
    }

    private void init() {
        File rootDir = new File(mCacheDirPath);
        File[] list = rootDir.listFiles();
        for (File file : list) {
            String[] split = file.getName().split("_");
            Entry entry = new Entry(split[0], split[1], Integer.parseInt(split[2]));
            Map<String, Entry> map = mCacheMap.get(entry.key);
            if (map == null) {
                map = new HashMap<>();
                mCacheMap.put(entry.key, map);
            }
            mCurrentSize += file.length();
            mCurrentCount++;
            map.put(entry.language, entry);
        }
    }


    Observable<String> loadCache(final String key, final String language, final long time) {
        return loadCache(key, language, time, new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                return s;
            }
        });
    }

    public <T> Observable<T> loadCache(final String key, final String language, final long time, Function<String,T> map) {
        return realLoadCache(key, language, time).map(map)
                .onErrorResumeNext(new Function<Throwable, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull Throwable throwable) throws Exception {
                        SLog.w(TAG, "loadCache onErrorResumeNext", throwable);
                        deleteCache(key, language);
                        return Observable.empty();
                    }
                });
    }

    public Observable<String> realLoadCache(final String key, final String language, final long time) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext(ObjectHelper.requireNonNull(getCacheData(key, language, time), "The getCacheData function returned a null value."));
                emitter.onComplete();
            }
        });
    }

    public int getCacheVersion(String key, String language) {
        Map<String, Entry> map = mCacheMap.get(key);
        if (map != null) {
            Entry entry = map.get(language);
            if (entry != null) {
                return entry.version;
            }
        }
        return 0;
    }

    public String getCacheData(String key, String language, long time) throws IOException {
        Entry entry = null;
        if (time > 0) {
            entry = isExpiry(key, language, time);
        } else {
            Map<String, Entry> map = mCacheMap.get(key);
            if (map != null) {
                entry = map.get(language);
            }
        }
        if (entry != null) {
            byte[] data = readCacheData(entry);
            return decodeData(data);
        }
        return null;
    }

    public Entry isExpiry(String key, String language, long existTime) {
        Map<String, Entry> map = mCacheMap.get(key);
        if (map != null) {
            Entry entry = map.get(language);
            if (entry != null) {
                File file = entry.getFile();
                if (file.exists()) {
                    long time = System.currentTimeMillis() - file.lastModified();
                    if (time < existTime * 1000) {
                        return entry;
                    }
                    deleteCache(entry);
                }
            }
        }
        return null;
    }

    public void saveCacheData(String key, String language, int version, String content) throws IOException {
        synchronized (key) {
            Map<String, Entry> map = mCacheMap.get(key);
            if (map == null) {
                map = new HashMap<>();
                mCacheMap.put(key, map);
            }
            Entry entry = map.remove(language);
            if (entry != null) {
                deleteCache(entry);
            }
            entry = new Entry(key, language, version);
            byte[] bytes = encodeData(content);
            if ((mCurrentCount + 1 > mMaxCount) || (mCurrentSize + bytes.length > mMaxSize)) {
                cleanExpiredCache(key, bytes.length);
            }
            writeCacheData(entry, bytes);
            map.put(language, entry);
        }
    }

    private void cleanExpiredCache(String key, long length) {
        Iterator<Map.Entry<String, Map<String, Entry>>> iterator = mCacheMap.entrySet().iterator();
        do {
            if (!iterator.hasNext()) {
                break;
            }
            Map.Entry<String, Map<String, Entry>> next = iterator.next();
            if (!next.getKey().equals(key)) {
                iterator.remove();
                Map<String, Entry> map = next.getValue();
                for (Map.Entry<String, Entry> e : map.entrySet()) {
                    Entry entry = e.getValue();
                    if (entry != null) {
                        deleteCache(entry);
                    }
                }
                map.clear();
            }
        } while (mCurrentSize + length > mMaxSize);
    }


    public void deleteCache(String key, String language) {
        synchronized (key) {
            Map<String, Entry> map = mCacheMap.get(key);
            if (map != null) {
                Entry entry = map.remove(language);
                if (entry != null) {
                    deleteCache(entry);
                }
            }
        }
    }


    private byte[] readCacheData(Entry entry) throws IOException {
        synchronized (entry) {
            File file = entry.getFile();
            if (file.exists()) {
                FileInputStream ips = new FileInputStream(file);
                ByteArrayOutputStream ops = new ByteArrayOutputStream();
                byte[] buff = new byte[4096]; int len;
                while ((len = ips.read(buff)) != -1) {
                    ops.write(buff, 0, len);
                }
                ips.close();
                return ops.toByteArray();
            }
            return null;
        }
    }

    private void writeCacheData(Entry entry, byte[] data) throws IOException {
        synchronized (entry) {
            File file = entry.getFile();
            FileOutputStream ops = new FileOutputStream(file);
            ops.write(data);
            ops.flush();
            ops.close();
            mCurrentSize += file.length();
            mCurrentCount++;
        }
    }


    private void deleteCache(Entry entry) {
        synchronized (entry) {
            File file = entry.getFile();
            mCurrentSize -= file.length();
            mCurrentCount--;
            file.delete();
        }
    }


    private String decodeData(byte[] data) {
        try {
            byte[] rawKey = MessageDigest.getInstance("MD5").digest(CACHE_ASE_KEY.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] bytes = Base64.decode(data, Base64.NO_WRAP);
            return new String(cipher.doFinal(bytes), "UTF-8");
        } catch (Exception e) {
           SLog.e(TAG, "decodeAES", e);
        }
        return null;
    }

    private byte[] encodeData(String content) {
        try {
            byte[] rawKey = MessageDigest.getInstance("MD5").digest(CACHE_ASE_KEY.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] bytes = cipher.doFinal(content.getBytes("UTF-8"));
            return Base64.encode(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
           SLog.e(TAG, "encodeAES", e);
        }
        return null;
    }


    private final class Entry {
        private final String key;
        private final String language;
        private int version;

        public Entry(String key, String language, int version) {
            this.key = key;
            this.language = language;
            this.version = version;
        }

        public File getFile() {
            return  new File(mCacheDirPath, String.format("%s_%s_%s", key, language, version));
        }
    }



}

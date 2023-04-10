package com.streambus.commonmodule.api;

import android.util.Base64;

import com.google.gson.Gson;
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
public class LocalCache implements ICacheApi {
    private static final String TAG = "LocalCache";
    private static final String CACHE_ASE_KEY = "CACHE_ASE_KEY";
    private static final String KEY_LOCALCACHE_VERSION = "key_localcache_version";
    private Map<String, Map<String, Entry>> mCacheMap;

    private String mCacheDirPath;


    private static LocalCache INSTANCE;

    public static void setup(String cacheDirPath) {
        INSTANCE = new LocalCache(cacheDirPath);
    }

    public static LocalCache getInstance() {
        return INSTANCE;
    }

    public LocalCache(String cacheDirPath) {
        mCacheDirPath = cacheDirPath;
        mCacheMap = Collections.synchronizedMap(new HashMap<String, Map<String, Entry>>());
        if (RequestApi.APP_VERSION_CODE != PreferencesUtils.get(KEY_LOCALCACHE_VERSION, 0)) {
            cleanAllCache();
            PreferencesUtils.put(KEY_LOCALCACHE_VERSION, RequestApi.APP_VERSION_CODE);
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
        SLog.d(TAG,"mCacheDirPath >> "+mCacheDirPath);
        File rootDir = new File(mCacheDirPath);
        String[] list = rootDir.list();
        SLog.d(TAG,"list >> "+new Gson().toJson(list));
        for (String name : list) {
            String[] split = name.split("_");
            SLog.d(TAG,"list >> "+new Gson().toJson(split));
            Entry entry = new Entry(split[0], split[1], Integer.parseInt(split[2]));
            Map<String, Entry> map = mCacheMap.get(entry.key);
            if (map == null) {
                map = new HashMap<>();
                mCacheMap.put(entry.key, map);
            }
            map.put(entry.language, entry);
        }
    }



    public Observable<String> loadCache(final String key, final String language) {
        return loadCache(key, language, new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                return s;
            }
        });
    }

    public <T> Observable<T> loadCache(final String key, final String language, Function<String,T> map) {
        return realLoadCache(key,language).map(map)
                .onErrorResumeNext(new Function<Throwable, ObservableSource<T>>() {
                    @Override
                    public ObservableSource<T> apply(@NonNull Throwable throwable) throws Exception {
                        SLog.w(TAG, "loadCache onErrorResumeNext", throwable);
                        deleteCache(key, language);
                        return Observable.empty();
                    }
                });
    }

    public Observable<String> realLoadCache(final String key, final String language) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext(ObjectHelper.requireNonNull(getCacheData(key, language), "The getCacheData function returned a null value."));
                emitter.onComplete();
            }
        });
    }

    public int getCacheVersion(String key, String language) {
        int version = 0;
        Map<String, Entry> map = mCacheMap.get(key);
        if (map != null) {
            Entry entry = map.get(language);
            if (entry != null) {
                version = entry.version;
            }
        }
        return version;
    }

    public String getCacheData(String key, String language) throws IOException {
        String content = null;
        Map<String, Entry> map = mCacheMap.get(key);
        if (map != null) {
            Entry entry = map.get(language);
            if (entry != null) {
                byte[] data = readCacheData(entry);
                content = decodeData(data);
            }
        }
        return content;
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
            writeCacheData(entry, bytes);
            map.put(language, entry);
        }
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
            File file = new File(mCacheDirPath, String.format("%s_%s_%s", entry.key, entry.language, entry.version));
           SLog.d(TAG, "readCacheData size=" + file.length() + "  path=>" + file.getAbsolutePath());
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
            File file = new File(mCacheDirPath, String.format("%s_%s_%s", entry.key, entry.language, entry.version));
            FileOutputStream ops = new FileOutputStream(file);
            ops.write(data);
            ops.flush();
            ops.close();
           SLog.d(TAG, "writeCacheData size=" + file.length() + "  path=>" + file.getAbsolutePath());
        }
    }


    private void deleteCache(Entry entry) {
        synchronized (entry) {
            File file = new File(mCacheDirPath, String.format("%s_%s_%s", entry.key, entry.language, entry.version));
            file.delete();
        }
    }



    private String decodeData(byte[] data) {
        try {
//           SLog.d(TAG, "decodeData=>" + new String(data, "UTF-8"));
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
            byte[] encode = Base64.encode(bytes, Base64.NO_WRAP);
//           SLog.d(TAG, "encodeData=>" + new String(encode, "UTF-8"));
            return encode;
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
    }

}

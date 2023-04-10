package com.streambus.commonmodule.advert;

import android.app.Application;

import com.google.gson.reflect.TypeToken;
import com.streambus.basemodule.utils.PreferencesUtils;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.AppBuildConfig;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.advert.bean.AdvertBean;
import com.streambus.commonmodule.api.RequestApi;
import com.streambus.commonmodule.bean.RootDataBean;
import com.streambus.commonmodule.utils.AESUtil;
import com.streambus.commonmodule.utils.GsonHelper;
import com.streambus.requestapi.OkHttpHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.ByteString;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/2/2
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class AdvertManager {
    private static final String TAG = "AdvertManager";
    private static final String ULR_ADVERT_PATH = "https://appupd.aegis-corporate.com/app_manager/adprom";
    private static final String KEY_SPLASH_IMAGE = "key_splash_image";
    private static final String PATH_SPLASH_IMAGE = "/Splash/Image/";

    private static AdvertManager INSTANCE;
    public static AdvertManager getInstance() {
        return INSTANCE;
    }

    public static void setup(Application context) {
        INSTANCE = new AdvertManager(context);
    }

    private final Application mContext;
    private final String mAdvertDir;
    private final String mAdvertTem;
    private AdvertManager(Application context) {
        mContext = context;
        File advertDir = new File(context.getExternalCacheDir(), "Advert");
        if (!advertDir.exists()) {
            advertDir.mkdir();
        }
        mAdvertDir = advertDir.getAbsolutePath();
        File advertTem = new File(advertDir, "Tem");
        if (!advertTem.exists()) {
            advertTem.mkdir();
        }
        mAdvertTem = advertTem.getAbsolutePath() + "/";
        initSplashCacheData();
    }

    public MutableLiveData<String> splashAdvertLiveData = new MutableLiveData<>();
    private List<String> mSplashImages = new ArrayList<>();
    public void initSplashCacheData() {
        File splashDir = new File(mAdvertDir + PATH_SPLASH_IMAGE);
        if (!splashDir.exists()) {
            splashDir.mkdirs();
        }
        String[] list = splashDir.list();
        if (list != null && list.length != 0) {
            mSplashImages = new ArrayList<>(Arrays.asList(list));
            int index = mSplashImages.indexOf(PreferencesUtils.get(KEY_SPLASH_IMAGE, ""));
            String nextImg = mSplashImages.get((index + 1) % mSplashImages.size());
            SLog.d(TAG, "initSplashCacheData index=" + index + "  nextImg=" + nextImg + "  mSplashImages=>" + mSplashImages);
            splashAdvertLiveData.setValue(mAdvertDir + PATH_SPLASH_IMAGE + nextImg);
            PreferencesUtils.put(KEY_SPLASH_IMAGE, nextImg);
        }
    }

    public void updateSplashAdvert() {
        Disposable subscribe = requestAdvert(AdvertBean.POSITION_DISPLAY_SPLASH)
                .map(new Function<List<AdvertBean>, Boolean>() {
                    @Override
                    public Boolean apply(List<AdvertBean> list) throws Exception {
                        boolean isOk = true;
                        for (AdvertBean bean  : list) {
                            SLog.d(TAG, "updateSplashAdvert list=>" + list);
                            for (String url  : bean.getImageList()) {
                                String key = ByteString.of((url).getBytes()).md5().hex();
                                if (mSplashImages.remove(key)) {
                                    continue;
                                }
                                File file = new File(mAdvertTem + key);
                                boolean downLoad= downLoadFile(file, url);
                                SLog.i(TAG, "downLoadFile downLoad=>" + downLoad);
                                if (downLoad) {
                                    boolean rename = file.renameTo(new File(mAdvertDir + PATH_SPLASH_IMAGE + key));
                                    SLog.i(TAG, "renameTo rename=>" + rename);
                                    if (rename){
                                        continue;
                                    }
                                }
                                isOk = false;
                            }
                        }
                        if (isOk) {
                            for (String name : mSplashImages) {
                                File file = new File(mAdvertDir + PATH_SPLASH_IMAGE + name);
                                if (!file.getAbsolutePath().equals(splashAdvertLiveData.getValue())) {
                                    file.delete();
                                }
                            }
                        }
                        return isOk;
                    }
                }).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean b) throws Exception {
                        SLog.i(TAG, "updateSplashAdvert result=>" + b);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        SLog.e(TAG, "updateSplashAdvert", throwable);
                    }
                });
    }


    public static Observable<List<AdvertBean>> requestAdvert(String display){
        return Observable.just(display)
                .map(new Function<String, List<AdvertBean>>() {
                    @Override
                    public List<AdvertBean> apply(String display) throws Exception {
                        String json = String.format("{\"accountId\": \"%s\", \"pkg\": \"%s\", \"appVersion\": \"%s\", \"channelCode\": \"%s\", \"displayPosition\": \"%s\"}",
                                Constants.VALUE_LOGIN_ACCOUNT_ID, RequestApi.APP_PACKAGE_NAME, RequestApi.APP_VERSION_NAME, AppBuildConfig.FLAVOR, display);
                        String encodeData = AESUtil.encryptBase64(json, Constants.PPMGR_PERFCOS_AES_KEY);
                        SLog.d(TAG, "requestAdvert request json=>" + json + "   encodeData=>" + encodeData);
                        Request request = new Request.Builder().addHeader(OkHttpHelper.HEADER_NO_ENCRYPTION, OkHttpHelper.HEADER_NO_ENCRYPTION)
                                .url(ULR_ADVERT_PATH).post(RequestBody.create(MediaType.parse("text/plain"), encodeData)).build();
                        Call call = OkHttpHelper.getCmsClient().newCall(request);
                        Response response = call.execute();
                        if (response.isSuccessful()) {
                            String data = response.body().string();
                            String decodeData = AESUtil.decryptBase64(data, Constants.PPMGR_PERFCOS_AES_KEY);
                            SLog.d(TAG, "response data=>" + data + "   decodeData=>" + decodeData);
                            RootDataBean<List<AdvertBean>> bean = GsonHelper.toType(decodeData, new TypeToken<RootDataBean<List<AdvertBean>>>() {
                            }.getType());
                            return bean.getData();
                        }
                        throw new IllegalStateException("http response code=" + response.code());
                    }
                });
    }

    private static boolean downLoadFile(File file, String url) {
        Response response = null;
        try {
            Request request = new Request.Builder().addHeader("FILE", "FILE").url(url).build();
            response = OkHttpHelper.getOkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                InputStream ips = response.body().byteStream();
                FileOutputStream ops = new FileOutputStream(file);
                int len;
                byte[] buff = new byte[1024];
                while ((len = ips.read(buff)) != -1) {
                    ops.write(buff, 0, len);
                }
                ops.flush();
                ops.close();
                ips.close();
                return true;
            }
        } catch (Exception e) {
            SLog.e(TAG, "downLoadFile Exception", e);
        }finally {
            if (response != null) {
                response.close();
            }
        }
        return false;
    }
}

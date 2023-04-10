package com.streambus.commonmodule.api;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.Base64;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.streambus.basemodule.utils.PreferencesUtils;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.bean.ApkListBean;
import com.streambus.commonmodule.bean.ApkUpgradeInfo;
import com.streambus.commonmodule.bean.BuyServiceBean;
import com.streambus.commonmodule.bean.CategoryBean;
import com.streambus.commonmodule.bean.CategoryListBean;
import com.streambus.commonmodule.bean.ChannelVodBean;
import com.streambus.commonmodule.bean.ColumnBean;
import com.streambus.commonmodule.bean.EmailCodeBean;
import com.streambus.commonmodule.bean.HomeItemBean;
import com.streambus.commonmodule.bean.HotWordBean;
import com.streambus.commonmodule.bean.MemberBean;
import com.streambus.commonmodule.bean.OrderBean;
import com.streambus.commonmodule.bean.ResultBean;
import com.streambus.commonmodule.bean.RoleListBean;
import com.streambus.commonmodule.bean.RootDataBean;
import com.streambus.commonmodule.bean.TagTypeBean;
import com.streambus.commonmodule.login.MyAppLogin;
import com.streambus.commonmodule.utils.AESUtil;
import com.streambus.commonmodule.utils.GsonHelper;
import com.streambus.commonmodule.utils.RSAUtil;
import com.streambus.commonmodule.utils.ZxingUtil;
import com.streambus.requestapi.LoginModule;
import com.streambus.requestapi.OkHttpHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import androidx.annotation.IntDef;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.functions.Predicate;
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
 * 创建日期：2020/7/21
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class RequestApi {

    private static final String TAG = "RequestApi";


    public static final String CONTENT_VERSION = "Content-Version";
    public static final AbstractMap.SimpleEntry<String, String> API_VERSION_3 = new AbstractMap.SimpleEntry<>("Api-Version", "3.0");
    private static final int CACHE_TYPE_NONE = 0;
    private static final int CACHE_TYPE_LOCAL = 1;
    private static final int CACHE_TYPE_LRUDISK = 2;

    public static final int TAG_TYPE_LIVE = 1;
    public static final int TAG_TYPE_VOD = 2;
    public static final int TAG_TYPE_SEXY = 3;
    public static Context sContext;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TAG_TYPE_LIVE, TAG_TYPE_VOD})
    private @interface TagType { }

    public static String LANGUAGE = Locale.getDefault().getLanguage();
    public static String CMS_MAVIS_URL = "https://127.0.0.1/testcms";
    public static String EPG_MAVIS_URL = "https://127.0.0.1/testepg";
    public static String FILE_MAVIS_URL = "https://127.0.0.1/testfile";
    public static String ACCOUNT_ID = "NULL";
    public static String ACCOUNT_NAME = "NULL";
    public static String TOKEN = "NULL";
    public static String ACCOUNT_JSON = "NULL";

    public static String APP_PACKAGE_NAME;
    public static String APP_VERSION_NAME;
    public static int APP_VERSION_CODE;


    public static final String fileUrl(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        } else {
            return path.startsWith("/") ? FILE_MAVIS_URL + path : FILE_MAVIS_URL + "/" + path;
        }
    }

    public static class ResultException extends Exception{
        private int result;
        public ResultException(int result) {
            super("result=" + result);
            this.result = result;
        }

        public int getResult() {
            return result;
        }
    }

    public static class ResultCode {
        public static final int CODE_0 = 0;///数据正常返回
        public static final int CODE_2 = 2; ///数据未更新
        public static final int CODE_3 = 3; ///TOKEN 过期
        public static final int CODE_4 = 4;///數據被清空
        public static final int CODE_5 = 5;///帐号没权限
        public static final int CODE_8 = 8; ///强制退出apk
        public static final int CODE_9 = 9; ///自定义，数据未更新
    }


    public static void setup(Context context, boolean isTestService) {
        sContext = context;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            APP_PACKAGE_NAME = packageInfo.packageName;
            APP_VERSION_NAME = packageInfo.versionName;
            APP_VERSION_CODE = packageInfo.versionCode;
        } catch (Exception ignore) {ignore.printStackTrace(); }

        setMemberServiceTest(isTestService);
        initCache(context);
        updateLogin();
        if (TextUtils.isEmpty(FILE_MAVIS_URL)) {
            FILE_MAVIS_URL = "https://127.0.0.1/testfile";
        }
        MyAppLogin.getInstance().updateLoginAudioState.observeForever(new androidx.lifecycle.Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == LoginModule.EVENT_TYPE_SUCCESS) {
                    updateLogin();
                }
            }
        });
    }

    private static void updateLogin() {
        ACCOUNT_ID = Constants.VALUE_LOGIN_ACCOUNT_ID;
        ACCOUNT_NAME = Constants.VALUE_LOGIN_ACCOUNT_NAME;
        TOKEN = Constants.VALUE_LOGIN_TOKEN;

        CMS_MAVIS_URL = MyAppLogin.getInstance().getCmsUrl(false);
        FILE_MAVIS_URL = MyAppLogin.getInstance().getFileUrl(false);
        EPG_MAVIS_URL = MyAppLogin.getInstance().getEpgUrl(false);

        ACCOUNT_JSON = String.format("{\"accountId\": \"%s\", \"token\": \"%s\", \"app_package\": \"%s\", \"language\": \"%s\"}",
                ACCOUNT_ID, TOKEN, APP_PACKAGE_NAME, LANGUAGE);
    }


    public static void updateMavisUrl() {
        boolean b = CMS_MAVIS_URL.equals(FILE_MAVIS_URL);
        CMS_MAVIS_URL = MyAppLogin.getInstance().getCmsUrl(true);
        if (b) {
            FILE_MAVIS_URL = CMS_MAVIS_URL;
        }
    }

    public static void initCache(Context context) {
        File localDir = new File(context.getCacheDir(), "LocalCache");
        if (!localDir.exists()) {
            localDir.mkdirs();
        }
        LocalCache.setup(localDir.getAbsolutePath());

        File lruDiskDir = new File(context.getCacheDir(), "LruDiskDir");
        if (!lruDiskDir.exists()) {
            lruDiskDir.mkdirs();
        }
        LruDiskCache.setup(lruDiskDir.getAbsolutePath(), 100 * 1025 * 1024, 2000);
    }

    private static Observable<String> loadRemote(String url, String content, final String key, final ICacheApi cacheApi, Map.Entry<String,String>... headers) {
        return loadRemote2(url, content, key, cacheApi, true, headers);
    }
    private static Observable<String> loadRemote2(String url, String content, final String key, final ICacheApi cacheApi, boolean defHandData, Map.Entry<String,String>... headers) {
        return Observable.defer(new Callable<ObservableSource<String>>() {
            @Override
            public ObservableSource<String> call() throws Exception {
                Request.Builder builder = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse("text/plain"), content));
                if (!TextUtils.isEmpty(key) && cacheApi != null) {
                    builder.addHeader(CONTENT_VERSION, cacheApi.getCacheVersion(key, LANGUAGE) + "");
                }
                if (headers != null) {
                    for (Map.Entry<String, String> header : headers) {
                        builder.addHeader(header.getKey(), header.getValue());
                    }
                }
                SLog.d(TAG, "content data=>" + content + " ,"+ url);
                return new CallExecuteObservable(defHandData, OkHttpHelper.getCmsClient().newCall(builder.build())).filter(new Predicate<String>() {
                    @Override
                    public boolean test(String data) throws Exception {
                        SLog.d(TAG, "CallExecuteObservable data=>" + data);
                        ResultBean bean = GsonHelper.toType(data, ResultBean.class);
                        if (bean.getResult() == ResultCode.CODE_2) {
                            SLog.d(TAG, "CallExecuteObservable data is newest");
                            return false;
                        }
                        if (bean.getResult() == ResultCode.CODE_0) {
                            SLog.d(TAG, "CallExecuteObservable Result=>" + true);
                            if (!TextUtils.isEmpty(key) && cacheApi != null) {
                                cacheApi.saveCacheData(key, LANGUAGE, bean.getContentVersion(), data);
                            }
                            return true;
                        }
                        if (bean.getResult() == ResultCode.CODE_3) {
                            SLog.d(TAG, "CallExecuteObservable token exept");
                            MyAppLogin.getInstance().reTryLogin();
                        }
                        if (bean.getResult() == ResultCode.CODE_4) {
                            SLog.d(TAG, "CallExecuteObservable no data");
                            if (!TextUtils.isEmpty(key) && cacheApi != null) {
                                cacheApi.deleteCache(key, LANGUAGE);
                            }
                        }
                        throw new ResultException(bean.getResult());
                    }
                });
            }
        });
    }


    private static  <T> ObservableTransformer<Map.Entry<String,T>,T> getDataTransformer() {
        return new ObservableTransformer<Map.Entry<String, T>, T>() {
            @Override
            public ObservableSource<T> apply(Observable<Map.Entry<String, T>> upstream) {
                return upstream.distinctUntilChanged(new Function<Map.Entry<String, T>, String>() {
                    @Override
                    public String apply(Map.Entry<String, T> entry) throws Exception {
                        return entry.getKey();
                    }
                }).map(new Function<Map.Entry<String, T>, T>() {
                    @Override
                    public T apply(Map.Entry<String, T> entry) throws Exception {
                        return entry.getValue();
                    }
                });
            }
        };
    }

    /************************************* - VOD - ****************************************/

    /**
     * 获取栏目数据
     * "{\"account\": \"%s\",\"token\": \"%s\",\"language\": \"%s\"}"
     */
    public static final String VOD_HOME_TYPE = "/client/column_v3/home";
    public static final Map.Entry<String, Observable<List<ColumnBean>>> remoterHomeType(ICacheApi cacheApi) {
        String key = ByteString.of(VOD_HOME_TYPE.getBytes()).md5().hex();
        Observable<List<ColumnBean>> remoter = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                String url = CMS_MAVIS_URL + VOD_HOME_TYPE;
                String content = String.format("{\"account\": \"%s\",\"token\": \"%s\",\"language\": \"%s\",\"pkg\": \"%s\",\"version\": \"%s\",\"versionCode\": \"%s\"}",
                        ACCOUNT_ID, TOKEN, LANGUAGE, APP_PACKAGE_NAME, APP_VERSION_NAME, APP_VERSION_CODE);
                Disposable disposable = loadRemote(url, content, key, cacheApi, API_VERSION_3)
                        .subscribeOn(Schedulers.io())
                        .subscribe(emitter::onNext, emitter::onError, emitter::onComplete);
                // 不取消，让当前网络请求执行完成
            }
        }).map(new Function<String, List<ColumnBean>>() {
            @Override
            public List<ColumnBean> apply(String data) throws Exception {
                RootDataBean<List<ColumnBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<ColumnBean>>>() {
                }.getType());
                return bean.getData();
            }
        });
        return new AbstractMap.SimpleImmutableEntry<>(key, remoter);
    }

    public static Maybe<StateListDrawable> requestVodHomeIcon(List<String> iconList, Context context) {
        Maybe<Drawable> focusedDrawMaybe = Maybe.fromFuture(Glide.with(context).load(RequestApi.fileUrl(iconList.get(0))).submit());
        Maybe<Drawable> selectedDrawMaybe = Maybe.fromFuture(Glide.with(context).load(RequestApi.fileUrl(iconList.get(1))).submit());
        Maybe<Drawable> normalDrawMaybe = Maybe.fromFuture(Glide.with(context).load(RequestApi.fileUrl(iconList.get(2))).submit());
        return Maybe.zip(focusedDrawMaybe, selectedDrawMaybe, normalDrawMaybe, new Function3<Drawable, Drawable, Drawable, StateListDrawable>() {
            @Override
            public StateListDrawable apply(Drawable focusedDraw, Drawable selectedDraw, Drawable normalDraw) throws Exception {
                StateListDrawable stateListDrawable = new StateListDrawable();
                stateListDrawable.addState(new int[]{android.R.attr.state_focused}, focusedDraw);
                stateListDrawable.addState(new int[]{android.R.attr.state_selected}, selectedDraw);
                stateListDrawable.addState(new int[]{}, normalDraw);
                return stateListDrawable;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }



    /**
     * 获取首页数据
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\"}"
     */
    public static final String VOD_HOME_ITEM = "/client/column_v3/home/";//"/client/vod/home/";
    public static final Observable<HomeItemBean> requestVodHomeChannel(long columnUnid, boolean isRefresh) {
        return Observable.just(columnUnid).flatMap(new Function<Long, ObservableSource<HomeItemBean>>() {
            @Override
            public ObservableSource<HomeItemBean> apply(Long columnUnid) throws Exception {
                LocalCache cacheApi = LocalCache.getInstance();
                String key = ByteString.of((VOD_HOME_ITEM + columnUnid).getBytes()).md5().hex();
                String url = CMS_MAVIS_URL + VOD_HOME_ITEM + columnUnid;
                SLog.i(TAG, "apply: requestHomeChannel key " + key + "  url=" + url);

                String content = String.format("{\"account\": \"%s\",\"token\": \"%s\", \"language\": \"%s\",\"pkg\": \"%s\",\"version\": \"%s\",\"versionCode\": \"%s\"}"
                        , ACCOUNT_ID, TOKEN, LANGUAGE, APP_PACKAGE_NAME, APP_VERSION_NAME, APP_VERSION_CODE);
                Observable<HomeItemBean> remote = loadRemote2(url, content, key, cacheApi, false)
                        .map(new Function<String, HomeItemBean>() {
                            @Override
                            public HomeItemBean apply(String data) throws Exception {
                                //                                FileUtils.saveFile(new File(Environment.getExternalStorageDirectory(), "home_" + columnUnid), data);
                                return GsonHelper.toType(data, HomeItemBean.class);
                            }
                        });
                // 已经有数据了，尝试获取最新数据
                if (isRefresh) {
                    return remote;
                }
                Observable<HomeItemBean> cache = cacheApi.loadCache(key, LANGUAGE, new Function<String, HomeItemBean>() {
                    @Override
                    public HomeItemBean apply(String data) throws Exception {
                        HomeItemBean itemBean = GsonHelper.toType(data, HomeItemBean.class);
                        if (itemBean.getColumnList() == null) {
                            throw new IllegalStateException("itemBean.getColumnList() == null");
                        }
                        return itemBean;
                    }
                });
                return Observable.concat(cache, remote);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static final Map.Entry<String, Observable<ColumnBean>> remoterHomeColumn(long columnUnid, ICacheApi cacheApi) {
        String key = ByteString.of((VOD_HOME_ITEM + columnUnid).getBytes()).md5().hex();
        return new AbstractMap.SimpleEntry<>(key, Observable.defer(new Callable<ObservableSource<ColumnBean>>() {
            @Override
            public ObservableSource<ColumnBean> call() throws Exception {
                return Observable.just(0).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, ObservableSource<ColumnBean>>() {
                    @Override
                    public ObservableSource<ColumnBean> apply(Integer integer) throws Exception {
                        String url = CMS_MAVIS_URL + VOD_HOME_ITEM + columnUnid;
                        String content = String.format("{\"account\": \"%s\",\"token\": \"%s\", \"language\": \"%s\",\"pkg\": \"%s\",\"version\": \"%s\",\"versionCode\": \"%s\"}"
                                , ACCOUNT_ID, TOKEN, LANGUAGE, APP_PACKAGE_NAME, APP_VERSION_NAME, APP_VERSION_CODE);
                        return loadRemote2(url, content, key, cacheApi, false)
                                .map(new Function<String, ColumnBean>() {
                                    @Override
                                    public ColumnBean apply(String data) throws Exception {
                                        return GsonHelper.toType(data, ColumnBean.class);
                                    }
                                });
                    }
                });
            }
        }));
    }

    /**
     * 获取栏目数据
     * vod 所有点播节目
     * movies_all 电影
     * serials_all 电视剧
     * anime_all 动漫
     * sport_all 体育
     * show_all 音乐
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\"}"
     */
    public static final String VOD_COLUMN_CATEGORY = "/client/column/";

    public static final Observable<List<CategoryBean>> requestVodCategory(String columnUnid) {
        return Observable.just(columnUnid).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String columnUnid) throws Exception {
                String key = ByteString.of((VOD_COLUMN_CATEGORY + columnUnid).getBytes()).md5().hex();
                LocalCache cacheApi = LocalCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                String url = CMS_MAVIS_URL + VOD_COLUMN_CATEGORY + columnUnid;
                String content = String.format("{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\"}", ACCOUNT_ID, TOKEN, LANGUAGE);
                Observable<String> remote = loadRemote2(url, content, key, cacheApi, true, API_VERSION_3);
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, List<CategoryBean>>() {
            @Override
            public List<CategoryBean> apply(String data) throws Exception {
                RootDataBean<CategoryListBean> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<CategoryListBean>>() {
                }.getType());
                return bean.getData().getCategoryList();
            }
        });
    }

    /**
     * 获取点播栏目列表对应的节目数据
     * 排序方式，默认 0
     * 0: 默认排序；(按照上映时间、发布时间)
     * 1: 上映时间排序；
     * 2: 发布时间
     * 3: 节目名称排序
     * 4: 指定排序
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"pageSize\": %s,\"orderType\": %s,\"pageNum\": %s}"
     */
    public static final String VOD_CATEGORY_CHANNEL = "/client/search/";//"/client/channel/"//此接口把 分组的数据全比请求回来// ;//"/client/column/";
    public static final int PAGE_SIZE = 30;

    public static final Map.Entry<String, Observable<RootDataBean<List<ChannelVodBean>>>> remoterVodChannelByCategory(final String categoryId, final String videoType,final int pageNum, ICacheApi cacheApi) {
        final int orderType = PreferencesUtils.get(Constants.KEY_RESOURCE_SORT, 0);
        String key = ByteString.of((VOD_CATEGORY_CHANNEL + "+" + videoType + "+" + categoryId + "+" + orderType + "+" + pageNum).getBytes()).md5().hex();
        return new AbstractMap.SimpleEntry<>(key, Observable.just(CMS_MAVIS_URL).subscribeOn(Schedulers.io()).flatMap(new Function<String, ObservableSource<RootDataBean<List<ChannelVodBean>>>>() {
            @Override
            public ObservableSource<RootDataBean<List<ChannelVodBean>>> apply(String host) throws Exception {
                String url = host + VOD_CATEGORY_CHANNEL + videoType;
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("language", LANGUAGE);
                map.put("isUnlock", !Constants.SUBJECT_LOCK.getValue());
                map.put("columnids", new String[]{categoryId});
                map.put("orderType", orderType);
                map.put("pageSize", PAGE_SIZE);
                map.put("pageNum", pageNum);
                String content = GsonHelper.toJson(map);
                SLog.d(TAG, "requestVodChannel params content=>" + content);
                return loadRemote(url, content, key, cacheApi, API_VERSION_3)
                        .map(new Function<String, RootDataBean<List<ChannelVodBean>>>() {
                            @Override
                            public RootDataBean<List<ChannelVodBean>> apply(String data) throws Exception {
                                return GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {}.getType());
                            }
                        });
            }
        }));
    }

    public static final Map.Entry<String, Observable<RootDataBean<List<ChannelVodBean>>>> remoterVodChannelBySearch(final String searchKey, final String[] categoryIds, final String videoType,final int pageNum, ICacheApi cacheApi) {
        final int orderType = PreferencesUtils.get(Constants.KEY_RESOURCE_SORT, 0);
        String key = ByteString.of((VOD_CATEGORY_CHANNEL + "+" + videoType + "+" + searchKey + "+" + searchKey + "+" + Arrays.toString(categoryIds) + "+" + pageNum).getBytes()).md5().hex();
        return new AbstractMap.SimpleEntry<>(key, Observable.just(CMS_MAVIS_URL).subscribeOn(Schedulers.io()).flatMap(new Function<String, ObservableSource<RootDataBean<List<ChannelVodBean>>>>() {
            @Override
            public ObservableSource<RootDataBean<List<ChannelVodBean>>> apply(String host) throws Exception {
                String url = host + VOD_CATEGORY_CHANNEL + videoType;
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("language", LANGUAGE);
                map.put("isUnlock", !Constants.SUBJECT_LOCK.getValue());
                map.put("orderType", orderType);
                map.put("pageSize", PAGE_SIZE);
                map.put("pageNum", pageNum);
                map.put("dtype", "1000");
                map.put("keyword", searchKey);
                if (categoryIds != null) {
                    map.put("columnids", categoryIds);
                }
                String content = GsonHelper.toJson(map);
                SLog.d(TAG, "requestVodChannel params content=>" + content);
                return loadRemote(url, content, key, cacheApi, API_VERSION_3)
                        .map(new Function<String, RootDataBean<List<ChannelVodBean>>>() {
                            @Override
                            public RootDataBean<List<ChannelVodBean>> apply(String data) throws Exception {
                                return GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {}.getType());
                            }
                        });
            }
        }));
    }

    public static final Observable<RootDataBean<List<ChannelVodBean>>> requestVodChannel(String videoType, final String categoryId, final int orderType, final int pageNum) {
        return Observable.just(videoType).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String videoType) throws Exception {
                int orderType= PreferencesUtils.get(Constants.KEY_RESOURCE_SORT, 0);
                String key = ByteString.of((VOD_CATEGORY_CHANNEL + "+" + videoType + "+" + categoryId + "+" + orderType + "+" + pageNum)
                        .getBytes()).md5().hex();
                Observable<String> cache;
                ICacheApi cacheApi;
                if (pageNum == 1) {
                    cacheApi = LocalCache.getInstance();
                    cache = LocalCache.getInstance().loadCache(key, LANGUAGE);
                } else {
                    cacheApi = LruDiskCache.getInstance();
                    cache = LruDiskCache.getInstance().loadCache(key, LANGUAGE, 0);
                }
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                String url = CMS_MAVIS_URL + VOD_CATEGORY_CHANNEL + videoType;
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("language", LANGUAGE);
                map.put("isUnlock", !Constants.SUBJECT_LOCK.getValue());
                map.put("orderType", orderType);
                map.put("pageSize", PAGE_SIZE);
                map.put("pageNum", pageNum);
                map.put("dtype", "1000");
                if (!TextUtils.isEmpty(categoryId)) {
                    map.put("columnids", new String[]{categoryId});
                }
                String content = GsonHelper.toJson(map);
                SLog.d(TAG, "requestVodChannel params content=>" + content);
                Observable<String> remote = loadRemote2(url, content, key, cacheApi, true, API_VERSION_3);
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                SLog.d(TAG, "requestVodChannel distinct data=>" + data);
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, RootDataBean<List<ChannelVodBean>>>() {
            @Override
            public RootDataBean<List<ChannelVodBean>> apply(String data) throws Exception {
                SLog.d(TAG, "requestVodChannel map data=>" + data);
                return GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {}.getType());
            }
        });
    }


    /**
     * 热度搜索
     * 根据 keyword 或 tagId 搜索 tag包括 年份、类型等信息
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"pageSize\": %s,\"orderType\": %s,\"pageNum\": %s}"
     * vod 所有点播节目
     * movies_all 电影
     * serials_all 电视剧
     * anime_all 动漫
     */
    public static final String VOD_SEARCH_SORT_CHANNEL = "/client/search_sort/";

    public static final Observable<RootDataBean<List<ChannelVodBean>>> requestVodSearchSortChannel(String columnUnid, final boolean isUnlock, final String orderby, final int pageNum, final String keyword, final int... tagids) {
        return Observable.just(columnUnid).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String columnUnid) throws Exception {
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("language", LANGUAGE);
                map.put("isUnlock", isUnlock);
                map.put("orderby", orderby);
                map.put("pageSize", PAGE_SIZE);
                map.put("pageNum", pageNum);
                //map.put("dtype", "1000");
                if (tagids != null && tagids.length != 0) {
                    map.put("tagids", Arrays.asList(tagids));
                } else {
                    map.put("keyword", keyword);
                }
                String content = GsonHelper.toJson(map);
                SLog.i(TAG, "requestVodSearchChannel remote content=>" + content + " ,columnUnid=" + columnUnid);
                String key = ByteString.of((VOD_SEARCH_SORT_CHANNEL + columnUnid + content).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                String url = CMS_MAVIS_URL + VOD_SEARCH_SORT_CHANNEL + columnUnid;
                Observable<String> remote = loadRemote(url, content, key, cacheApi, API_VERSION_3)
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(String data) throws Exception {
                                //RootVodChannelBean rootBean = GSON.fromJson(data, RootVodChannelBean.class);
                                //SLog.i(TAG, "requestVodChannel remote data.size=>" + rootBean.getChannelList().size());
                            }
                        });
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, RootDataBean<List<ChannelVodBean>>>() {

            @Override
            public RootDataBean<List<ChannelVodBean>> apply(String data) throws Exception {
                SLog.d(TAG, "requestVodChannel map data=>" + data);
                RootDataBean<List<ChannelVodBean>> rootBean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {
                }.getType());
                return rootBean;
            }
        });
    }


    public static final String VOD_HOT_WORD = "/client/hotword";
    public static final Map.Entry<String, Observable<List<HotWordBean>>> remoterHotword(ICacheApi cacheApi) {
        String key = ByteString.of((VOD_CATEGORY_CHANNEL).getBytes()).md5().hex();
        return new AbstractMap.SimpleEntry<>(key, Observable.just(CMS_MAVIS_URL).flatMap(new Function<String, ObservableSource<List<HotWordBean>>>() {
            @Override
            public ObservableSource<List<HotWordBean>> apply(String host) throws Exception {
                String content = String.format("{\"account\": \"%s\", \"token\": \"%s\", \"pkg\": \"%s\", \"language\": \"%s\"}",
                        ACCOUNT_ID, TOKEN, APP_PACKAGE_NAME, LANGUAGE);
                String url = host + VOD_HOT_WORD;
                return loadRemote2(url, content, key, cacheApi, true, API_VERSION_3)
                        .map(new Function<String, List<HotWordBean>>() {
                            @Override
                            public List<HotWordBean> apply(String data) throws Exception {
                                return ((RootDataBean<List<HotWordBean>>)GsonHelper.toType(data, new TypeToken<RootDataBean<List<HotWordBean>>>() {}.getType())).getData();
                            }
                        });
            }
        }));
    }


    public static final Observable<RootDataBean<List<HotWordBean>>> requestHotword() {
        return Observable.just("").flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("language", LANGUAGE);
                map.put("isUnlock", !Constants.SUBJECT_LOCK.getValue());
                map.put("orderType", PreferencesUtils.get(Constants.KEY_RESOURCE_SORT, 0));
                map.put("pageSize", PAGE_SIZE);
                map.put("pageNum", 0);
                String content = GsonHelper.toJson(map);
                SLog.i(TAG, "requestVodSearchChannel remote content=>" + content);
                String key = ByteString.of((VOD_HOT_WORD + content).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                String url = CMS_MAVIS_URL + VOD_HOT_WORD;
                Observable<String> remote = loadRemote2(url, content, key, cacheApi, true, API_VERSION_3)
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(String data) throws Exception {
                                //RootVodChannelBean rootBean = GsonHelper.toType(data, RootVodChannelBean.class);
                                //SLog.i(TAG, "requestVodChannel remote data.size=>" + rootBean.getChannelList().size());
                            }
                        });
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, RootDataBean<List<HotWordBean>> >() {
            @Override
            public RootDataBean<List<HotWordBean>> apply(String data) throws Exception {
                RootDataBean<List<HotWordBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<HotWordBean>>>() {}.getType());
                return bean;
            }
        });
    }

    public static final String VOD_SEARCH_V2 = "/client/search_v2/";//"/client/channel/"//此接口把 分组的数据全比请求回来// ;//"/client/column/";

    public static final Observable<RootDataBean<List<ChannelVodBean>>> requestVodSearchV2(String columnUnid, final boolean isUnlock, final int orderType, final int pageNum, final String keyword, final int[] types, int[] columns, final boolean onlyAdult, final long... tagids) {

        return Observable.just(columnUnid).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String columnUnid) throws Exception {
                int orderType= PreferencesUtils.get(Constants.KEY_RESOURCE_SORT, 0);
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("language", LANGUAGE);
                map.put("isUnlock", isUnlock);
                map.put("orderType", orderType);
                map.put("pageSize", PAGE_SIZE);
                map.put("pageNum", pageNum);
                map.put("adult", onlyAdult);
                //map.put("dtype", "1000");
                if (tagids != null && tagids.length != 0) {
                    map.put("tagids", tagids);
                }
                if (!TextUtils.isEmpty(keyword)) {
                    map.put("keyword", keyword);
                }
                if (null != types && types.length > 0) {
                    map.put("types", types);
                }
                if (null != columns) {
                    map.put("columnids", columns);
                }
                String content = GsonHelper.toJson(map);
                SLog.i(TAG, "requestVodSearchV2 remote content=>" + content + " ,columnUnid=" + columnUnid);
                String key = ByteString.of((VOD_SEARCH_V2 + columnUnid + content).getBytes()).md5().hex();
                LocalCache cacheApi = LocalCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                String url = CMS_MAVIS_URL + VOD_SEARCH_V2 + columnUnid;
                Observable<String> remote = loadRemote2(url, content, key, null, true, API_VERSION_3)
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(String data) throws Exception {
                                //RootVodChannelBean rootBean = GsonHelper.toType(data, RootVodChannelBean.class);
                                //SLog.i(TAG, "requestVodChannel remote data.size=>" + rootBean.getChannelList().size());
                            }
                        });
//                return Observable.concat(cache, remote);
                return remote;
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, RootDataBean<List<ChannelVodBean>>>() {
            @Override
            public RootDataBean<List<ChannelVodBean>> apply(String data) throws Exception {
                SLog.d(TAG, "requestVodChannel map data=>" + data);
                return GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {}.getType());
            }
        });
    }

    /**
     * 根据 keyword 或 tagId 搜索 tag包括 年份、类型等信息
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"pageSize\": %s,\"orderType\": %s,\"pageNum\": %s}"
     * vod 所有点播节目
     * movies_all 电影
     * serials_all 电视剧
     * anime_all 动漫
     */
    public static final String VOD_SEARCH_CHANNEL = "/client/search/";

    public static final Observable<RootDataBean<List<ChannelVodBean>>> requestVodSearchChannel(String columnUnid, final boolean isUnlock, final int orderType, final int pageNum, final String keyword, final long... tagids) {
        return requestVodSearchChannel(columnUnid, isUnlock, orderType, pageNum, keyword, null, tagids);
    }

    /**
     * @param orderType 1,上映时间排序
     * @param types [3, 4] //节目类型，电影和电视剧
     * @return
     */
    public static final Observable<RootDataBean<List<ChannelVodBean>>> requestVodSearchChannel(String columnUnid, final boolean isUnlock, final int orderType, final int pageNum, final String keyword, int[] types, final long... tagids) {

        return Observable.just(columnUnid).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String columnUnid) throws Exception {
                int orderType= PreferencesUtils.get(Constants.KEY_RESOURCE_SORT, 0);
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("language", LANGUAGE);
                map.put("isUnlock", isUnlock);
                map.put("orderType", orderType);
                map.put("pageSize", PAGE_SIZE);
                map.put("pageNum", pageNum);
                //map.put("dtype", "1000");
                if (tagids != null && tagids.length != 0) {
                    map.put("tagids", tagids);
                }
                if (!TextUtils.isEmpty(keyword)) {
                    map.put("keyword", keyword);
                }
                if (null != types && types.length > 0) {
                    map.put("types", types);
                }
                String content = GsonHelper.toJson(map);
                SLog.i(TAG, "requestVodSearchChannel remote content=>" + content + " ,columnUnid=" + columnUnid);
                String key = ByteString.of((VOD_SEARCH_CHANNEL + columnUnid + content).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                String url = CMS_MAVIS_URL + VOD_SEARCH_CHANNEL + columnUnid;
                Observable<String> remote = loadRemote(url, content, key, cacheApi, API_VERSION_3)
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(String data) throws Exception {
                                //RootVodChannelBean rootBean = GsonHelper.toType(data, RootVodChannelBean.class);
                                //SLog.i(TAG, "requestVodChannel remote data.size=>" + rootBean.getChannelList().size());
                            }
                        });
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, RootDataBean<List<ChannelVodBean>>>() {
            @Override
            public RootDataBean<List<ChannelVodBean>> apply(String data) throws Exception {
                SLog.d(TAG, "requestVodChannel map data=>" + data);
                return GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {}.getType());
            }
        });
    }

    //根据节目Id搜索
    public static final Observable<List<ChannelVodBean>> requestVodSearchChannel(String columnUnid, final long... channelIds) {
        return Observable.just(columnUnid).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String columnUnid) throws Exception {
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("language", LANGUAGE);
                //map.put("progids",channelIds);
                map.put("channelids",channelIds);
                String content = GsonHelper.toJson(map);
                String key = ByteString.of((VOD_SEARCH_CHANNEL + columnUnid + content).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                String url = CMS_MAVIS_URL + VOD_SEARCH_CHANNEL + columnUnid;
                Observable<String> remote = loadRemote2(url, content, key, cacheApi, true, API_VERSION_3);
                return remote;//Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, List<ChannelVodBean>>() {
            @Override
            public List<ChannelVodBean> apply(String data) throws Exception {
                SLog.d(TAG, "requestVodChannel map data=>" + data);
                RootDataBean<List<ChannelVodBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {}.getType());
                return bean.getData();
            }
        });

    }

    /**
     * 详情页面下的相似节目推荐
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"progid\": %s}"
     */
    public static final String VOD_RECOMMEND_CHANNEL = "/client/recommend_by_tag/";//"/client/vod/recommend";
    public static final Map.Entry<String, Observable<List<ChannelVodBean>>> remoterVodRecommendChannel(String videoType, final long channelId, ICacheApi cacheApi) {
        String key = ByteString.of((VOD_RECOMMEND_CHANNEL + "+" + videoType + "+" + channelId).getBytes()).md5().hex();
        return new AbstractMap.SimpleEntry<>(key, Observable.defer(new Callable<ObservableSource<List<ChannelVodBean>>>() {
            @Override
            public ObservableSource<List<ChannelVodBean>> call() throws Exception {
                return Observable.just(0).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, ObservableSource<List<ChannelVodBean>>>() {
                    @Override
                    public ObservableSource<List<ChannelVodBean>> apply(Integer integer) throws Exception {
                        String url = CMS_MAVIS_URL + VOD_RECOMMEND_CHANNEL + videoType;
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("account", ACCOUNT_ID);
                        map.put("token", TOKEN);
                        //map.put("dtype", "1000");
                        map.put("isUnlock", !Constants.SUBJECT_LOCK.getValue());
                        map.put("language", LANGUAGE);
                        map.put("channelids",new long[] {channelId});
                        String content = GsonHelper.toJson(map);
                        return loadRemote(url, content, key, cacheApi, API_VERSION_3)
                                .map(new Function<String, List<ChannelVodBean>>() {
                                    @Override
                                    public List<ChannelVodBean> apply(String data) throws Exception {
                                        RootDataBean<List<ChannelVodBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {}.getType());
                                        return bean.getData();
                                    }
                                });
                    }
                });
            }
        }));
    }
    public static final Observable<List<ChannelVodBean>> requestVodRecommendChannel(String videoType, final long channelId) {
        return Observable.just(videoType).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String videoType) throws Exception {
                String key = ByteString.of((VOD_RECOMMEND_CHANNEL + "+" + videoType + "+" + channelId).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                //String content = String.format("{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"progid\": %s}", ACCOUNT_ID, TOKEN, LANGUAGE, channelId);
                String url = CMS_MAVIS_URL + VOD_RECOMMEND_CHANNEL + videoType;
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                //map.put("dtype", "1000");
                map.put("isUnlock", !Constants.SUBJECT_LOCK.getValue());
                map.put("language", LANGUAGE);
                map.put("channelids",new long[] {channelId});
                String content = GsonHelper.toJson(map);
                Observable<String> remote = loadRemote2(url, content, key, cacheApi, true, API_VERSION_3);
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, List<ChannelVodBean>>() {
            @Override
            public List<ChannelVodBean> apply(String data) throws Exception {
                SLog.d(TAG, "requestVodChannel map data=>" + data);
                RootDataBean<List<ChannelVodBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {}.getType());
                return bean.getData();
            }
        });
    }

    /**
     * 详情页面下的相似节目推荐
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"progid\": %s}"
     */
    public static final String VOD_CLIENT_RECOMMEND = "/client/recommend/";//"/client/vod/recommend";

    public static final Observable<List<CategoryBean>> requestVodClientRecommend(String videoType) {
        return Observable.just(videoType).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String videoType) throws Exception {
                int position = 2;//?
                String key = ByteString.of((VOD_CLIENT_RECOMMEND + "+" + videoType + "+" + position).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                //String content = String.format("{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"progid\": %s}", ACCOUNT_ID, TOKEN, LANGUAGE, channelId);
                String url = CMS_MAVIS_URL + VOD_CLIENT_RECOMMEND + videoType;
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                //map.put("dtype", "1000");
                map.put("isUnlock", !Constants.SUBJECT_LOCK.getValue());
                map.put("language", LANGUAGE);
                map.put("position", position);
                String content = GsonHelper.toJson(map);
                Observable<String> remote = loadRemote(url, content, key, cacheApi, API_VERSION_3);
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }

        }).flatMap(new Function<String, ObservableSource<List<CategoryBean>>>() {
            @Override
            public ObservableSource<List<CategoryBean>> apply(String data) throws Exception {
                SLog.d(TAG, "requestVodChannel map data=>" + data);
                RootDataBean<List<CategoryBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<CategoryBean>>>() {}.getType());
                //RootVodChannelBean bean = GsonHelper.toType(data, RootVodChannelBean.class);
                return Observable.just(bean.getData());
            }
        });

    }


    //根据节目Id搜索
    public static final Map.Entry<String, Observable<List<ChannelVodBean>>> remoterVodChannelByChannelId(String columnUnid, ICacheApi cacheApi, final long... channelIds) {
        String key = ByteString.of((VOD_SEARCH_CHANNEL + columnUnid + Arrays.asList(channelIds).toString()).getBytes()).md5().hex();
        return new AbstractMap.SimpleEntry<>(key, Observable.defer(new Callable<ObservableSource<List<ChannelVodBean>>>() {
            @Override
            public ObservableSource<List<ChannelVodBean>> call() throws Exception {
                return Observable.just(0).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, ObservableSource<List<ChannelVodBean>>>() {
                    @Override
                    public ObservableSource<List<ChannelVodBean>> apply(Integer integer) throws Exception {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("account", ACCOUNT_ID);
                        map.put("token", TOKEN);
                        map.put("language", LANGUAGE);
                        map.put("channelids",channelIds);
                        String content = GsonHelper.toJson(map);
                        String url = CMS_MAVIS_URL + VOD_SEARCH_CHANNEL + columnUnid;
                        return loadRemote(url, content, key, cacheApi, API_VERSION_3)
                                .map(new Function<String, List<ChannelVodBean>>() {
                                    @Override
                                    public List<ChannelVodBean> apply(String data) throws Exception {
                                        SLog.d(TAG, "requestVodChannel map data=>" + data);
                                        RootDataBean<List<ChannelVodBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {
                                        }.getType());
                                        return bean.getData();
                                    }
                                });
                    }
                });
            }
        }));
    }


    /**
     * 详专辑节目查询
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"progid\": %s}"
     */
    public static final String VOD_QUERY_SERIALS = "/client/queryById/";//"/client/vod/querySerials";

    public static final Map.Entry<String, Observable<List<ChannelVodBean>>> remoterVodQuerySerials(String channelId, ICacheApi cacheApi) {
        String key = ByteString.of((VOD_QUERY_SERIALS + channelId).getBytes()).md5().hex();
        return new AbstractMap.SimpleEntry<>(key, Observable.defer(new Callable<ObservableSource<List<ChannelVodBean>>>() {
            @Override
            public ObservableSource<List<ChannelVodBean>> call() throws Exception {
                return Observable.just(0).subscribeOn(Schedulers.io()).flatMap(new Function<Integer, ObservableSource<List<ChannelVodBean>>>() {
                    @Override
                    public ObservableSource<List<ChannelVodBean>> apply(Integer integer) throws Exception {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("account", ACCOUNT_ID);
                        map.put("token", TOKEN);
                        map.put("dtype", "1000");
                        map.put("language", LANGUAGE);
                        map.put("channelid", Integer.parseInt(channelId));
                        String content = GsonHelper.toJson(map);
                        String url = CMS_MAVIS_URL + VOD_QUERY_SERIALS;
                        return loadRemote(url, content, key, cacheApi, API_VERSION_3).map(new Function<String, List<ChannelVodBean>>() {
                            @Override
                            public List<ChannelVodBean> apply(String data) throws Exception {
                                RootDataBean<CategoryBean> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<CategoryBean>>(){}.getType());
                                return bean.getData().getChannelList();
                            }
                        });
                    }
                });
            }
        }));
    }

    public static final Observable<List<ChannelVodBean>> requestVodQuerySerials(String channelId) {
        return Observable.just(channelId).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String channelId) throws Exception {
                String key = ByteString.of((VOD_QUERY_SERIALS + "+" + channelId).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                //String content = String.format("{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"progid\": %s}", ACCOUNT_ID, TOKEN, LANGUAGE, channelId);
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("dtype", "1000");
                map.put("language", LANGUAGE);
                map.put("channelid", Integer.parseInt(channelId));
                String content = GsonHelper.toJson(map);
                String url = CMS_MAVIS_URL + VOD_QUERY_SERIALS;
                Observable<String> remote = loadRemote2(url, content, key, cacheApi, true, API_VERSION_3);
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, List<ChannelVodBean>>() {
            @Override
            public List<ChannelVodBean> apply(String data) throws Exception {
                RootDataBean<CategoryBean> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<CategoryBean>>(){}.getType());
                //RootVodChannelBean bean = GsonHelper.toType(data, RootVodChannelBean.class);
                return bean.getData().getChannelList();
            }
        });

    }


    /**
     * 详专辑节目查询  1:live; 2:vod
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"type\": %s}"
     */
    public static final String REQUEST_TAG_LIST = "/client/tags";

    public static final Observable<List<TagTypeBean>> requestTagList(@TagType int type) {
        return Observable.just(type +"").flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String type) throws Exception {
                String key = ByteString.of((REQUEST_TAG_LIST + "+" + type).getBytes()).md5().hex();
                LocalCache cacheApi = LocalCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                String content = String.format("{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"type\": %s}", ACCOUNT_ID, TOKEN, LANGUAGE, type);
                String url = CMS_MAVIS_URL + REQUEST_TAG_LIST;
                Observable<String> remote = loadRemote(url, content, key, cacheApi, API_VERSION_3);
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, List<TagTypeBean>>() {
            @Override
            public List<TagTypeBean> apply(String data) throws Exception {
                RootDataBean<List<TagTypeBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<TagTypeBean>>>() {
                }.getType());
                return bean.getData();
            }
        });

    }

    /**
     * 提交评分接口
     */
    public static final String REQUEST_SCORE_COMMIT = "/client/score";
    public static Observable<ResultBean> requestScoreCommit(String channelId, int rating) {
        String content = String.format("{\"programmeId\": \"%s\", \"rating\": %d, \"account\": \"%s\", \"token\": \"%s\"}", channelId, rating, ACCOUNT_ID, TOKEN);
        return Observable.just(content)
                .flatMap(new Function<String, ObservableSource<ResultBean>>() {
                    @Override
                    public ObservableSource<ResultBean> apply(String content) throws Exception {
                        return loadRemote(CMS_MAVIS_URL + REQUEST_SCORE_COMMIT, content, null, null)
                                .map(new Function<String, ResultBean>() {
                                    @Override
                                    public ResultBean apply(String s) throws Exception {
                                        return GsonHelper.toType(s, ResultBean.class);
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io());
    }


    public static final String VOD_SUB_COLUMN = "/client/subcolumn/";

    /**
     * 获取分组下的二级分组
     */
    public static final Observable<List<CategoryBean>> requestSubColumn(String categortId, final int pageNum, final int subColumnPageSize) {
        return Observable.just(categortId).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull String categoryId) throws Exception {
                String key = ByteString.of((VOD_SUB_COLUMN + "+" + categoryId + "+" + pageNum + "|" + subColumnPageSize).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                //String content = String.format("{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"type\": %s}", ACCOUNT_ID, TOKEN, LANGUAGE, type);
                Map<String, Object> params = new HashMap<>();
                params.put("account", ACCOUNT_ID);
                params.put("token", TOKEN);
                params.put("language", LANGUAGE);
                params.put("pageSize", 5);
                params.put("subColumnPageSize", subColumnPageSize);
                params.put("pageNum", pageNum);
                String content = GsonHelper.toJson(params);
                String url = CMS_MAVIS_URL + VOD_SUB_COLUMN + categoryId;
                Observable<String> remote = loadRemote(url, content, key, cacheApi, API_VERSION_3);
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).flatMap(new Function<String, ObservableSource<List<CategoryBean>>>() {
            @Override
            public ObservableSource<List<CategoryBean>> apply(@NonNull String s) throws Exception {
                SLog.d(TAG, "requestRoleList s=>" + s);
                RootDataBean<List<CategoryBean>> bean = GsonHelper.toType(s, new TypeToken<RootDataBean<List<CategoryBean>>>() {
                }.getType());
                return Observable.just(bean.getData());
            }
        });
    }

    private static final String AUTH_PROGRAMME = "/client/auth/programme";
    public static Observable<Integer> requestChannelAuth(String channelId){
        return Observable.just(channelId)
                .flatMap(new Function<String, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(String programmeId) throws Exception {
                        String content = String.format("{\"account\": \"%s\",\"token\": \"%s\",\"language\": \"%s\",\"programmeId\": \"%s\"}", ACCOUNT_ID, TOKEN, LANGUAGE, programmeId);
                        return loadRemote(CMS_MAVIS_URL + AUTH_PROGRAMME, content, null, null)
                                .map(new Function<String, Integer>() {
                                    @Override
                                    public Integer apply(String s) throws Exception {
                                        ResultBean bean = GsonHelper.toType(s, ResultBean.class);
                                        return bean.getResult();
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io());
    }

    public static final String VOD_CAST = "/client/castList/";

    /**
     * 获取演职员列表
     *
     * @param roleType
     * @return 演职员类型 1：导演；2：演员；3：编辑
     * 接口参数
     * {
     * "role": 1,                  //演职员类型 1：导演；2：演员；3：编辑
     * "keyword": "leim",          // 模糊查询
     * "account": "12",            //登录account
     * "token": "adfasddfgooulkj12kjhfasd",// 登录凭证（上次登录缓存）
     * "pageSize": 1,
     * "pageNum": 10
     * }
     */
    public static final Observable<RoleListBean> requestRoleList(int roleType, int pageNum, int pageSize, final String keyword) {
        Map<String, Object> params = new HashMap<>();
        params.put("role", roleType);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        params.put("keyword", keyword);
        return Observable.just(params).flatMap(new Function<Map<String, Object>, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(@NonNull Map<String, Object> stringObjectMap) throws Exception {
                String key = ByteString.of((VOD_CAST + pageSize + "/" + pageNum + "/" + roleType + "/" + keyword).getBytes()).md5().hex();
                ICacheApi cacheApi;
                Observable<String> cache;
                if (pageNum == 1) {
                    cacheApi = LocalCache.getInstance();
                    cache = LocalCache.getInstance().loadCache(key, LANGUAGE);
                }else {
                    cacheApi = LruDiskCache.getInstance();
                    cache = LruDiskCache.getInstance().loadCache(key, LANGUAGE, 0);
                }
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                //String content = String.format("{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"type\": %s}", ACCOUNTID, TOKEN, LANGUAGE, type);
                params.put("account", ACCOUNT_ID);
                params.put("token", TOKEN);
                params.put("language", LANGUAGE);
                String content = GsonHelper.toJson(params);
                String url = CMS_MAVIS_URL + VOD_CAST;
                Observable<String> remote = loadRemote(url, content, key, cacheApi, API_VERSION_3);
                return remote;//Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, RoleListBean>() {
            @Override
            public RoleListBean apply(@NonNull String s) throws Exception {
                //SLog.d(TAG, "requestRoleList s=>" + s);
                RoleListBean bean = GsonHelper.toType(s, RoleListBean.class);
                //                for (int i = 0; i < bean.getData().size(); i++) {
                //                    bean.getData().get(i).setType(ChannelVodBean.VIDEO_TYPE_ROLE);
                //                }
                return bean;
            }
        });
    }

    /**
     * 根据 keyword 或 tagId 搜索 tag包括 年份、类型等信息
     * "{\"account\": \"%s\", \"token\": \"%s\",\"language\": \"%s\",\"pageSize\": %s,\"orderType\": %s,\"pageNum\": %s}"
     * vod 所有点播节目
     * movies_all 电影
     * serials_all 电视剧
     * anime_all 动漫
     */

    public static final Observable<RootDataBean<List<ChannelVodBean>>> requestVodRoleSearchChannel(final String videoType, int castId, final boolean isUnlock, final int orderType, final int pageNum, final String keyword, final int... tagids) {
        return Observable.just(castId).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer castId) throws Exception {
                HashMap<String, Object> map = new HashMap<>();
                map.put("account", ACCOUNT_ID);
                map.put("token", TOKEN);
                map.put("language", LANGUAGE);
                map.put("isUnlock", isUnlock);
                map.put("orderType", orderType);
                map.put("pageSize", PAGE_SIZE);
                map.put("pageNum", pageNum);
                map.put("castId", castId);
                //map.put("dtype", "1000");
                if (tagids != null && tagids.length != 0) {
                    map.put("tagids", Arrays.asList(tagids));
                } else {
                    map.put("keyword", keyword);
                }
                String content = GsonHelper.toJson(map);
                SLog.i(TAG, "requestVodSearchChannel remote content=>" + content + " ,columnUnid=" + castId);
                String key = ByteString.of((VOD_SEARCH_CHANNEL + videoType + castId + content).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                String url = CMS_MAVIS_URL + VOD_CATEGORY_CHANNEL + videoType;
                Observable<String> remote = loadRemote(url, content, key, cacheApi, API_VERSION_3)
                        .doOnNext(new Consumer<String>() {
                            @Override
                            public void accept(String data) throws Exception {
                                //RootVodChannelBean rootBean = GSON.fromJson(data, RootVodChannelBean.class);
                                //SLog.i(TAG, "requestVodChannel remote data.size=>" + rootBean.getChannelList().size());
                            }
                        });
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, RootDataBean<List<ChannelVodBean>>>() {
            @Override
            public RootDataBean<List<ChannelVodBean>> apply(String data) throws Exception {
                SLog.d(TAG, "requestVodChannel map data=>" + data);
                return GsonHelper.toType(data, new TypeToken<RootDataBean<List<ChannelVodBean>>>() {}.getType());
            }
        });
    }

    public static final String DOWNLOAD_DOMAIN_MAPPING = "/client/video_download_domain_mapping";
    public static final Observable<Map<String,String>> requestDownloadDomainMapping() {
        return Observable.defer(new Callable<ObservableSource<? extends Map<String, String>>>() {
            @Override
            public ObservableSource<? extends Map<String, String>> call() throws Exception {
                String url = CMS_MAVIS_URL + DOWNLOAD_DOMAIN_MAPPING;
                String content = String.format("{\"account\": \"%s\",\"token\": \"%s\"}", ACCOUNT_ID, TOKEN);
                return loadRemote(url, content, null, null)
                        .map(new Function<String, Map<String, String>>() {
                            @Override
                            public Map<String, String> apply(String s) throws Exception {
                                Map<String, String> mappings = new HashMap<>();
                                JSONArray jsonArray = new JSONObject(s).getJSONArray("data");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jobject = jsonArray.getJSONObject(i);
                                    mappings.put(jobject.getString("source"), jobject.getString("target"));
                                }
                                return mappings;
                            }
                        });
            }
        }).subscribeOn(Schedulers.io());
    }



    /************************************* - setting 接口 - ****************************************/
    /**
     * 消息查询接口
     *
     */
    public static final String REQUEST_APP_MESSAGE = "/client/message/app";

    public static final Observable<ApkListBean> requestMessage(String packageName) {
        return Observable.just(packageName).flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String packageName) throws Exception {
                String key = ByteString.of((REQUEST_APP_MESSAGE + "+" + packageName).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                StringBuilder content = new  StringBuilder();
                content.append("?")
                        .append("packageName").append("=").append(packageName);
                String url = CMS_MAVIS_URL + REQUEST_APP_MESSAGE + content.toString();
                Observable<String> remote = loadRemote(url, "", key, cacheApi, API_VERSION_3);
                return Observable.concat(cache, remote);
            }
        }).distinctUntilChanged(new Function<String, String>() {
            @Override
            public String apply(@NonNull String data) throws Exception {
                return ByteString.of(data.getBytes()).md5().hex();
            }
        }).map(new Function<String, ApkListBean>() {
            @Override
            public ApkListBean apply(String data) throws Exception {
                ApkListBean bean = GsonHelper.toType(data, ApkListBean.class);
                return bean;
            }
        });
    }

    /**
     * 消息查询接口
     *
     */
    public static final String REQUEST_APP_INFO = "/app/api/appinfo";

    public static final Observable<ApkUpgradeInfo> requestAppInfo() {
        return Observable.just("").flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                String key = ByteString.of((REQUEST_APP_INFO + "+" + APP_PACKAGE_NAME + "+" + APP_VERSION_CODE).getBytes()).md5().hex();
                LruDiskCache cacheApi = LruDiskCache.getInstance();
                Observable<String> cache = cacheApi.loadCache(key, LANGUAGE, 0);
                if (TextUtils.isEmpty(CMS_MAVIS_URL)) {
                    return cache;
                }
                StringBuilder content = new StringBuilder();
                content.append("?")
                        .append("packageName").append("=").append(APP_PACKAGE_NAME)
                        .append("&").append("versionCode").append("=").append(APP_VERSION_CODE);
                String url = CMS_MAVIS_URL + REQUEST_APP_INFO + content.toString();
                Observable<String> remote = loadRemote(url, "", key, cacheApi, API_VERSION_3);
                return Observable.concat(cache, remote);
            }
        }).map(new Function<String, ApkUpgradeInfo>() {
            @Override
            public ApkUpgradeInfo apply(String data) throws Exception {
                ApkUpgradeInfo bean = GsonHelper.toType(data, ApkUpgradeInfo.class);
                return bean;
            }
        }).subscribeOn(Schedulers.io());
    }


    /************************************* - 获取用户信息 - ****************************************/
    /**
     * 获取用户信息
     */
    private static Observable<String> loadAccountData(String path, String content) {
        return MyAppLogin.getInstance().requestData(path, content)
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String response) throws Exception {
                        ResultBean bean = GsonHelper.toType(response, ResultBean.class);
                        if (bean.getResult() != ResultCode.CODE_0) {
                            throw new IllegalStateException("Result code=" + bean.getResult() + "  Result=>" + response);
                        }
                        int start;
                        int end = response.indexOf("[");
                        if (end == -1) {
                            end = response.indexOf("\":{\"");
                        } else {
                            end = response.lastIndexOf("\":{\"", end);
                            if (end == -1) {
                                start = response.lastIndexOf("]");
                                end = response.indexOf("\":{\"", start);
                            }
                        }
                        if (end == -1) {
                            end = response.indexOf("\":[{\"");
                            if (end != -1) {
                                start = response.lastIndexOf("{\"", end);
                                if (start != 0) {
                                    end = -1;
                                }
                            }
                        }
                        if (end != -1) {
                            start = response.lastIndexOf("\"", end - 1);
                            response = response.substring(0, start + 1) + "data" + response.substring(end);
                        }
                        return response;
                    }
                });
    }

    /**
     * 获取订单信息
     */
    private static final String ORDER_LIST="/bs/order/list";

    public static Observable<List<OrderBean>> requestOrderInfo(int pageNum) {
        return Observable.just(pageNum)
                .flatMap(new Function<Integer, ObservableSource<List<OrderBean>>>() {
                    @Override
                    public ObservableSource<List<OrderBean>> apply(Integer pageNum) throws Exception {
                        String content = String.format("{\"accountId\": \"%s\", \"token\": \"%s\", \"pageSize\" : %d, \"pageNum\" : %d}",
                                ACCOUNT_ID, TOKEN, PAGE_SIZE, pageNum);
                        return loadAccountData(ORDER_LIST, content)
                                .map(new Function<String, List<OrderBean>>() {
                                    @Override
                                    public List<OrderBean> apply(String data) throws Exception {
                                        RootDataBean<List<OrderBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<OrderBean>>>() {}.getType());
                                        return bean.getData();
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io());
    }

    /**
     * 获取服务信息
     */
    private static final String SERVICE_LIST="/bs/charge/serviceList";
    public static Observable<List<BuyServiceBean>> requestBuyService() {
        return Observable.just(0)
                .flatMap(new Function<Integer, ObservableSource<List<BuyServiceBean>>>() {
                    @Override
                    public ObservableSource<List<BuyServiceBean>> apply(Integer pageNum) throws Exception {
                        return loadAccountData(SERVICE_LIST, ACCOUNT_JSON)
                                .map(new Function<String, List<BuyServiceBean>>() {
                                    @Override
                                    public List<BuyServiceBean> apply(String data) throws Exception {
                                        RootDataBean<List<BuyServiceBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<BuyServiceBean>>>() {}.getType());
                                        return bean.getData();
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io());
    }

    /**
     * 通过邮箱发送购买链接
     */
    private static final String EMAIL_CREATE_MEMBER_SERVICE="/bs/email/sendPay";
    public static Observable<EmailCodeBean> requestEmailCreateMemberService(String email, String serviceId, String payUrl) {
        return Observable.just(0)
                .flatMap(new Function<Integer, ObservableSource<EmailCodeBean>>() {
                    @Override
                    public ObservableSource<EmailCodeBean> apply(Integer integer) throws Exception {
                        String json = String.format("{\"accountId\": \"%s\", \"token\": \"%s\", \"email\": \"%s\", \"serviceId\": \"%s\", \"pay_url\": \"%s\", \"language\": \"%s\"}",
                                ACCOUNT_ID, TOKEN, email, serviceId, payUrl, LANGUAGE);
                        return loadAccountData(EMAIL_CREATE_MEMBER_SERVICE, json)
                                .map(new Function<String, EmailCodeBean>() {
                                    @Override
                                    public EmailCodeBean apply(String data) throws Exception {
                                        return GsonHelper.toType(data, EmailCodeBean.class);
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.io());
    }

    public static final String BUY_SUBSCRIPTION_THROUGH_EMAIL = "/bs/atv/email_subscribe";

    public static Flowable<RootDataBean> requestStoreThroughEmail(final String email) {
        return constructStoreUrl().flatMap(new Function<String, Publisher<? extends RootDataBean>>() {
            @Override
            public Publisher<? extends RootDataBean> apply(String storeUrl) throws Exception {
                String jsonParam =  String.format("{\"accountId\": \"%s\", \"token\": \"%s\", \"email\": \"%s\", \"url\": \"%s\", \"language\": \"%s\"}",
                        ACCOUNT_ID, TOKEN, email, storeUrl, LANGUAGE);
                return loadAccountData(BUY_SUBSCRIPTION_THROUGH_EMAIL, jsonParam)
                        .map(new Function<String, RootDataBean>() {
                            @Override
                            public RootDataBean apply(String data) throws Exception {
                                return GsonHelper.toType(data, RootDataBean.class);
                            }
                        }).subscribeOn(Schedulers.io()).toFlowable(BackpressureStrategy.BUFFER);
            }
        });
    }


    private static String MEMBER_SERVICE_HOST = "https://member.quicktvod.com";
    private static final String MEMBER_SERVICE_KEY="/tv/member/common";//"/rich/member/common";
    private static final String MEMBER_SERVICE_CREATE = "/rich/member/box/create";
    private static void setMemberServiceTest(boolean isTest) {
        MEMBER_SERVICE_HOST = isTest ? "https://richtv.fasaxy.com" : "https://member.quicktvod.com";
    }
    public static Observable<MemberBean> requestMemberServiceKey() {
        return Observable.create(new ObservableOnSubscribe<MemberBean>() {
            @Override
            public void subscribe(ObservableEmitter<MemberBean> emitter) throws Exception {
                Request request = new Request.Builder().url(MEMBER_SERVICE_HOST + MEMBER_SERVICE_KEY).post(RequestBody.create(MediaType.parse("text/plain"), "")).build();
                Call call = OkHttpHelper.getOkHttpClient().newCall(request);
                emitter.setDisposable(new Disposable() {
                    @Override
                    public void dispose() {
                        call.cancel();
                    }

                    @Override
                    public boolean isDisposed() {
                        return call.isCanceled();
                    }
                });
                Response response = call.execute();
                if (response.isSuccessful()) {
                    MemberBean bean = GsonHelper.toType(response.body().string(), MemberBean.class);
                    if (!TextUtils.isEmpty(bean.getKey())) {
                        emitter.onNext(bean);
                        emitter.onComplete();
                    } else {
                        emitter.onError(new IllegalStateException("Http response bean.key=null"));
                    }
                } else {
                    emitter.onError(new IllegalStateException("Http response code=" + response.code()));
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private final static String MEMBER_STORE_PATH = "https://member.atv-oficial.com/login";

    public static Flowable<String> constructStoreUrl() {
        return Flowable.just("").flatMap(new Function<String, Publisher<String>>() {
            @Override
            public Publisher<String> apply(String s) throws Exception {
                long utcTime = MyAppLogin.getInstance().getUtcTime();
                String jsonParam = String.format("{\"token\":\"%s\", \"uid\":%s, \"ln\":\"%s\",\"utc\":%s}", TOKEN, ACCOUNT_ID, LANGUAGE, utcTime);
                String enParam = AESUtil.aesEncryptForLiuMing(jsonParam, Constants.PPMGR_MEMBER_AES_KEY);
                String url = String.format("%s?param=%s", MEMBER_STORE_PATH, enParam);
                SLog.i(TAG, "[createStoreQrcode]jsonParam=" + jsonParam + " ,enParam=" + enParam);
                SLog.i(TAG, "[createStoreQrcode]url=" + url);
                return Flowable.just(url);
            }
        });
    }

    public static Flowable<Map.Entry<String,Bitmap>> createStoreQrcode(final int width, final int height) {
        return constructStoreUrl().flatMap(new Function<String, Publisher<Map.Entry<String,Bitmap>>>() {
            @Override
            public Publisher<Map.Entry<String,Bitmap>> apply(String url) throws Exception {
                final Bitmap bitmap = ZxingUtil.createQRImage(url, width, height);
                return Flowable.just(new AbstractMap.SimpleEntry<String, Bitmap>(url, bitmap));
            }
        }).onBackpressureBuffer();
    }

    public static Observable<Map.Entry<String,Bitmap>> createMemberServiceQrcode(String serviceId, String key, int width, int height) {
        return Observable.create(new ObservableOnSubscribe<Map.Entry<String,Bitmap>>() {
            private volatile boolean isDisposed;
            private Disposable disposable = new Disposable() {
                @Override
                public void dispose() {
                    isDisposed = true;
                }
                @Override
                public boolean isDisposed() {
                    return isDisposed;
                }
            };
            @Override
            public void subscribe(ObservableEmitter<Map.Entry<String,Bitmap>> emitter) throws Exception {
                emitter.setDisposable(disposable);
                long utcTime = MyAppLogin.getInstance().getUtcTime();
                String json = String.format("{\"uid\":\"%s\",\"uname\":\"%s\",\"sid\":\"%s\",\"utc\":%s,\"lu\":\"%s\"}", ACCOUNT_ID, ACCOUNT_NAME, serviceId, utcTime, LANGUAGE);
                SLog.d(TAG, "createMemberServiceQrcode param=>" + json);
                byte[] bytes = RSAUtil.encryptByPublicKey(json.getBytes("UTF-8"), Base64.decode(key, Base64.NO_WRAP));
                String param = new String(Base64.encode(bytes, Base64.URL_SAFE | Base64.NO_WRAP), "UTF-8");
                String url = MEMBER_SERVICE_HOST + MEMBER_SERVICE_CREATE + String.format("?param=%s&pt=%s", param, utcTime);
                if (!isDisposed) {
                    Bitmap bitmap = ZxingUtil.createQRImage(url, width, height);
                    if (!isDisposed) {
                        emitter.onNext(new AbstractMap.SimpleEntry<>(url, bitmap));
                        emitter.onComplete();
                    } else {
                        bitmap.recycle();
                    }
                }
            }
        }).subscribeOn(Schedulers.io());
    }


}

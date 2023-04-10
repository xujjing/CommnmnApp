package com.streambus.commonmodule.api;

import android.content.Context;

import com.google.gson.internal.LinkedTreeMap;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.bean.ChannelVodBean;
import com.streambus.commonmodule.login.MyAppLogin;
import com.streambus.commonmodule.table.DaoMaster;
import com.streambus.commonmodule.table.DaoSession;
import com.streambus.commonmodule.table.HistoryStatusInfo;
import com.streambus.commonmodule.table.IVodChannelInfo;
import com.streambus.commonmodule.table.VodFavChannelInfo;
import com.streambus.commonmodule.table.VodFavChannelInfoDao;
import com.streambus.commonmodule.table.VodHisChannelInfo;
import com.streambus.commonmodule.table.VodHisChannelInfoDao;
import com.streambus.commonmodule.utils.GsonHelper;
import com.streambus.commonmodule.utils.LanguageUtils;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2019/6/4
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class DaoHelper {

    private static final Object PRESENT = new Object();
    private static LinkedTreeMap<Long, Object> sVodFavoriteChannels;
    private static LinkedTreeMap<Long, Object> sVodHistoryChannels;
    private static DaoSession mDaoSession;

    private static final String TAG = "DaoHelper";

    public static void initDataBse(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, Constants.BASE_DB_NAME){
            @Override
            public void onUpgrade(Database db, int oldVersion, int newVersion) {
                SLog.d(TAG, "DaoMaster onUpgrade oldVersion=" + oldVersion + "  newVersion=" + newVersion);
                MigrationHelper.getInstance().migrateDb(db);
            }
        };
        Database database = helper.getWritableDb();
        mDaoSession = new DaoMaster(database).newSession();
        initFavoriteChannels();
    }

    public static final boolean isVodFavorite(Long channelId) {
        return sVodFavoriteChannels.containsKey(channelId);
    }

    public static final boolean isVodHistory(Long channelId) {
        return sVodHistoryChannels.containsKey(channelId);
    }

    private static void initFavoriteChannels() {
        sVodFavoriteChannels = new LinkedTreeMap<>();
        sVodHistoryChannels = new LinkedTreeMap<>();
        Disposable subscribe1 = Observable.just(mDaoSession.getVodFavChannelInfoDao())
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<VodFavChannelInfoDao>() {
                    @Override
                    public void accept(VodFavChannelInfoDao dao) throws Exception {
                        for (VodFavChannelInfo info : dao.loadAll()) {
                            sVodFavoriteChannels.put(info.getId(), PRESENT);
                        }
                    }
                }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends VodFavChannelInfoDao>>() {
                    @Override
                    public ObservableSource<? extends VodFavChannelInfoDao> apply(Throwable throwable) throws Exception {
                        return Observable.empty();
                    }
                }).subscribe();

        Disposable subscribe2 = Observable.just(mDaoSession.getVodHisChannelInfoDao())
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<VodHisChannelInfoDao>() {
                    @Override
                    public void accept(VodHisChannelInfoDao dao) throws Exception {
                        for (VodHisChannelInfo info : dao.loadAll()) {
                            sVodHistoryChannels.put(info.getId(), PRESENT);
                        }
                    }
                }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends VodHisChannelInfoDao>>() {
                    @Override
                    public ObservableSource<? extends VodHisChannelInfoDao> apply(Throwable throwable) throws Exception {
                        return Observable.empty();
                    }
                }).subscribe();
    }


    public static Observable<Boolean> deleteIsFav(boolean isFav){
        return Observable.just(isFav)
                .map(new Function<Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean aBoolean) throws Exception {
                        try {
                            if(aBoolean){
                                sVodFavoriteChannels.clear();
                                mDaoSession.getVodFavChannelInfoDao().deleteAll();
                            }else{
                                sVodHistoryChannels.clear();
                                mDaoSession.getVodHisChannelInfoDao().deleteAll();
                                mDaoSession.getHistoryStatusInfoDao().deleteAll();
                            }
                            return true;
                        }catch (Exception e){
                           SLog.e(TAG," deleteAll >>>>",e);
                        }
                        return false;
                    }
                });
    }

    public static Observable<Boolean> deleteAll(){
        return Observable.just("")
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String s) throws Exception {
                        try {
                            sVodFavoriteChannels.clear();
                            sVodHistoryChannels.clear();
                            mDaoSession.getVodFavChannelInfoDao().deleteAll();
                            mDaoSession.getVodHisChannelInfoDao().deleteAll();
                            mDaoSession.getHistoryStatusInfoDao().deleteAll();
                            return true;
                        }catch (Exception e){
                            SLog.e(TAG," deleteAll >>>>",e);
                        }
                        return false;
                    }
                });
    }

    /************************************* - VOD - ****************************************/
    public static Observable<List<ChannelVodBean>> getVodChannelList(boolean isFav) {
        return Observable.just(isFav)
                .flatMap(new Function<Boolean, ObservableSource<List<ChannelVodBean>>>() {
                    @Override
                    public ObservableSource<List<ChannelVodBean>> apply(Boolean isFav) throws Exception {
                        List<IVodChannelInfo> vodInfos;
                        if (isFav) {
                            vodInfos = new ArrayList<>(mDaoSession.getVodFavChannelInfoDao().queryBuilder().orderDesc(VodFavChannelInfoDao.Properties.UpdateTime).list());
                        } else {
                            vodInfos = new ArrayList<>(mDaoSession.getVodHisChannelInfoDao().queryBuilder().orderDesc(VodHisChannelInfoDao.Properties.UpdateTime).list());
                        }
                        if (vodInfos == null || vodInfos.isEmpty()) {
                            return Observable.just(new ArrayList<>());
                        }
                        return Observable.concat(jsonInfoToBean(vodInfos), updateInfoData(vodInfos));
                    }

                    private Observable<List<ChannelVodBean>> jsonInfoToBean(List<IVodChannelInfo> vodInfos) {
                        return Observable.just(vodInfos).map(new Function<List<IVodChannelInfo>, List<ChannelVodBean>>() {
                            @Override
                            public List<ChannelVodBean> apply(List<IVodChannelInfo> vodInfos) throws Exception {
                                ArrayList<ChannelVodBean> list = new ArrayList<>();
                                for (IVodChannelInfo info : vodInfos) {
                                    ChannelVodBean channelVodBean = GsonHelper.toType(info.getChannelJson(), ChannelVodBean.class);
                                    if (!isFav && ((VodHisChannelInfo)info).getUpdateTime() > 0) {
                                        channelVodBean.setUpdateTime(((VodHisChannelInfo)info).getUpdateTime());
                                    }
                                    list.add(channelVodBean);
                                }
                                return list;
                            }
                        });
                    }

                    private Observable<List<ChannelVodBean>> updateInfoData(List<IVodChannelInfo> infos) {//param:infos 所有的记录信息
                        return Observable.just(infos).flatMap(new Function<List<IVodChannelInfo>, ObservableSource<List<ChannelVodBean>>>() {
                            @Override
                            public ObservableSource<List<ChannelVodBean>> apply(List<IVodChannelInfo> infos) throws Exception {
                                long channelIds[] = new long[infos.size()];
                                for (int i = 0; i < infos.size(); i++) {
                                    channelIds[i] = infos.get(i).getId();
                                }
                                return RequestApi.requestVodSearchChannel("vod", channelIds)
                                        .doOnNext(new Consumer<List<ChannelVodBean>>() {
                                            @Override
                                            public void accept(List<ChannelVodBean> upList) throws Exception {
                                                updateVodInfo(isFav, upList, new LinkedList<>(infos));
                                            }
                                        });
                            }
                        }).onErrorResumeNext(new Function<Throwable, ObservableSource<List<ChannelVodBean>>>() {
                            @Override
                            public ObservableSource<List<ChannelVodBean>> apply(@NonNull Throwable throwable) throws Exception {
                                return Observable.empty();
                            }
                        });
                    }
                });
    }


    private final static void updateVodInfo(boolean isFav, List<ChannelVodBean> upList, LinkedList<IVodChannelInfo> infos) {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                for (ChannelVodBean bean: upList) {
                    IVodChannelInfo info = removeVodInfos(infos, Long.parseLong(bean.getId()));//找到旧的记录
                    if (info != null) {
                        if (isFav) {
                            info.setChannelJson(GsonHelper.toJson(bean));
                            mDaoSession.getVodFavChannelInfoDao().update((VodFavChannelInfo) info);
                        } else {
                            SLog.i(TAG, "[updateFavChannel]getUpdateTime=" + info.getUpdateTime());
                            bean.setUpdateTime(info.getUpdateTime());
                            info.setChannelJson(GsonHelper.toJson(bean));
                            mDaoSession.getVodHisChannelInfoDao().update((VodHisChannelInfo) info);
                        }
                    }
                }
            }
        });
    }

    private static final IVodChannelInfo removeVodInfos(LinkedList<IVodChannelInfo> infos, long id) {
        Iterator<IVodChannelInfo> iterator = infos.iterator();
        while (iterator.hasNext()) {
            IVodChannelInfo info = iterator.next();
            if (info.getId() == id) {
                iterator.remove();
                return info;
            }
        }
        return null;
    }

    public static Maybe<Boolean> addVodChannel(boolean isFav, ChannelVodBean bean, HistoryStatusInfo... statusInfo) {
        return Maybe.just(isFav)
                .map(new Function<Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean isFav) throws Exception {
                        try {
                            if (isFav) {
                                sVodFavoriteChannels.put(Long.parseLong(bean.getId()), PRESENT);
                                mDaoSession.getVodFavChannelInfoDao().insertOrReplace(new VodFavChannelInfo(Long.parseLong(bean.getId()), GsonHelper.toJson(bean), MyAppLogin.getInstance().getUtcTime()));
                            } else {
                                sVodHistoryChannels.put(Long.parseLong(bean.getId()), PRESENT);
                                mDaoSession.getVodHisChannelInfoDao().insertOrReplace(new VodHisChannelInfo(Long.parseLong(bean.getId()), GsonHelper.toJson(bean), MyAppLogin.getInstance().getUtcTime()));
                                if (statusInfo != null) {
                                    statusInfo[0].toJsonLookEpisodeString();
                                    statusInfo[0].toJsonLookTrailerString();
                                    mDaoSession.getHistoryStatusInfoDao().insertOrReplace(statusInfo[0]);
                                }
                            }
                            return Boolean.TRUE;
                        } catch (Exception e) {
                            SLog.e(TAG, "addVodChannel isFav=" + isFav, e);
                        }
                        return Boolean.FALSE;
                    }
                }).subscribeOn(Schedulers.io());
    }

    public static Observable<HistoryStatusInfo> getHistoryStatus(long channelId) {
        return Observable.just(channelId)
                .map(new Function<Long, HistoryStatusInfo>() {
                    @Override
                    public HistoryStatusInfo apply(Long channelId) throws Exception {
                        HistoryStatusInfo statusInfo = mDaoSession.getHistoryStatusInfoDao().load(channelId);
                        statusInfo.toTypeLookEpisodeMap();
                        statusInfo.toTypeLookTrailerMap();
                        return statusInfo;
                    }
                }).onErrorReturn(new Function<Throwable, HistoryStatusInfo>() {
                    @Override
                    public HistoryStatusInfo apply(Throwable throwable) throws Exception {
                        SLog.w(TAG, "getHistoryStatus throwable channelId=>" + channelId, throwable);
                        HistoryStatusInfo statusInfo = new HistoryStatusInfo();
                        statusInfo.setId(channelId);
                        statusInfo.setVideoLanguage(LanguageUtils.getDefaultAudioLanguage());
                        statusInfo.setSbtLanguage(LanguageUtils.getDefaultSubtitleLanguage());
                        return statusInfo;
                    }
                });
    }

    public static Maybe<Boolean> deleteVodChannel(boolean isFav, ChannelVodBean... channelVodBeans) {
        return Maybe.just(isFav)
                .map(new Function<Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean isFav) throws Exception {
                        try {
                            for (ChannelVodBean bean : channelVodBeans) {
                                if (isFav) {
                                    sVodFavoriteChannels.remove(Long.parseLong(bean.getId()));
                                    mDaoSession.getVodFavChannelInfoDao().deleteByKey(Long.parseLong(bean.getId()));
                                } else {
                                    sVodHistoryChannels.remove(Long.parseLong(bean.getId()));
                                    mDaoSession.getVodHisChannelInfoDao().deleteByKey(Long.parseLong(bean.getId()));
                                    mDaoSession.getHistoryStatusInfoDao().deleteByKey(Long.parseLong(bean.getId()));
                                }
                            }
                            return Boolean.TRUE;
                        } catch (Exception e) {
                            SLog.e(TAG, "deleteVodChannel isFav=" + isFav, e);
                        }
                        return Boolean.FALSE;
                    }
                }).subscribeOn(Schedulers.io());
    }
}

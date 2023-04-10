package com.stv.iptv.app;

import androidx.lifecycle.ViewModel;

import com.google.gson.reflect.TypeToken;
import com.streambus.basemodule.utils.NoDataException;
import com.streambus.basemodule.utils.SLog;
import com.streambus.basemodule.utils.SimpleRequestSubscriber;
import com.streambus.commonmodule.api.LocalCache;
import com.streambus.commonmodule.api.RequestApi;
import com.streambus.commonmodule.bean.ColumnBean;
import com.streambus.commonmodule.bean.RootDataBean;
import com.streambus.commonmodule.utils.GsonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/4/29
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class HomeViewModel extends ViewModel {
    private static final String TAG = "VodHomeColumnViewModel";
    private SerialDisposable mRequestDataDisposable;

    @Override
    protected void onCleared() {
        if (mRequestDataDisposable != null && !mRequestDataDisposable.isDisposed()) {
            mRequestDataDisposable.dispose();
        }
    }

    public void subjectRequestData(SimpleRequestSubscriber<List<ColumnBean>> subscriber) {
        mRequestDataDisposable = new SerialDisposable();
        mRequestDataDisposable.replace(Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                Map.Entry<String, Observable<List<ColumnBean>>> remoteEntry = RequestApi.remoterHomeType(LocalCache.getInstance());
                ReplaySubject<List<ColumnBean>> cacheReplay = ReplaySubject.create();
                Disposable cacheDisposable = mRequestDataDisposable.replace(LocalCache.getInstance().loadCache(remoteEntry.getKey(), RequestApi.LANGUAGE, new Function<String, List<ColumnBean>>() {
                            @Override
                            public List<ColumnBean> apply(String data) throws Exception {
                                RootDataBean<List<ColumnBean>> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<List<ColumnBean>>>() {
                                }.getType());
                                return bean.getData();
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .subscribe(cacheReplay::onNext, cacheReplay::onError, cacheReplay::onComplete))
                        ? mRequestDataDisposable.get() : null;

                mRequestDataDisposable.replace(remoteEntry.getValue().singleOrError()
                        .takeUntil(cacheReplay.singleOrError().delay(5000, TimeUnit.MILLISECONDS).onErrorResumeNext(new Single<List<ColumnBean>>() {
                            @Override
                            protected void subscribeActual(SingleObserver<? super List<ColumnBean>> observer) {
                                //缓存获取失败的话，不发送事件
                            }
                        }))
                        .doOnSuccess(columnBeans -> cacheDisposable.dispose())
                        .onErrorResumeNext(new Function<Throwable, SingleSource<? extends List<ColumnBean>>>() {
                            @Override
                            public SingleSource<? extends List<ColumnBean>> apply(Throwable throwable) throws Exception {
                                SLog.w(TAG, "doRemote onErrorResumeNext=>", throwable);
                                if (throwable instanceof RequestApi.ResultException && RequestApi.ResultCode.CODE_4 == ((RequestApi.ResultException) throwable).getResult()) {
                                    cacheDisposable.dispose();
                                    return Single.error(new NoDataException());
                                }
                                return cacheReplay.singleOrError();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(subscriber.onNext, subscriber.onError));
            }
        }));
    }

}

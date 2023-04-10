package com.streambus.basemodule.utils;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/4/29
 * 描    述:
 * 修订历史：
 * ================================================
 */
public abstract class SimpleObserver<T> implements Observer<T> {
    private static final String TAG = "SimpleObserver";
    protected Disposable disposable;
    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
    }

    @Override
    public abstract void onNext(T t);

    @Override
    public void onError(Throwable e){
        SLog.w(TAG, "onError", e);
    }

    @Override
    public void onComplete() {

    }
}

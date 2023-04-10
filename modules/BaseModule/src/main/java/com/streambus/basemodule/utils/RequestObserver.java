package com.streambus.basemodule.utils;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/4/29
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class RequestObserver<T> implements Observer<T> {
    private static final String TAG = "RequestObserver";
    protected final Function<T, Boolean> onNext;
    protected final Consumer<Throwable> onError;
    protected final Consumer<Boolean> onComplete;
    protected Consumer<Disposable> onSubscribe;

    protected Disposable disposable;
    private boolean isNext;
    public RequestObserver(Function<T, Boolean> onNext, Consumer<Throwable> onError, Consumer<Boolean> onComplete) {
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
    }
    public RequestObserver(Consumer<Disposable> onSubscribe, Function<T, Boolean> onNext, Consumer<Throwable> onError, Consumer<Boolean> onComplete) {
        this.onSubscribe = onSubscribe;
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
    }

    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
        if (onSubscribe != null) {
            try {
                onSubscribe.accept(disposable);
            } catch (Exception e) {
                disposable.dispose();
                onError(e);
            }
        }
    }

    @Override
    public void onNext(T t) {
        if (isNext) {
            return;
        }
        try {
            isNext = onNext.apply(t);
        } catch (Exception e) {
            disposable.dispose();
            onError(e);
        }
    }

    @Override
    public void onError(Throwable e){
        try {
            onError.accept(e);
        } catch (Exception e1) {
            RxJavaPlugins.onError(e1);
        }
    }

    @Override
    public void onComplete() {
        try {
            onComplete.accept(isNext);
        } catch (Exception e) {
            RxJavaPlugins.onError(e);
        }
    }
}

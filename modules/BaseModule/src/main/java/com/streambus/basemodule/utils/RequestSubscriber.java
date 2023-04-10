package com.streambus.basemodule.utils;

import java.util.EmptyStackException;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/7/8
 * 描    述:
 * 修订历史：
 * ================================================
 */
public abstract class RequestSubscriber<T>{
    private static final String TAG = "RequestSubscriber";
    protected Object data;
    public final Consumer<T> onCache = new Consumer<T>() {
        @Override
        public void accept(T t) throws Exception {
            data = t;
            handleCache(t);
        }
    };
    public final Consumer<T> onRemote = new Consumer<T>() {
        @Override
        public void accept(T t) throws Exception {
            data = t;
            handleRemote(t);
        }
    };

    public final Action onComplete = new Action() {
        @Override
        public void run() throws Exception {
            if (data == null) {
                handleError(new EmptyStackException());
                return;
            }
            handleComplete();
        }
    };

    public final Consumer<Throwable> onError = this::handleError;

    protected abstract void handleCache(T t);

    protected abstract void handleRemote(T t);

    protected void handleError(Throwable throwable) {
        SLog.w(TAG, "handleError", throwable);
    }

    protected void handleComplete() {
        SLog.i(TAG, "handleComplete");
    }

    public interface SimpleObserver<P>{
        boolean loadData(P data, Throwable throwable);
        boolean loadMoreData(P data, Throwable throwable);
    }

}

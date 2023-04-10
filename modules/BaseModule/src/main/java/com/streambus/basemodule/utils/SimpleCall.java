package com.streambus.basemodule.utils;

import java.util.concurrent.Callable;

import io.reactivex.functions.Cancellable;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/8/2
 * 描    述:
 * 修订历史：
 * ================================================
 */
public abstract class SimpleCall<T> implements Callable<T>, Cancellable {
    @Override
    public void cancel() throws Exception {

    }

}

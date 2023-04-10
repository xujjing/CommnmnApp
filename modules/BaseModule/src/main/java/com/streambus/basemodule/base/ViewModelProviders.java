package com.streambus.basemodule.base;

import android.app.Application;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/9/11
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class ViewModelProviders {
    public static final ViewModelProvider.Factory INSTANCE_FACTORY = new ViewModelProvider.NewInstanceFactory();
    public static final ViewModelProvider of(ViewModelStoreOwner viewModelStoreOwner) {
        return new ViewModelProvider(viewModelStoreOwner, INSTANCE_FACTORY);
    }
    public static final ViewModelProvider of(ViewModelStoreOwner viewModelStoreOwner, Application application) {
        return new ViewModelProvider(viewModelStoreOwner, ViewModelProvider.AndroidViewModelFactory.getInstance(application));
    }
}

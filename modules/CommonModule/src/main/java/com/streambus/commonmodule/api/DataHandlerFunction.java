package com.streambus.commonmodule.api;


import com.google.gson.reflect.TypeToken;
import com.streambus.commonmodule.bean.RootDataBean;
import com.streambus.commonmodule.utils.GsonHelper;

import java.util.AbstractMap;
import java.util.Map;

import io.reactivex.functions.Function;
import okio.ByteString;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/6/6
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class DataHandlerFunction<T> implements Function<String, Map.Entry<String,T>> {
    @Override
    public Map.Entry<String,T> apply(String data) throws Exception {
        RootDataBean<T> bean = GsonHelper.toType(data, new TypeToken<RootDataBean<T>>() {
        }.getType());
        return new AbstractMap.SimpleEntry<>(ByteString.of(data.getBytes()).md5().hex(), bean.getData());
    }
}

package com.streambus.commonmodule.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Response;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/7/23
 * 描    述:
 * 修订历史：
 * ================================================
 */
final class CallExecuteObservable extends Observable<String > {
    private static final String TAG = "CallExecuteObservable";
    private final boolean defHandData;
    private final Call originalCall;

    CallExecuteObservable(boolean defHandData, Call originalCall) {
        this.defHandData = defHandData;
        this.originalCall = originalCall;
    }

    private String defHandResponse(String response) {
        try {
            JsonElement parse = new JsonParser().parse(response);
            JsonObject jsonObject = parse.getAsJsonObject();
            if (jsonObject.get("result").getAsInt() != 0) {
                return response;// result != 0
            }
            Set<Map.Entry<String, JsonElement>> set = jsonObject.entrySet();
            Map.Entry<String, JsonElement> hitEntry = null;
            for (Map.Entry<String, JsonElement> entry : set) {
                if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray()) {
                    if (hitEntry != null) {
                        return response; //大于一个Json对象
                    }
                    hitEntry = entry;
                }
            }
            if (hitEntry == null) {
                return response;
            }
            jsonObject.remove(hitEntry.getKey());
            jsonObject.add("data", hitEntry.getValue());
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override protected void subscribeActual(Observer<? super String> observer) {
        // Since Call is a one-shot type, clone it for each new observer.
        Call call = originalCall.clone();
        observer.onSubscribe(new CallDisposable(call));
        boolean terminated = false;
        try {
            Response  response = call.execute();
            if (!call.isCanceled()) {
                if (!response.isSuccessful()) {
                    throw new IllegalStateException("http Error code=" + response.code());
                }
                String data = defHandData ? defHandResponse(response.body().string()) : response.body().string();
                observer.onNext(data);
            }
            if (!call.isCanceled()) {
                terminated = true;
                observer.onComplete();
            }
        } catch (Throwable t) {
            RequestApi.updateMavisUrl();
            Exceptions.throwIfFatal(t);
            if (terminated) {
                RxJavaPlugins.onError(t);
            } else if (!call.isCanceled()) {
                try {
                    observer.onError(t);
                } catch (Throwable inner) {
                    Exceptions.throwIfFatal(inner);
                    RxJavaPlugins.onError(new CompositeException(t, inner));
                }
            }
        }
    }

    private static final class CallDisposable implements Disposable {
        private final Call rawCall;
        private boolean canceled;

        CallDisposable(Call  call) {
            this.rawCall = call;
        }

        @Override public void dispose() {
            canceled = true;

            Call call;
            synchronized (this) {
                call = rawCall;
            }
            if (call != null) {
                call.cancel();
            }
        }

        @Override public boolean isDisposed() {
            if (canceled) {
                return true;
            }
            synchronized (this) {
                return rawCall != null && rawCall.isCanceled();
            }
        }
    }
}

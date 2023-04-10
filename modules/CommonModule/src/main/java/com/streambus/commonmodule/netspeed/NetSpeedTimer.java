package com.streambus.commonmodule.netspeed;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.streambus.basemodule.utils.SLog;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Yoostar on 2021/5/12 18:47
 */
public class NetSpeedTimer {
    private final String TAG = NetSpeedTimer.class.getSimpleName();

    private long defaultDelay = 1000;
    private long defaultPeriod = 1000;

    public static final int ERROR_CODE = -10101010;
    public static final int NET_SPEED_TIMER_DEFAULT = 101010;

    private int msgWhat = ERROR_CODE;
    private NetSpeed mNetSpeed;
    private Handler mHandler;
    private Context mContext;
    private Disposable disposable;

    public NetSpeedTimer(Context context, NetSpeed netSpeed, Handler handler) {
        this.mContext = context;
        this.mNetSpeed = netSpeed;
        this.mHandler = handler;
    }

    public NetSpeedTimer setDelayTime(long delay) {
        this.defaultDelay = delay;
        return this;
    }

    public NetSpeedTimer setPeriodTime(long period) {
        this.defaultPeriod = period;
        return this;
    }

    public NetSpeedTimer setHanderWhat(int what) {
        this.msgWhat = what;
        return this;
    }

    /**
     * 开启获取网速定时器
     */
    public void startSpeedTimer() {
        SLog.i(TAG, "[startSpeedTimer]++");
        if (null != disposable && !disposable.isDisposed()) {
            return;
        }
        disposable = Observable.interval(defaultDelay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (null != mNetSpeed && null != mHandler) {
                            Message obtainMessage = mHandler.obtainMessage();
                            if (msgWhat != ERROR_CODE) {
                                obtainMessage.what = msgWhat;
                            } else {
                                obtainMessage.what = NET_SPEED_TIMER_DEFAULT;
                            }
                            obtainMessage.obj = mNetSpeed.getNetSpeed(mContext
                                    .getApplicationInfo().uid);
                            mHandler.sendMessage(obtainMessage);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        SLog.e(TAG, "[startSpeedTimer][interval]", throwable);
                    }
                });

    }

    /**
     * 关闭定时器
     */
    public void stopSpeedTimer() {
        SLog.i(TAG, "[stopSpeedTimer]++");
        if (null != disposable && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }
}


package com.streambus.commonmodule.logs;

import com.streambus.basemodule.utils.SLog;
import com.streambus.requestapi.RALog;
import timber.log.Timber;
import tv.danmaku.ijk.media.DebugLog;

/**
 * @author :Yoostar
 * Date    :2020/7/29 9:30
 * Description :
 **/
public class LogImp implements SLog.ISLog, RALog.ReqApiLogImp, DebugLog.IDebug {

    /************************************* - SLog.ISLog - ****************************************/
    private LogImp() {
    }
    private static LogImp INSTANCE;
    public static LogImp getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LogImp();
        }
        return INSTANCE;
    }

    @Override
    public void v(String tag, String msg) {
        Timber.tag(tag).v(msg);
    }

    @Override
    public void d(String tag, String msg) {
        Timber.tag(tag).d(msg);
    }

    @Override
    public void i(String tag, String msg) {
        Timber.tag(tag).i(msg);
    }

    @Override
    public void w(String tag, String msg) {
        Timber.tag(tag).w(msg);
    }

    @Override
    public void w(String tag, String msg, Throwable t) {
        Timber.tag(tag).w(t, msg);
    }

    @Override
    public void e(String tag, String msg) {
        Timber.tag(tag).e(msg);
    }

    @Override
    public void e(String tag, String msg, Throwable t) {
        Timber.tag(tag).e(t,msg);
    }

    
    /************************************* - RALog.ReqApiLogImp, TinkerLog.TinkerLogImp - ****************************************/
    
    @Override
    public void v(String tag, String msg, Object... obj) {
        Timber.tag(tag).v(msg, obj);
    }

    @Override
    public void d(String tag, String msg, Object... obj) {
        Timber.tag(tag).d(msg, obj);
    }

    @Override
    public void i(String tag, String msg, Object... obj) {
        Timber.tag(tag).i(msg, obj);
    }

    @Override
    public void w(String tag, String msg, Object... obj) {
        Timber.tag(tag).w(msg, obj);
    }

    @Override
    public void e(String tag, String msg, Object... obj) {
        Timber.tag(tag).e(msg, obj);
    }

    @Override
    public void printErrStackTrace(String tag, Throwable tr, String format, Object... obj) {
        Timber.tag(tag).w(tr, format, obj);
    }
}

package tv.danmaku.ijk.media.player;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import tv.danmaku.ijk.media.DebugLog;


/**
 * Created by huanglu on 2019/8/28.
 */

public class IjkLineProbe {
    private static final String TAG = "IjkLineProbe";
    private static IjkLineProbe ijkLineProbe;
    private EventHandler mEventHandler;
    private HandlerThread eventThread;
    public LineDetection mLineDetection;
    private String mLineAry;
    private int mPlayId;

    public synchronized static IjkLineProbe getInstance() {
        if (null == ijkLineProbe) {
            ijkLineProbe = new IjkLineProbe();
        }
        return ijkLineProbe;
    }

    public IjkLineProbe() {
        initIjkLineProbe();
    }

    private void initIjkLineProbe() {
        Log.e(TAG, "[initIjkLineProbe]++");
        mPlayId = 0;
        mLineAry = "";
        IjkMediaPlayer.loadLibrariesOnce(null);
        if (eventThread == null || !eventThread.isAlive()) {
            eventThread = new HandlerThread("IjkLineProbe_callback thread");
            eventThread.start();
            mEventHandler = new EventHandler(this, eventThread.getLooper(), callback);
        }

//        Looper looper;
//        if ((looper = Looper.myLooper()) != null) {
//            mEventHandler = new EventHandler(this, looper, callback);
//        } else if ((looper = Looper.getMainLooper()) != null) {
//            mEventHandler = new EventHandler(this, looper, callback);
//        } else {
//            mEventHandler = null;
//        }
        _native_setup(new WeakReference<IjkLineProbe>(this), IjkMediaPlayer.IJK_LOG_VERBOSE);
    }

    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }


    public void setLineDetection(LineDetection lineDetection) {
        mLineDetection = lineDetection;
    }

    private static void postEventFromNative(Object weakThiz, int what,
                                            int arg1, Object obj) {
        Log.d(TAG, "postEventFromNative what = " + what + "  arg1= " + arg1 + "  obj= " + obj);

        if (weakThiz == null) {
            return;
        }
        @SuppressWarnings("rawtypes")
        IjkLineProbe mr = (IjkLineProbe) ((WeakReference) weakThiz).get();
        if (mr == null) {
            return;
        }
        Log.d(TAG, "postEventFromNative send what = " + what + "  arg1= " + arg1 + "  obj= " + obj + " ," + (mr.mEventHandler != null));
        if (mr.mEventHandler != null) {
            Message m = mr.mEventHandler.obtainMessage(what, arg1, 0, obj);
            mr.mEventHandler.sendMessage(m);
        }
    }

    private static class EventHandler extends Handler {
        private final WeakReference<IjkLineProbe> mWeakRecorder;
        static final int IJK_LINE_DETECTION = 0;
        private Callback callback;
        public EventHandler(IjkLineProbe mr, Looper looper, Callback callback) {
            super(looper, callback);
            this.callback = callback;
            mWeakRecorder = new WeakReference<IjkLineProbe>(mr);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "postEventFromNative send handleMessage");
            IjkLineProbe ijkLineProbe = mWeakRecorder.get();
            if (ijkLineProbe == null || ijkLineProbe.mPlayId == 0) {
                DebugLog.w(TAG,
                        "IjkLineProbe went away with unhandled events");
                return;
            }
        }
    }

    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case EventHandler.IJK_LINE_DETECTION:
                    ///处理返回的探测结果
                    Log.d(TAG, "postEventFromNative handleMessage what=" + msg.arg1 + " ,msg=" + (String) msg.obj);
                    if (mLineDetection != null) {
                        mLineDetection.lineDetection(msg.arg1, (String) msg.obj);
                    }
                    return true;
                default:
                    break;
            }
            return false;
        }
    };

    public int getPlayId() {
        return mPlayId;
    }

    public void setPlayId(int mPlayId) {
        this.mPlayId = mPlayId;
    }

    public String getLineAry() {
        return mLineAry;
    }

    public void setLineAry(String mLineAry) {
        this.mLineAry = mLineAry;
    }

    public void setLineDetecltion(int playId, String playLine) {
        _setLineDetecltion(playId, playLine);
    }

    private native void _native_setup(Object IjkLineProbe_this, int logType);

    private native void _setLineDetecltion(int playId, String playLine);

    private native void _native_stop();

    public interface LineDetection {
        void lineDetection(int playId, String optimizeLine);
    }

    public void release() {
        if (null != eventThread && eventThread.isAlive()) {
            eventThread.quit();
            eventThread = null;
            _native_stop();
        }
    }
}

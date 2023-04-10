package tv.danmaku.ijk.media.player;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.IntDef;
import tv.danmaku.ijk.media.DebugLog;


/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2018/9/20
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class IjkMediaRecorder extends Thread {

    private static final String TAG = "IjkMediaRecord";
    private final String BASE_PATH;
    private IChannel mCurrentChannel;
    private OnRecordCallBack mOnRecordCallBack;
    private OnRecordTsDeleteListener mOnRecordTsDeleteListener;
    private String mRecordMeadiaName;
    private String mSavePath;

    private long mNativeMediaRecorder;
    private EventHandler mEventHandler;

    //启动录制后返回结果 => int recordResponse = _startRecord
    public static final int RETURN_STATE_SUCCESS = 2;//录制成功没有任何异常
    public static final int RETURN_STATE_START = 1;//录制已启动
    public static final int RETURN_STATE_IDLE = 0;//录制未开始 未定义异常均返回该消息
    public static final int RETURN_STATE_CLOSE = 3;//停止录制 -- 无论是否录制完整 写入文件结束符都会返回这个消息
    public static final int RETURN_STATE_CREATE_M3U8_FIAL = -3;//创建本地录制文件.m3u8失败
    public static final int RETURN_STATE_MALLO_INSUFFICIENT_FIAL = -5;//检测磁盘可用大小，如果磁盘已经不足
    public static final int RETURN_STATE_STOP_REQUEST = -6;// 如果请求停止录制
    public static final int RETURN_STATE_FAILED_CONTEXT = -7;// malloc结构体失败
    public static final int RETURN_STATE_FAILED_INFO = -8;// 获取流信息失败
    public static final int RETURN_STATE_FAILED_DEMUX = -9;// 预读取封装结构失败
    public static final int RETURN_STATE_FAILED_NEWSTREAM = -10;// newstream失败
    public static final int RETURN_STATE_FAILED_OPENFILE = -11;// 磁盘写入新文件失败
    public static final int RETURN_STATE_FAILED_WRITEDATA = -12;// 磁盘写入文件数据失败
    public static final int RETURN_STATE_FAILED_WRITETRAILER = -13;// 磁盘写入文件尾信息失败
    public static final int RETURN_STATE_FAILED_FAT32 = 1024; ///当前U盘格式不支持
    /**
     * 记得把新建的TAG注入到下面的对应栏目中 TAG的取值只能取如下几种
     */
    private int mRecordType;
    public static final int RECORD_ONLY = 0; //
    public static final int RECORD_CONTINUED = 1; //
    private boolean mIsDeleteRecordFile;
    private File mSaveFile = null;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            RECORD_ONLY,//仅录制
            RECORD_CONTINUED,//仅录制
    })
    @interface RecordType {
    }


    public IjkMediaRecorder(String basePath) {
        BASE_PATH = basePath;

        File file = new File(BASE_PATH);
        if (!(file.exists() && file.isDirectory())) {
            file.delete();
            file.mkdir();

        }
        DebugLog.i(TAG, "instance IjkMediaRecorder path==>" + BASE_PATH);
        initRecorder();
    }

    private void initRecorder() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        native_setup(new WeakReference<IjkMediaRecorder>(this), IjkMediaPlayer.IJK_LOG_DEBUG);
    }

    public IChannel getCurrentChannel() {
        return mCurrentChannel;
    }

    public void setOnRecordCallBack(OnRecordCallBack onRecordCallBack) {
        mOnRecordCallBack = onRecordCallBack;
    }

    public void setOnRecordTsDeleteListener(OnRecordTsDeleteListener onRecordTsDeleteListener) {
        mOnRecordTsDeleteListener = onRecordTsDeleteListener;
    }

    private ArrayList<String> mCacheList = new ArrayList<>();

    public void startRecordMeadia(IChannel channel, @RecordType int recordType) {
        mCurrentChannel = channel;
        mRecordType = recordType;
        mRecordMeadiaName = channel.getRecordFileName();
        //  mRecordMeadiaName = fileName;
        mSavePath = BASE_PATH + "/" + mRecordMeadiaName;
        mCacheList.add(mSavePath);
        mSaveFile = new File(mSavePath);
        if (!(mSaveFile.exists() && mSaveFile.isDirectory())) {
            mSaveFile.delete();
            mSaveFile.mkdir();
        }
        start();
    }

    @Override
    public void run() {
        DebugLog.i(TAG, "启动时移_startRecord mCurrentChannel = " + mCurrentChannel.toString());
        int recordResponse = _startRecord(mCurrentChannel.getUrl(), mSavePath, mRecordMeadiaName, mCurrentChannel.getRecordMaxTime(), mRecordType);
        DebugLog.i(TAG, "recordResponse = " + recordResponse);
        postEventFromNative(mEventHandler.mWeakRecorder, EventHandler.RECORD_RESPONSE, recordResponse, 0, null);
        //_startRecord方法中进行视频录制，该方法执行完说明，视频录制已经结束，需要释放录制资源
        release();
        if (mIsDeleteRecordFile && recordResponse == RETURN_STATE_SUCCESS) {
            release();
            for (int i = 0; i < mCacheList.size(); i++) {
                DebugLog.i(TAG, "删除老的 文件delete RecordFile ==> " + mCacheList.get(i));
                delFolder(mCacheList.get(i));
                mCacheList.remove(i);
            }
        }
        mSaveFile = null;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    private static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    //删除文件夹
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
            DebugLog.i(TAG, "delFolder=>" + e.getMessage());
        }
    }

    private void release() {
        DebugLog.i(TAG, "record -- release");
        _release();
    }

    private void stopRecordMeadia() {
        DebugLog.i("XUYANZI", "stopRecordMeadia...........");
        _stopRecord();
        mCurrentChannel = null;
        mRecordMeadiaName = "";
        mSavePath = "";
    }

    /**
     * 结束时移
     */
    public void stopDeleteRecordMeadia() {
        stopRecordMeadia();
        mIsDeleteRecordFile = true;
    }


    private void onRecordBegin() {
        if (mOnRecordCallBack != null) {
            mOnRecordCallBack.onRecordBegin(mSavePath + "/" + mRecordMeadiaName + ".m3u8");
        }
    }

    private void onRecordError(int what) {
        if (mOnRecordCallBack != null) {
            mOnRecordCallBack.onRecordError(what);
        }
    }

    private void upDataRecordTitleTime(int recordSec) {
        if (mOnRecordCallBack != null) {
            mOnRecordCallBack.upDataRecordTitleTime(recordSec);
        }
    }

    private void onRecordComplete() {
        if (mOnRecordCallBack != null) {
            mOnRecordCallBack.onRecordComplete();
        }
    }

    private void onRecordResponse(int what) {
        if (mOnRecordCallBack != null) {
            mOnRecordCallBack.onRecordResponse(what);
        }
    }


    private void onRecordStop(int recordSec) {
        if (mOnRecordCallBack != null) {
            mOnRecordCallBack.onRecordStop(recordSec);
        }
    }


    private void onRecordTSDelete(int tsSec) {
        if (mOnRecordTsDeleteListener != null) {
            mOnRecordTsDeleteListener.onRecordTsDelete(tsSec);
        }
    }


    private static void postEventFromNative(Object weakThiz, int what,
                                            int arg1, int arg2, Object obj) {
        Log.d(TAG, "postEventFromNative what = " + what + "  arg1= " + arg1 + "  arg2= " + arg2 + "  obj= " + obj);

        if (weakThiz == null) {
            return;
        }

        @SuppressWarnings("rawtypes")
        IjkMediaRecorder mr = (IjkMediaRecorder) ((WeakReference) weakThiz).get();
        if (mr == null) {
            return;
        }
        if (mr.mEventHandler != null) {
            Message m = mr.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mr.mEventHandler.sendMessage(m);
        }
    }

    private static class EventHandler extends Handler {
        private final WeakReference<IjkMediaRecorder> mWeakRecorder;

        //发送消息类型
        static final int RECORD_ERROR = -10000;      // 失败
        static final int RECORD_WARN = -5;          // 异常
        static final int RECORD_RESPONSE = 0;           // 录制返回结果
        static final int RECORD_BEGIN = 1;           // 开始
        static final int RECORD_COMPLETE = 2;           // 完成
        static final int RECORD_UPDATE = 3;           // 更新时间
        static final int RECORD_TS_DELETE = 4;           // 删除最先创建的TS文件
        static final int RECORD_STOP = 5;           // 停止录制

        //错误类型
        static final int RECORD_MALLO_FAILED = -1;//M3U8文件写入失败
        static final int RECORD_OPTION_ERROR = -2;//未定义
        static final int RECORD_NONPACKET_ERROR = -3;//input网络io失败
        static final int RECORD_MALLO_INSUFFICIENT = -4;//磁盘异常 满了
        static final int RECORD_INPUTPACKET_NONVIDEO = -5;//15秒拉不到video数据
        static final int RECORD_OUTPUT_WRITE_FAILED = -6;//缓存文件写入数据失败

        public EventHandler(IjkMediaRecorder mr, Looper looper) {
            super(looper);
            mWeakRecorder = new WeakReference<IjkMediaRecorder>(mr);
        }

        @Override
        public void handleMessage(Message msg) {
            IjkMediaRecorder recorder = mWeakRecorder.get();
            if (recorder == null || recorder.mNativeMediaRecorder == 0) {
                DebugLog.w(TAG, "IjkMediaRecorder went away with unhandled events");
                return;
            }
            Log.d(TAG, "postEventFromNative handleMessage = " + msg.what +
                    "  arg1= " + msg.arg1 + "  arg2= " + msg.arg2 + "  obj= " + msg.obj +
                    " mOnRecordCallBack = " + recorder.mOnRecordCallBack);
            switch (msg.what) {
                case RECORD_BEGIN:
                    recorder.onRecordBegin();
                    return;
                case RECORD_UPDATE:
                    recorder.upDataRecordTitleTime(msg.arg1);
                    return;
                case RECORD_STOP:
                    recorder.onRecordStop(msg.arg1);
                    return;
                case RECORD_TS_DELETE:
                    recorder.onRecordTSDelete(msg.arg1);
                    return;
                case RECORD_COMPLETE:
                    recorder.onRecordComplete();
                    return;
                case RECORD_ERROR:
                    recorder.onRecordError(msg.arg1);
                    return;
                case RECORD_RESPONSE:
                    recorder.onRecordResponse(msg.arg1);
                    return;
                default:
                    DebugLog.w(TAG, "Unknown message type " + msg.what);
            }
        }
    }

    public interface IChannel {
        String getUrl();

        int getRecordMaxTime();

        String getRecordFileName();
    }


    public interface OnRecordCallBack {
        boolean onRecordBegin(String patchUrl);

        boolean onRecordError(int what);

        boolean onRecordComplete();

        void onRecordStop(int recordSec);

        boolean upDataRecordTitleTime(int recordSec);

        void onRecordResponse(int what);

    }

    public interface OnRecordTsDeleteListener {

        void onRecordTsDelete(int tsSec);
    }

    public static class RecorderChannel implements IChannel {
        private String recordFileName;
        private String url;
        private int recordMaxTime;

        public RecorderChannel(String url, String recordFileName, int recordMaxTime) {
            this.url = url;
            this.recordMaxTime = recordMaxTime;
            this.recordFileName = recordFileName;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public int getRecordMaxTime() {
            return recordMaxTime;
        }

        @Override
        public String getRecordFileName() {
            return recordFileName;
        }

        @Override
        public String toString() {
            return "RecorderChannel{" +
                    "url='" + url + '\'' +
                    ", recordMaxTime=" + recordMaxTime +
                    '}';
        }
    }

    /*
      1 为fat32
      2 为ntfs
       */
    public int getUSB() {
        return _getUSB();
    }

    private native void native_setup(Object IjkMediaRecorder_this, int logType);

    private native int _startRecord(String url, String savePath, String recordMeadiaName, int recordMaxTime, int recordType);

    private native void _stopRecord();

    private native void _release();

    private native static int _getUSB();
}

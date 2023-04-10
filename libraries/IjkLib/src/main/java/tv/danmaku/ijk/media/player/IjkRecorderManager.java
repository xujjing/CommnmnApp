package tv.danmaku.ijk.media.player;

import android.content.Context;
import android.util.Log;

import tv.danmaku.ijk.media.DebugLog;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2018/9/30
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class IjkRecorderManager {
    private static final String TAG = "TAG_IjkRecorderManager";
    private static IjkRecorderManager sRecorderManager;
    private final Context mApplication;
    private RecorderInfo mCurrentRecorderInfo;
    private boolean mIsRev = false;
    private String mRecKey = "";


    private IjkRecorderManager(Context application) {
        mApplication = application;
    }

    public static IjkRecorderManager getInstans(Context application) {
        if (sRecorderManager == null) {
            synchronized (IjkRecorderManager.class) {
                if (sRecorderManager == null) {
                    sRecorderManager = new IjkRecorderManager(application);
                }
            }
        }
        return sRecorderManager;
    }

    public boolean ismIsRev() {
        return mIsRev;
    }

    public void setmIsRev(boolean mIsRev) {
        this.mIsRev = mIsRev;
    }

    /**
     * 返回 0 ，表示 添加成功
     * 返回 >0, 表示有冲突，返回值为冲突节目的录制开始时间
     *
     * @param starRecodTime
     * @return
     */
    public void addCorderChannl(String recUrl, int rexMaxTime, long starRecodTime, String path, String channelName, String key) {
        DebugLog.w(TAG, "addCorderChannl===>" + "recUrl=>" + recUrl + "\n" + "###rexMaxTime=>" + rexMaxTime + "####=>" + starRecodTime + "###path=>" + path + "####key=>" + key);
        setmIsRev(true);
        RecorderInfo recorderInfo = new RecorderInfo(recUrl, rexMaxTime, starRecodTime, channelName, key);
        mRecKey = key;
        mCurrentRecorderInfo = recorderInfo;
    }

    /**
     * 结束当前正在录制的节目
     */
    public void stopCurrentRecording() {
        setmIsRev(false);
        if (mCurrentRecorderInfo != null) {
            mRecKey = "";
           // DebugLog.i(TAG, "stopCurrentRecording=》结束当前正在录制的节目");
            mCurrentRecorderInfo = null;
        }
    }


    /**
     * 当前是否存在录制的节目
     *
     * @return
     */
    public boolean isCurrentRecording() {
        if (mCurrentRecorderInfo != null) {
            return true;
        }
        return false;
    }

    private boolean mStartTimeShift = false;

    public boolean isCurrentTimeShift() {
        return mStartTimeShift;
    }

    public void setmTimeShift(boolean start) {
        mStartTimeShift = start;
    }

    /**
     * 当前正在录制的节目，不容许做删除操作
     *
     * @return
     */
    public String getCurrentRecordingName() {
        if (mCurrentRecorderInfo != null && mCurrentRecorderInfo.mChannelName != null) {
            return mCurrentRecorderInfo.mChannelName;
        }
        return "";
    }

    public String getCurrentRecordingKey() {
        if (mRecKey != null) {
            return mRecKey;
        }
        return "";
    }

    private IjkMediaRecorder mMediaRecorder;
    private IjkMediaRecorder.OnRecordCallBack mRecordCallBack = new IjkMediaRecorder.OnRecordCallBack() {
        @Override
        public boolean onRecordBegin(String patchUrl) {
            mCurrentRecorderInfo.mSavaPath = patchUrl;
            //Log.d(TAG, "onRecordBegin patchUrl =" + patchUrl);
            if (mCallBack != null) {
                mCallBack.call(IjkMediaRecorder.RETURN_STATE_START);
            }
            return true;
        }

        @Override
        public boolean onRecordError(int what) {
            Log.d(TAG, "onRecordError errorType = " + what);
            return true;
        }

        @Override
        public boolean onRecordComplete() {
            Log.d(TAG, "onRecordComplete");
            return true;
        }

        @Override
        public void onRecordStop(int recordSec) {
            mCurrentRecorderInfo.mRadRecordedTime = recordSec;
            Log.d(TAG, "onRecordStop ==> recordSec =  " + recordSec);
        }

        @Override
        public boolean upDataRecordTitleTime(int recordSec) {
            mCurrentRecorderInfo.mRadRecordedTime = recordSec;
            if (mCallBack != null) {
                mCallBack.ProgressUupdate(recordSec);
            }
            Log.d(TAG, "upDataRecordTitleTime ==> recordSec =  " + recordSec + "===>设置的总的录制时长为==>" + mCurrentRecorderInfo.mRecordMaxTime);
            return true;
        }

        @Override
        public void onRecordResponse(int what) {
            switch (what) {
                case IjkMediaRecorder.RETURN_STATE_SUCCESS:////录制完成
                   // DebugLog.i(TAG, "recorder RETURN_STATE_SUCCESS  mCurrentRecorderInfo ==> " + mCurrentRecorderInfo);
                    break;
                case IjkMediaRecorder.RETURN_STATE_MALLO_INSUFFICIENT_FIAL:
                    if (getUSB() == 1) {
                        what = IjkMediaRecorder.RETURN_STATE_FAILED_FAT32;
                    }
                    //Log.i(TAG, "recorder RETURN_STATE_MALLO_INSUFFICIENT_FIAL CODE  ==> " + getUSB());
                   // DebugLog.i(TAG, "recorder RETURN_STATE_MALLO_INSUFFICIENT_FIAL ==> " + mCurrentRecorderInfo);
                    break;
                case IjkMediaRecorder.RETURN_STATE_STOP_REQUEST:
                   // DebugLog.i(TAG, "recorder RETURN_STATE_STOP_REQUEST ==> " + mCurrentRecorderInfo);
                    break;
                case IjkMediaRecorder.RETURN_STATE_FAILED_CONTEXT:
                   // DebugLog.i(TAG, "recorder RETURN_STATE_FAILED_CONTEXT ==> " + mCurrentRecorderInfo);
                    break;
                case IjkMediaRecorder.RETURN_STATE_FAILED_INFO:
                  //  DebugLog.i(TAG, "recorder RETURN_STATE_FAILED_INFO ==> " + mCurrentRecorderInfo);
                    break;
                case IjkMediaRecorder.RETURN_STATE_FAILED_DEMUX:
                  //  DebugLog.i(TAG, "recorder RETURN_STATE_FAILED_DEMUX ==> " + mCurrentRecorderInfo);
                    break;
                case IjkMediaRecorder.RETURN_STATE_FAILED_NEWSTREAM:
                  //  DebugLog.i(TAG, "recorder RETURN_STATE_FAILED_NEWSTREAM ==> " + mCurrentRecorderInfo);
                    break;
                case IjkMediaRecorder.RETURN_STATE_FAILED_OPENFILE:
                  //  DebugLog.i(TAG, "recorder RETURN_STATE_FAILED_OPENFILE ==> " + mCurrentRecorderInfo);
                    break;
                default:
                   // DebugLog.w(TAG, "RECORDER_Undefined Response ==> " + mCurrentRecorderInfo);//未知。未定义
                    break;
            }
        }
    };

    public class RecorderInfo implements IjkMediaRecorder.IChannel {

        public RecorderInfo(String recUrl, int rexMaxTime, long recordStartTime, String channelName, String key) {
            this.mRecordStartTime = recordStartTime;
            this.mRecordMaxTime = rexMaxTime;
            this.mUrl = recUrl;
            this.mChannelName = channelName;
            this.mKey = key;
        }

        private String mUrl;

        private long mRecorderId;

        private long mRecordStartTime;// 在xxx毫秒时刻，开始录制

        private int mRecordMaxTime;  //  期望录制最长时间

        private int mRadRecordedTime; // 已经录制多长时间

        private int mRecordState;    //  当前录制的状态  0 - 未开始   1 - 已开始录制

        private String mSavaPath;    //  录制保存的文件名，播放录制到本地的视频地址

        private String mChannelName; ///保存的节目名称

        private String mKey; ///保存到数据库中的Key，用于录制完成后，从库中删除该条记录

        @Override
        public String getUrl() {
            return mUrl;
        }

        public String getmKey() {
            return mKey;
        }

        @Override
        public int getRecordMaxTime() {
            return mRecordMaxTime;
        }

        @Override
        public String getRecordFileName() {
            return null;
        }

        public void setRecordMaxTime(int recordMaxTime) {
            mRecordMaxTime = recordMaxTime;
        }

        @Override
        public String toString() {
            return "RecorderInfo{" +
                    "mUrl='" + mUrl + '\'' +
                    ", mRecorderId=" + mRecorderId +
                    ", mRecordStartTime=" + mRecordStartTime +
                    ", mRecordMaxTime=" + mRecordMaxTime +
                    ", mRadRecordedTime=" + mRadRecordedTime +
                    ", mRecordState=" + mRecordState +
                    ", mSavaPath='" + mSavaPath + '\'' +
                    ", mChannelName='" + mChannelName + '\'' +
                    ", mKey='" + mKey + '\'' +
                    '}';
        }
    }

    RecordingStateCallBack mCallBack;

    public void addCallBack(RecordingStateCallBack callBack) {
        mCallBack = callBack;
    }

    public void cancelCallBack() {
        DebugLog.i(TAG, "cancelCallBack.........");
        stopCurrentRecording();
        mCallBack = null;
    }


    public interface RecordingStateCallBack {
        void call(int code);

        void ProgressUupdate(int progress);
    }

    private int getUSB() {
        if (mMediaRecorder != null) {
           // DebugLog.i(TAG, "recorder getUSB CODE  ==> " + mMediaRecorder.getUSB());
            return mMediaRecorder.getUSB();
        }
        return 0;
    }
}

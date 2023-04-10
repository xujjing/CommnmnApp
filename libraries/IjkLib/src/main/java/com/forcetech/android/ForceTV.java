//package com.forcetech.android;
//
//import android.content.Context;
//import android.os.Environment;
//import android.util.Log;
//
//import java.io.File;
//import java.io.IOException;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import tv.danmaku.ijk.media.DebugLog;
//
//public class ForceTV {
//    private static final String TAG = "ForceTV";
//
//    public static final String FTV_CMD_START_P2P = "http://127.0.0.1:9908/cmd.xml?cmd=switch_chan&id=%s&server=%s&link=%s&userid=$user=$mac=%s$playkey=%s$username=%s$channelid=%s$columnid=%s$vodid=%s$key=%s";
//    public static final String FTV_CMD_STOP_P2P = "http://127.0.0.1:9908/api?func=stop_chan&id=%s";
//    public static final String FTV_CMD_STOP_ALL_P2P = "http://127.0.0.1:9908/api?func=stop_all_chan";
//    public static final String FTV_CMD_GET_P2P_INFO = "http://127.0.0.1:9908/api?func=query_chan_p2p_info&id=";
//    public static final String FTV_CMD_SET_UPLOAD_FLOW = "http://127.0.0.1:9908/api?func=set_up_flow&id=%s&max=%d&avg=%d";
//    public static final String FTV_CMD_GET_CLIENT_INFO = "http://127.0.0.1:9908/api?func=query_process_info";
//    public static final String FTV_CMD_GET_QUERY_CHAN_DATA_INFO = "http://127.0.0.1:9908/api?func=query_chan_p2p_info&id=";
//
//    private boolean mClientIsStart = false;
//    private static final int P2P_BUFFER_SIZE = 50 * 1024 * 1024;
//    private static ForceTV sForceTV;
//    public static ForceTV getInstance() {
//        return sForceTV;
//    }
//
//    public static void setup(Context context) {
//        if (sForceTV != null) {
//            sForceTV.closeClient();
//        }
//        sForceTV = new ForceTV();
//    }
//
//    public ForceTV() {
//        openClient();
//    }
//
//    public void openClient() {
//        DebugLog.i(TAG, "openClient()......");
//        mClientIsStart = false;
//        OkHttpClient client = new OkHttpClient.Builder().build();
//        Request request = new Request.Builder().url(FTV_CMD_GET_CLIENT_INFO).build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                DebugLog.e(TAG, "getClientInfo failure, mClientIsStart=" + mClientIsStart, e);
//                startForceTv();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                DebugLog.i(TAG, "getClientInfo result=" + response.toString());
//                if (response.isSuccessful()) {
//                    mClientIsStart = true;
//                } else {
//                    startForceTv();
//                }
//            }
//        });
//    }
//
//    private void startForceTv() {
//        if (mClientIsStart) {
//            return;
//        }
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/cloudtv/log";
//        Log.d(TAG, "path=" + path);
//        File destDir = new File(path);
//        if (!destDir.exists()) {
//            destDir.mkdirs();
//        }
//        byte[] bytepath = path.getBytes();
//        DebugLog.i(TAG, "openClient return:" + String.valueOf(startWithLog(9908, P2P_BUFFER_SIZE, bytepath)));
//        mClientIsStart = true;
//    }
//
//    public void closeClient() {
//        DebugLog.i(TAG, "[closeClient]");
//        if (mClientIsStart) {
//            DebugLog.i(TAG, "closeClient return:" + String.valueOf(stop()));
//            mClientIsStart = false;
//        }
//    }
//
//    public boolean isClientState() {
//        return mClientIsStart;
//    }
//
//
//    private native int start(int port, int size);
//
//    private native int startWithLog(int port, int size, byte[] path);
//
//    private native int stop();
//
//
//    static {
//        System.loadLibrary("forcetv");
//    }
//}

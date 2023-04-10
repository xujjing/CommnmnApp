// CallbackListener.aidl
package com.iptv.aovivo.aovod;

// Declare any non-default types here with import statements

interface CallbackListener {
    void onServiceConnected();
    void sendMsgToClient(String msg);
}

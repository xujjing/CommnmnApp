// InterfaceAidl.aidl
package com.iptv.aovivo.aovod;

import com.iptv.aovivo.aovod.CallbackListener;
interface InterfaceAidl {
    String getSchoolName();
    void registerListener(CallbackListener listener);
    void unregisterListener(CallbackListener listener);
}
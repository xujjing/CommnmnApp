package com.streambus.commonmodule.umeng;

import com.umeng.message.entity.UMessage;

public interface IMessageCallback {
    boolean handleIMessage(UMessage uMessage);
}

#Android.mk
LOCAL_PATH := $(call my-dir)

#自己的编译模块
include $(CLEAR_VARS)

LOCAL_MODULE    := logfile_lib
LOCAL_SRC_FILES += native-lib.cpp

LOCAL_LDLIBS    += -L$(SYSROOT)/lib -latomic -llog -lz
LOCAL_CFLAGS    += $(L_CFLAGS) -g
LOCAL_JNI_SHARED_LIBRARIES := liblogfile_lib

include $(BUILD_SHARED_LIBRARY)

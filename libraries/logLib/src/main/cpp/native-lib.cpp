#include <jni.h>
#include <string>
#include <unistd.h>
#include <unistd.h>
#include <assert.h>
#include <cwchar>
#include <android/log.h>
#include <string.h>
#include <pthread.h>
#include "native-logfile.h"
#include "ijkmedia-logfile.h"

#define TAG "native_logfile"
#define LOGD(...) NULL
//#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__))


static JavaVM *g_jvm;
static pthread_key_t g_thread_key;
static pthread_once_t g_key_once = PTHREAD_ONCE_INIT;

static void JNI_ThreadDestroyed(void* value)
{
    JNIEnv *env = (JNIEnv*) value;
    if (env != NULL) {
        //LOGE("%s: [%d] didn't call JNI_DetachThreadEnv() explicity\n", __func__, (int)gettid());
        g_jvm->DetachCurrentThread();
        pthread_setspecific(g_thread_key, NULL);
    }
}

static void make_thread_key()
{
    pthread_key_create(&g_thread_key, JNI_ThreadDestroyed);
}


jint JNI_SetupThreadEnv(JNIEnv **p_env)
{
    JavaVM *jvm = g_jvm;
    if (!jvm) {
        LOGE("JNI_GetJvm: AttachCurrentThread: NULL jvm");
        return -1;
    }

    pthread_once(&g_key_once, make_thread_key);

    JNIEnv *env = (JNIEnv*) pthread_getspecific(g_thread_key);
    if (env) {
        *p_env = env;
        return 0;
    }

    if (jvm->AttachCurrentThread(&env, NULL) == JNI_OK) {
        pthread_setspecific(g_thread_key, env);
        *p_env = env;
        return 0;
    }

    return -1;
}


struct fileTreeIo_fields {
    jclass clazz;
    jmethodID method_handlerLogAndNextIjkMediaLog;
    jmethodID method_logNative;
};
static fileTreeIo_fields g_clazz;
static void loadClass_FileTreeIoNative(JNIEnv *env) {
    jclass clazz = env->FindClass("com/yoostar/fileloggingutil/FileTreeIo");
    g_clazz.clazz = (jclass)env->NewGlobalRef(clazz);
    g_clazz.method_handlerLogAndNextIjkMediaLog = env->GetStaticMethodID(
            g_clazz.clazz, "handlerIjkLogAndNextByteBuffer","(I)Ljava/nio/ByteBuffer;");
    g_clazz.method_logNative = env->GetStaticMethodID(
            g_clazz.clazz, "logNative","(ILjava/lang/String;[B)V");
}


static bool defOpen = false;
extern "C" JNIEXPORT void JNICALL
Java_com_yoostar_fileloggingutil_FileTreeIo_setLogNative(
        JNIEnv* env, jobject thiz, jboolean isopen) {
    LOGE("FileTreeIo_setPriority isopen=%d ", isopen);
    defOpen = isopen;
}

extern "C" int native_log_file(int priority, const char* tag, const char* message){
    LOGD("native_log_file priority=%d  tag=%s  message=>%s", priority, tag, message);
    if (!defOpen) {
        return 0 ;
    }
    JNIEnv *env = NULL;
    if (JNI_OK == JNI_SetupThreadEnv(&env)) {
        jstring j_tag =  env->NewStringUTF(tag);
        LOGD("log_file j_type--j_tag");
        int len = strlen(message);
        if (len > NATIVE_LOG_BUF_SIZE) {
            len = NATIVE_LOG_BUF_SIZE;
        }
        jbyteArray jbytes = env->NewByteArray(len);
        env->SetByteArrayRegion(jbytes, 0, len, reinterpret_cast<const jbyte *>(message));
        LOGD("native_log_file message--jbytes");
        env->CallStaticVoidMethod(g_clazz.clazz, g_clazz.method_logNative, priority, j_tag, jbytes);
        LOGD("native_log_file CallStaticVoidMethod");
        if (env->ExceptionCheck()) {
            LOGE("native_log_file ExceptionCheck");
            env->ExceptionDescribe(); // writes to logcat
            env->ExceptionClear();
        }
        env->DeleteLocalRef(j_tag);
        env->DeleteLocalRef(jbytes);
        LOGD("native_log_file DeleteLocalRef End");
        return 1;
    } else {
        LOGE("native_log_file logReqNative: NULL env");
        return -1 ;
    }
}


/************************************* - IJK LOG - ****************************************/
#include <atomic>
#include <unistd.h>

static volatile bool isIjkOpen = false;
static pthread_mutex_t ijkMutex;

static char* bytebuffer;
static int capacity = 0;
static std::atomic<int> position(0);


extern "C" JNIEXPORT jint JNICALL
Java_com_yoostar_fileloggingutil_FileTreeIo_getIjkBufferPosition(
        JNIEnv *env, jobject thiz) {
    if (isIjkOpen) {
        return position.load();
    }
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_yoostar_fileloggingutil_FileTreeIo_setIjkLog(
        JNIEnv* env, jobject thiz, jboolean isopen) {
    LOGE("FileTreeIo_setIjkLog isopen=%d ", isopen);
    pthread_mutex_init(&ijkMutex, NULL);
    isIjkOpen = isopen;
}

extern "C" int ijkmedia_log_file(int priority, const char* tag, const char* message){
    LOGD("ijkmedia_log_file priority=%d  tag=%s  message=>%s", priority, tag, message);
    if (!isIjkOpen) {
        return 0;
    }
    int len = strlen(message);
    if (len == 0) {
        return 0;
    } else if (len > IJKMEDIA_LOG_BUF_SIZE){
        len = IJKMEDIA_LOG_BUF_SIZE;
    }

    char key[128] = {0};
    struct timeval tv;
    gettimeofday(&tv, NULL);
    int keyLen = sprintf(key, "%d_%d %d-%d %d/%s: ", tv.tv_sec, tv.tv_usec,
                         getpid(), gettid(), priority, tag);
    len = len + keyLen + 1;

    if (position + len >= capacity) {
        LOGD("ijkmedia_log_file pthread_mutex_lock");
        pthread_mutex_lock(&ijkMutex);
        while (isIjkOpen && (position + len > capacity)) {//双重判断
            LOGE("ijkmedia_log_file handlerLogAndNextIjkMediaLog start");
            JNIEnv *env = NULL;
            if (JNI_OK == JNI_SetupThreadEnv(&env)) {
                jobject jbuffer = env->CallStaticObjectMethod(g_clazz.clazz, g_clazz.method_handlerLogAndNextIjkMediaLog, position.load());
                if (!env->ExceptionCheck()) {
                    bytebuffer = (char *) env->GetDirectBufferAddress(jbuffer);   //获取buffer数据首地址
                    capacity = env->GetDirectBufferCapacity(jbuffer);         //获取buffer的容量
                    env->DeleteLocalRef(jbuffer);
                    position = 0;
                    LOGE("ijkmedia_log_file handlerLogAndNextIjkMediaLog Success capacity=%d", capacity);
                    break;
                }
                LOGE("ijkmedia_log_file ExceptionCheck");
                env->ExceptionDescribe();
                env->ExceptionClear();
            }
            isIjkOpen = false;
            LOGE("ijkmedia_log_file handlerLogAndNextIjkMediaLog Failed");
        }
        LOGD("ijkmedia_log_file pthread_mutex_unlock");
        pthread_mutex_unlock(&ijkMutex);
    }
    LOGD("ijkmedia_log_file write prepare isIjkOpen=%d", isIjkOpen);
    if (isIjkOpen) {
        int writePos = position.fetch_add(len);
        LOGD("ijkmedia_log_file write writePos=%d  len=%d", writePos, len);
        if (writePos + len < capacity) {
            LOGD("ijkmedia_log_file write start");
            sprintf(bytebuffer + writePos, "%s%s\n", key, message);
            LOGD("ijkmedia_log_file write end");
            return 1;
        }
    }
    return -1;
}



/************************************* - 测试 native_log_file - ****************************************/
static const char *NATIVE_TAG = "native_login";
static const char *login_data = "{\"android_id\":\"b050e46360cb6d78\",\"android_incremental_version\":\"30\",\"android_release_version\":\"6.0.1\",\"app_name\":\"RequestApiTest\",\"app_package\":\"com.iptv.stv.bmovie\",\"app_version\":\"1.9.27\",\"brand_name\":\"SuperTV\",\"deviceId\":\"AD000b050e46360cb6d78\",\"device_name\":\"MXC89L\",\"driver\":\"rk322x_box\",\"ethernet_mac\":\"ee:79:02:61:79:07\",\"hardware\":\"rk30board\",\"ijkUserAgent\":\"Android/6.0.1 RequestApiTest/1.9.27\",\"imei\":\"null\",\"kernel_version\":\"3.10.0\",\"language\":\"en\",\"mainboard\":\"rk30sdk\",\"model\":\"OS02\",\"serial_no\":\"7902617907\",\"wifi_mac\":\"3CCF5B1A352D\"}";
bool isLogin;
static pthread_t login_thread;
static pthread_t heart_thread;
static void *login_test(void *arg) {
    while (isLogin) {
        for (int i = 0; i < 1000 && isLogin; ++i) {
            usleep(500 * 1000);//500毫秒
            char tem[1024] = {0};
            sprintf(tem, "login_test i=%d  login_data=>%s", i, login_data);
//            LOGD("login_test tem=>%s", tem);
            native_log_file(NATIVE_LOGFILE_DEBUG, NATIVE_TAG, tem);
        }
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_yoostar_fileloggingutil_FileTreeIo_testLoginNative(
        JNIEnv* env, jobject thiz,  jboolean b) {
    LOGE("Java_com_yoostar_fileloggingutil_FileTreeIo_testLoginNative");
    isLogin = b;
    if (login_thread) {
        LOGE("TestNative_testPlayKey pthread_join login_thread");
        pthread_join(login_thread, NULL);
        login_thread = NULL;
    }
    if (heart_thread) {
        LOGE( "TestNative_testPlayKey pthread_join heart_thread");
        pthread_join(heart_thread, NULL);
        heart_thread = NULL;
    }
    if (b) {
        LOGE("TestNative_testPlayKey pthread_create");
        pthread_create(&login_thread, NULL, login_test, NULL);
        pthread_create(&heart_thread, NULL, login_test, NULL);
    }
}


/************************************* - 测试 ijkmedia_log_file - ****************************************/
static const char *IJK_TAG = "IJKMEDIA";
static char* mypath[] = {
        "http://xxomvovk.com/playback/Amc.m3u8?key=0000000000000000&mode=1&params=4f1d8323013d6601e1487245d48a68ccX69CFJeE5E9YX0kgQp1Mm1AZqNrw%2B%2Bas6pAx9VIg5kjoo9%2B3TSN8kY8%2FN2sQzXXYo5x6%2FBdagb6uMeym8y9w%2Bg%3D%3D&start=1583514600000&end=1583519700000&type=3",
        "http://gs9w88xe.com/hls/569.m3u8?key=0000000000000000&mode=1&params=4f1d8323013d6601e1487245d48a68ccX69CFJeE5E9YX0kgQp1Mm1AZqNrw%2B%2Bas6pAx9VIg5kjoo9%2B3TSN8kY8%2FN2sQzXXYo5x6%2FBdagb6uMeym8y9w%2Bg%3D%3D"
};
static int play_key(const char *path, char *key, char *mid){
    strncpy(key, path, 30);
    strncpy(mid, path + 30, 20);
    return 0;
}

static pthread_t test_thread;
static pthread_t test_thread2;
static pthread_t test_thread3;
static bool isplay;

static void *playkey_test(void *arg) {
    while (isplay) {
        char key[4096] = {0};
        char mid[2048] = {0};
        usleep(5 * 1000);// 5毫秒
        for (int i = 0; i < 1000 && isplay; ++i) {
            usleep(1 * 1000);//1毫秒
            memset(key, 0, 4096);
            memset(mid, 0, 2048);
            int ret = play_key(mypath[i % 2], key, mid);
            char tem[1024] = {0};
            sprintf(tem, "play_key i=%d  ret=>%d  key=>%s\n  mid=>%s", i, ret, key, mid);
//            LOGD("playkey_test tem=>%s", tem);
            ijkmedia_log_file(IJKMEDIA_LOGFILE_DEBUG, IJK_TAG, tem);
        }
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_yoostar_fileloggingutil_FileTreeIo_testIJkLogNative(
        JNIEnv* env, jobject thiz,  jboolean b) {
    LOGE("Java_com_yoostar_fileloggingutil_FileTreeIo_testIJkLogNative");
    isplay = b;
    if (test_thread) {
        LOGE("TestNative_testPlayKey pthread_join test_thread");
        pthread_join(test_thread, NULL);
        test_thread = NULL;
    }
    if (test_thread2) {
        LOGE( "TestNative_testPlayKey pthread_join test_thread2");
        pthread_join(test_thread2, NULL);
        test_thread2 = NULL;
    }
    if (test_thread3) {
        LOGE("TestNative_testPlayKey pthread_join test_thread3");
        pthread_join(test_thread3, NULL);
        test_thread3 = NULL;
    }
    if (b) {
        LOGE("TestNative_testPlayKey pthread_create");
        pthread_create(&test_thread, NULL, playkey_test, NULL);
        pthread_create(&test_thread2, NULL, playkey_test, NULL);
        pthread_create(&test_thread3, NULL, playkey_test, NULL);
    }
}


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGE("JNIEXPORT jint JNI_OnLoad");

    JNIEnv *env = NULL;
    g_jvm = vm;
    if (vm->GetEnv((void**) &env,JNI_VERSION_1_4)!= JNI_OK) {
        return -1;
    }
    assert(env != NULL);
    loadClass_FileTreeIoNative(env);
    return JNI_VERSION_1_4;
}

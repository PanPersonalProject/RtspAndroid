#include <jni.h>
#include <string>
#include <stdio.h>
#include "AndroidLog.h"
#include "RTSPStream.h"
#include "testH264VideoStreamer.h"
#include "live555MediaServer.h"
#include "CameraSource.h"

typedef struct context {
    JavaVM  *javaVM;
    jclass   RtspClz;
    jobject  RtspObj;
    jmethodID  getFrame;
} Context;

Context g_ctx;
CRTSPStream rtspSender;


extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    memset(&g_ctx, 0, sizeof(g_ctx));

    g_ctx.javaVM = vm;
    if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR; // JNI version not supported.
    }

    jclass clz = env->FindClass("pan/project/stream_pusher/StreamPushLib");
    g_ctx.RtspClz = (jclass)env->NewGlobalRef(clz);
    g_ctx.getFrame = env->GetStaticMethodID(g_ctx.RtspClz, "getFrame", "()[B");

    return  JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_pan_project_stream_1pusher_StreamPushLib_00024Companion_startRtspServer(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jstring ip,
                                                                             jint port) {
    const char *nativeIp = env->GetStringUTFChars(ip, JNI_FALSE);


//    if (rtspSender.Init()) {
//        LOGI("rtspSender.Init() success");
//    }else{
//        LOGI("rtspSender.Init() failed");
//    }
    startRtspServer(nativeIp, port);
    env->ReleaseStringUTFChars(ip, nativeIp);
}

extern char const *inputFileName;



extern "C"
JNIEXPORT void JNICALL
Java_pan_project_stream_1pusher_StreamPushLib_00024Companion_setFilePath(JNIEnv *env, jobject thiz,
                                                                         jstring file_path) {
    const char *nativeFileName = env->GetStringUTFChars(file_path, JNI_FALSE);
    inputFileName = strdup(nativeFileName);
    LOGI("inputFileName=%s", inputFileName);
    env->ReleaseStringUTFChars(file_path, nativeFileName);
}



extern "C"
JNIEXPORT void JNICALL
Java_pan_project_stream_1pusher_StreamPushLib_00024Companion_sendH264Frame(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jbyteArray array) {
    int length = env->GetArrayLength(array);
    jbyte *elements = env->GetByteArrayElements(array, NULL);
    const unsigned char *data = reinterpret_cast<const unsigned char *>(elements);
    rtspSender.SendH264Data(data, length);
    env->ReleaseByteArrayElements(array, elements, 0);
}


int getFrame(int8_t* buf) {
    JavaVM *javaVM = g_ctx.javaVM;
    JNIEnv *env;

    jint res = javaVM->GetEnv((void **) &env, JNI_VERSION_1_6);
    jbyteArray arr = (jbyteArray) env->CallStaticObjectMethod(g_ctx.RtspClz, g_ctx.getFrame);
    int count = env->GetArrayLength(arr);
    env->GetByteArrayRegion(arr, 0, count, buf);

    env->DeleteLocalRef(arr);
    return count;

}

int CameraSource::getNextFrame(int8_t* buf) {
    int c = getFrame(buf);
    return c;
}

#include <jni.h>
#include <string>
#include "testH264VideoStreamer.h"
#include "AndroidLog.h"

extern "C"
JNIEXPORT void JNICALL
Java_pan_project_stream_1pusher_StreamPushLib_00024Companion_startRtspServer(JNIEnv *env,
                                                                             jobject thiz) {
    startRtspServer();

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
#include <jni.h>
#include <string>
#include <stdio.h>
#include "AndroidLog.h"
#include "testH264VideoStreamer.h"
#include "live555MediaServer.h"
#include "CameraSource.h"
#include "ByteBuffer.h"

ByteBuffer videoBufferQueue;



extern "C"
JNIEXPORT void JNICALL
Java_pan_project_stream_1pusher_StreamPushLib_00024Companion_startRtspServer(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jstring ip,
                                                                             jint port) {
    const char *nativeIp = env->GetStringUTFChars(ip, JNI_FALSE);
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
    MediaData *mediaData=new MediaData();
    mediaData->putBuffer(data, length);
    videoBufferQueue.WriteData(mediaData);
    env->ReleaseByteArrayElements(array, elements, 0);
}



MediaData CameraSource::getNextFrame() {
    MediaData mediaData;
    videoBufferQueue.ReadData(&mediaData);
    return mediaData;
}

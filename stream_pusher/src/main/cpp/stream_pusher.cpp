#include <jni.h>
#include <string>
#include "testH264VideoStreamer.h"

extern "C" JNIEXPORT void JNICALL
Java_pan_project_stream_1pusher_StreamPushLib_startRtspServer(
        JNIEnv* env,
        jobject /* this */) {
    startRtspServer();
}
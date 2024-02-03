#include <jni.h>
#include <malloc.h>
#include <cstring>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_pan_lib_camera_1record_NativeLib_yuv420ToNV21(JNIEnv *env, jobject thiz, jint width,
                                                jint height, jobject byte_buffer_y,
                                                jint byte_buffer_y_length,
                                                jobject byte_buffer_u,
                                                jint byte_buffer_u_length,
                                                jobject byte_buffer_v,
                                                jint byte_buffer_v_length) {

    auto *y_buffer = (jbyte *) env->GetDirectBufferAddress(byte_buffer_y);
    auto *u_buffer = (jbyte *) env->GetDirectBufferAddress(byte_buffer_u);
    auto *v_buffer = (jbyte *) env->GetDirectBufferAddress(byte_buffer_v);

    if (y_buffer != nullptr && u_buffer != nullptr && v_buffer != nullptr) {

        auto *nv21Array = static_cast< jbyte *>(malloc(sizeof(jbyte) * width * height * 3 / 2));

        memcpy(nv21Array, y_buffer, byte_buffer_y_length);

        for (int i = 0; i < byte_buffer_u_length; i++) {
            nv21Array[byte_buffer_y_length + i * 2] = v_buffer[i];
            nv21Array[byte_buffer_y_length + i * 2 + 1] = u_buffer[i];
        }

        jbyteArray nv21Data = env->NewByteArray(width * height * 3 / 2);
        env->SetByteArrayRegion(nv21Data, 0, width * height * 3 / 2, nv21Array);

        free(nv21Array);

        return nv21Data;
    }
    return nullptr;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_pan_lib_camera_1record_NativeLib_yuvToNV21(JNIEnv *env, jobject thiz, jint width,
                                                jint height, jobject byte_buffer_y,
                                                jint byte_buffer_y_length,
                                                jobject byte_buffer_u,
                                                jint byte_buffer_u_length,
                                                jobject byte_buffer_v,
                                                jint byte_buffer_v_length) {


    auto *y_buffer = (jbyte *) env->GetDirectBufferAddress(byte_buffer_y);
    auto *u_buffer = (jbyte *) env->GetDirectBufferAddress(byte_buffer_u);
    auto *v_buffer = (jbyte *) env->GetDirectBufferAddress(byte_buffer_v);
    if (y_buffer != nullptr && u_buffer != nullptr && v_buffer != nullptr) {

        auto *nv21Array = static_cast< jbyte *>(malloc(sizeof(jbyte) * width * height * 3 / 2));

        memcpy(nv21Array, y_buffer, byte_buffer_y_length);
        memcpy(nv21Array + byte_buffer_y_length, v_buffer, byte_buffer_v_length);
        nv21Array[byte_buffer_y_length + byte_buffer_v_length] = u_buffer[byte_buffer_u_length - 1];

        jbyteArray nv21Data = env->NewByteArray(width * height * 3 / 2);
        env->SetByteArrayRegion(nv21Data, 0, width * height * 3 / 2, nv21Array);

        free(nv21Array);

        return nv21Data;
    }
    return nullptr;
}
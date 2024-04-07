/******************************************************************************************
 * File created by: pan
 * Creation date: 2024/2/13
 * Description: print android log
 ******************************************************************************************/
#ifndef ANDROID_LOG_H
#define ANDROID_LOG_H

#include <android/log.h>

#define LOG_TAG "stream_pusher"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
void logBytes(int length, const unsigned char *data);



#endif // ANDROID_LOG_H
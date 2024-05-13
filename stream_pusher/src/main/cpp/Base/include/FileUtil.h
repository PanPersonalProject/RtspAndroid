#ifndef RTSP_DEMO_FILEUTIL_H
#define RTSP_DEMO_FILEUTIL_H

#include <stdio.h>
#include <jni.h>

class FileUtil {

public:
    static FILE *getVideoFile();

    static void writeDataToFile(_JNIEnv *env, _jbyteArray *byteArray);


    static void writeDataToFile(const uint8_t *data, size_t size);

    static void write4LengthDataToFile(const unsigned char *data, size_t size);
};


#endif
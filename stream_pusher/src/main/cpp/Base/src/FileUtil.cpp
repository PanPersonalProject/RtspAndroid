
#include "FileUtil.h"
#include "AndroidLog.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>


FILE *FileUtil::getVideoFile() {
    const char *externalStoragePath = getenv("EXTERNAL_STORAGE");
    if (externalStoragePath == NULL) {
        LOGI("externalStoragePath is NULL");
        return NULL;
    }
    char documentPath[1024];

    strcpy(documentPath, externalStoragePath);

//    strcat(documentPath, "/Documents/demo.vedu");
    strcat(documentPath, "/Documents/test.264");

    LOGI("documentPath = %s", documentPath);
    // Open the file
    FILE *fp = fopen(documentPath, "rb+");
    if (fp == NULL) {
        LOGI("open file failed");
        return NULL;
    }
    LOGI("open file success");

    return fp;
}


void FileUtil::writeDataToFile(JNIEnv *env, jbyteArray byteArray) {

    const char *externalStoragePath = getenv("EXTERNAL_STORAGE");
    char documentPath[1024];
    strcpy(documentPath, externalStoragePath);
    strcat(documentPath, "/Documents/test.264");


    // 获取缓冲区数据
    jsize length = env->GetArrayLength(byteArray);
    jbyte *data = env->GetByteArrayElements(byteArray, nullptr);

    // 以追加和读取方式打开文件
    FILE *outputFile = fopen(documentPath, "ab+"); // "ab+" means append in binary mode and read

    if (outputFile != nullptr) {
        // 将缓冲区数据追加到文件末尾
        fwrite(data, sizeof(jbyte), length, outputFile);

        // 关闭文件
        fclose(outputFile);
    } else {
        perror("Unable to open file for appending");
    }


    env->ReleaseByteArrayElements(byteArray, data, 0);

}


void FileUtil::writeDataToFile(const uint8_t *data, size_t size) {

    const char *externalStoragePath = getenv("EXTERNAL_STORAGE");
    char documentPath[1024];
    strcpy(documentPath, externalStoragePath);
    strcat(documentPath, "/Documents/test.264");


    // 以追加和读取方式打开文件
    FILE *outputFile = fopen(documentPath, "ab+"); // "ab+" means append in binary mode and read

    if (outputFile != nullptr) {
        // 将数据写入文件
        fwrite(data, sizeof(uint8_t), size, outputFile);

        // 关闭文件
        fclose(outputFile);
    } else {
        perror("Unable to open file for writing");
    }


}


void FileUtil::write4LengthDataToFile(const unsigned char *data, size_t size) {

    const char *externalStoragePath = getenv("EXTERNAL_STORAGE");
    char documentPath[1024];
    strcpy(documentPath, externalStoragePath);
    strcat(documentPath, "/Documents/test.264");


    // 以追加和读取方式打开文件
    FILE *outputFile = fopen(documentPath, "ab+"); // "ab+" means append in binary mode and read

    if (outputFile != nullptr) {
        // 将数据写入文件
        fwrite(&size, 1, 4, outputFile);
        fwrite(data, sizeof(unsigned char), size, outputFile);

        // 关闭文件
        fclose(outputFile);
    } else {
        perror("Unable to open file for writing");
    }


}

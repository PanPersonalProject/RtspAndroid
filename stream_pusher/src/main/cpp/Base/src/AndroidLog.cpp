/******************************************************************************************
 * File created by: pan
 * Creation date: 2024/4/6
 * Description: Add a brief description of the file here.
 ******************************************************************************************/
#include <stdio.h>
#include <string>
#include <AndroidLog.h>

void logBytes(int length, const unsigned char *data) {
    std::string logMessage = "Byte Array: ";
    char byteStr[4];
    for (int i = 0; i < length; ++i) {
        snprintf(byteStr, sizeof(byteStr), "%02X ", data[i]);
        logMessage += byteStr;
    }

    LOGI("%s", logMessage.c_str());
}
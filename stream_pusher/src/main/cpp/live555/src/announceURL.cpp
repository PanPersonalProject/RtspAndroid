
#include "include/announceURL.hh"
#include "../../Base/include/AndroidLog.h"
#include <GroupsockHelper.hh> // for "weHaveAnIPv*Address()"

void printRtspUrl(const char *ip, int port, const char *streamName) {
    char rtspUrl[256];
    sprintf(rtspUrl, "rtsp://%s:%d/%s", ip, port, streamName);
    LOGI("%s", rtspUrl);
}
### rtsp live555

该组件是一个用于在 Android 平台上进行 RTSP 推流的工具，基于 Live555 库实现。通过该组件，您可以方便地将 Android 设备上的音频和视频流推送到 RTSP 服务器，以便进行实时流媒体传输。


#### 目前存在2个问题,尚未解决：

1.推流时而成功，时而失败，原因不清楚。之前也存在调用announceURL(rtspServer, sms)获取不到ipv4和ipv6 ip的情况，所以就从android端获取ip和从8554开始查找可用port，目前vlc拉流时而成功，时而失败，失败时重启电脑能解决问题。
```cpp
告知rtspurl的函数
void printRtspUrl(const char *ip, int port, const char *streamName) {
    char rtspUrl[256];
    sprintf(rtspUrl, "rtsp://%s:%d/%s", ip, port, streamName);
    LOGI("%s", rtspUrl);
}
```

2.拉流成功时，播放画面卡顿跳帧。目前排查h264编码后，getNextFrame获取的原始数据是正常的，但是CameraSource的deliverFrame函数newFrameSize > fMaxSize，目前还没找到解决方案。


##### 开发环境：
1.https://github.com/PanPersonalProject/live555_android  
live555 24年较新的版本，配置了config.android-arm64 makefile生成a文件

2.Android Studio版本: Android Studio Koala | 2023.3.2 Canary 2

如果有音视频大佬愿意指导或建议，我将不胜感激,谢谢!!!
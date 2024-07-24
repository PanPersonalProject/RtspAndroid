### rtsp live555

该组件是一个用于在 Android 平台上进行 RTSP 推流的工具，基于 Live555 库实现。通过该组件，您可以方便地将 Android 设备上的音频和视频流推送到 RTSP 服务器，以便进行实时流媒体传输。


#### 目前存在1个问题,尚未解决：

拉流成功时，播放画面卡顿跳帧。
问题可能是：
1.目前排查h264编码后，getNextFrame获取的原始数据是正常的，但是CameraSource的deliverFrame函数newFrameSize > fMaxSize，目前还没找到解决方案。

2.live555需要设置画面参数,我代码没设置。

##### 开发环境：
1.https://github.com/PanPersonalProject/live555_android  
live555 24年较新的版本，配置了config.android-arm64 makefile生成a文件

2.Android Studio版本: Android Studio Koala | 2023.3.2 Canary 2

### 此RTSP推流程序分两部分：

1. **采集和编码**：使用我编写的 [CameraX-H264](https://github.com/PanPersonalProject/CameraX-H264) 进行摄像头数据采集并编码为H264，同时采集麦克风数据并编码为AAC。

2. **RTSP服务器**：使用 PedroSG94 的 [RTSP-Server](https://github.com/pedroSG94/RTSP-Server) 进行推流。

#### 推送H264流

```kotlin
private val cameraPreviewInterface = object : CameraPreviewInterface {
    override fun getPreviewView(): PreviewView = binding.preview
    
    override fun onSpsPpsVps(sps: ByteBuffer, pps: ByteBuffer?, vps: ByteBuffer?) {
        val newSps = sps.duplicate()
        val newPps = pps?.duplicate()
        val newVps = vps?.duplicate() // H265需要vps
        rtspServer.setVideoInfo(newSps, newPps, newVps) // 设置SPS、PPS到SDP协议中
        if (!rtspServer.isRunning) {
            rtspServer.startServer()
        }
    }

    override fun onVideoBuffer(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtspServer.sendVideo(h264Buffer, info) // 发送H264数据
    }
}
```

#### 推送AAC流

```kotlin
private val aacInterface = object : AacInterface {
    override fun getAacData(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        rtspServer.sendAudio(aacBuffer, info) // 发送AAC数据
    }

    override fun onAudioFormat(mediaFormat: MediaFormat) {
        rtspServer.setAudioInfo(
            sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), 
            isStereo = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 2
        )
    }
}
```

完整流程请参考 [RtspCameraXFragment](app/src/main/java/pan/project/fastrtsplive/RtspCameraXFragment.kt)。

#### RTSP server Log
```kotlin
rtspServer.setLogs(needShowLog)// 是否打印所有日志
```

tag:CommandsManager 可以看到rtsp协议交互信息，和推流url地址


**Log示例**：
```log
2024-07-24 16:43:33.823  5905-8590  CommandsManager         pan.project.fastrtsplive             I  OPTIONS rtsp://192.168.0.106:1935/ RTSP/1.0
                                                                                                    CSeq: 2
                                                                                                    User-Agent: LibVLC/3.0.20 (LIVE555 Streaming Media v2016.11.28)
2024-07-24 16:43:33.839  5905-8590  CommandsManager         pan.project.fastrtsplive             I  DESCRIBE rtsp://192.168.0.106:1935/ RTSP/1.0
                                                                                                    CSeq: 3
                                                                                                    User-Agent: LibVLC/3.0.20 (LIVE555 Streaming Media v2016.11.28)
                                                                                                    Accept: application/sdp
2024-07-24 16:43:33.854  5905-8590  CommandsManager         pan.project.fastrtsplive             I  SETUP rtsp://192.168.0.106:1935/streamid=0 RTSP/1.0
                                                                                                    CSeq: 4
                                                                                                    User-Agent: LibVLC/3.0.20 (LIVE555 Streaming Media v2016.11.28)
                                                                                                    Transport: RTP/AVP;unicast;client_port=51050-51051
2024-07-24 16:43:33.863  5905-8590  CommandsManager         pan.project.fastrtsplive             I  SETUP rtsp://192.168.0.106:1935/streamid=1 RTSP/1.0
                                                                                                    CSeq: 5
                                                                                                    User-Agent: LibVLC/3.0.20 (LIVE555 Streaming Media v2016.11.28)
                                                                                                    Transport: RTP/AVP;unicast;client_port=51052-51053
                                                                                                    Session: 1185d20035702ca
2024-07-24 16:43:33.869  5905-8590  CommandsManager         pan.project.fastrtsplive             I  PLAY rtsp://192.168.0.106:1935/ RTSP/1.0
                                                                                                    CSeq: 6
                                                                                                    User-Agent: LibVLC/3.0.20 (LIVE555 Streaming Media v2016.11.28)
                                                                                                    Session: 1185d20035702ca
                                                                                                    Range: npt=0.000-
```


#### 开发环境：

Android Studio Ladybug | 2024.1.3 Canary 1
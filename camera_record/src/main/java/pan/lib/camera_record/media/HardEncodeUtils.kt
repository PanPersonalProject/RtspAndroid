package pan.lib.camera_record.media

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import java.util.concurrent.ArrayBlockingQueue

class HardEncodeUtils : Thread() {

    //The name of the codec to be instantiated.
    private val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC

    //The width of the content (in pixels)
    var width: Int = 720

    //The height of the content (in pixels)
    var height: Int = 1080
    private val hardcodeQueue = ArrayBlockingQueue<ByteArray>(50, true)
    private var isStopHardcode = false
    private lateinit var mediaCodec: MediaCodec

    fun init() {
        isStopHardcode = true



        mediaCodec = MediaCodec.createEncoderByType(mimeType)
        val mediaFormat = MediaFormat.createVideoFormat(mimeType, height, width)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,500)
        mediaFormat.setInteger(MediaFormat.KEY_DURATION,6000000)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,30)
        mediaFormat.setInteger(MediaFormat.KEY_CAPTURE_RATE,30)
        mediaFormat.setInteger(MediaFormat.KEY_CAPTURE_RATE,30)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val outputFormat = mediaCodec.outputFormat
        mediaCodec.start()
    }

    fun startRun(){
        start()
    }


    fun setData(data: ByteArray) {
        hardcodeQueue.offer(data)
    }

    override fun run() {
        super.run()
        while (isStopHardcode) {
            val data = hardcodeQueue.poll()
            if (data != null && data.isNotEmpty()) {
                encode(data)
            }
        }
        hardcodeQueue.clear()
    }

    private fun encode(data: ByteArray) {
        while (true) {
            val inputBufferIndex = mediaCodec.dequeueInputBuffer(-1)
            if (inputBufferIndex >= 0) {
                val inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex)
                inputBuffer!!.put(data)
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, data.size, 1000000, 0)
            }

            val bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
            val outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
            Log.w("Encoder", "outputBufferIndex: $outputBufferIndex")
            if (outputBufferIndex >= 0) {
                val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
                val outputFormat = mediaCodec.getOutputFormat(outputBufferIndex)
                val remaining = outputBuffer!!.remaining()
                val h264Data = ByteArray(remaining)
                outputBuffer.get(h264Data)


                mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
    }

}
package pan.lib.camera_record.media


import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * 这个类的主要功能是将输入的YUV数据编码为H.264数据。
 * @author pan qi
 * @since 2024/2/3
 */
class Encoder(private val outputBufferCallback: (ByteBuffer) -> Unit) {
    private var isStarted = false  // 用于标记编码器是否已经启动
    private var isEncoding = false  // 用于标记是否正在执行encode方法


    // MediaCodec用于编码视频数据
    private lateinit var codec: MediaCodec

    // MediaFormat用于配置MediaCodec
    private lateinit var format: MediaFormat

    // 创建一个队列来存储待处理的YUV数据
    private val queue = ConcurrentLinkedDeque<ByteBuffer>()

    // 初始化方法，用于创建和配置MediaCodec
    fun init(width: Int, height: Int) {
        // 创建一个MediaCodec用于编码，编码类型为H.264
        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

        // 创建一个MediaFormat用于配置MediaCodec，设置视频的宽度、高度、比特率、帧率、颜色格式和I帧间隔
        format =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, 125000)
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
                )
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

        // 配置MediaCodec，将MediaFormat传入
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    fun onNewYuvData(yuvData: ByteBuffer) {
//        YuvUtil.printYuvData(yuvData)
        // 当有新的YUV数据到来时，将其添加到队列中
        queue.add(yuvData)

        // 如果当前没有在执行encode函数，那么开始处理队列中的YUV数据
        if (!isEncoding) {
            processQueue()
        }
    }

    private fun processQueue() {

        if (!isStarted || queue.size < 10) {
            return
        }
        // 从队列中取出并移除一个YUV数据
        val yuvData = queue.poll()

        if (yuvData != null) {
            // 如果队列中有YUV数据，那么开始处理
            encode(yuvData)
        }
    }

    // 开始编码
    fun start() {
        codec.start()
        isStarted = true
    }

    /**
    作用：将输入的ByteBuffer编码为输出的ByteBuffer
    在编程中，缓冲区通常是一块预留的内存，用于临时存储数，以便在数据发送和接收之间提供一种缓冲机制。在这个Encoder类中，输入缓冲区和输出缓冲区是MediaCodec用于处理编码操作的关键部分。
    输入缓冲区：这是原始数据（在这个例子中是YUV格式的视频数据）被放入的地方。当你调用codec.getInputBuffer(inputBufferIndex)，你会得到一个可以写入原始数据的ByteBuffer。然后，你可以使用put方法将原始数据放入这个缓冲区。
    输出缓冲区：这是编码后的数据（在这个例子中是H.264格式的视频数据）被放入的地方。当你调用codec.getOutputBuffer(outputBufferIndex)，你会得到一个包含编码后数据的ByteBuffer。然后，你可以使用get方法将编码后的数据从这个缓冲区中取出。
    这种使用缓冲区的方式可以有效地处理数据，因为它允许数据在被处理的同时进行读写操作，从而提高了数据处理的效率。
     */
    private fun encode(input: ByteBuffer) {
        isEncoding = true
        // 获取一个输入缓冲区的索引，如果没有可用的缓冲区，这个方法将返回一个负数
        val inputBufferIndex = codec.dequeueInputBuffer(10000)
        if (inputBufferIndex < 0) {
            isEncoding = false
            return
        }
        // 获取指定索引的输入缓冲区
        val inputBuffer = codec.getInputBuffer(inputBufferIndex)
        // 将输入数据（即原始的YUV数据）放入输入缓冲区
        inputBuffer?.put(input)
        // 将填充了数据的输入缓冲区返回给编码器，编码器将在后台对这些数据进行编码
        codec.queueInputBuffer(inputBufferIndex, 0, input.remaining(), System.nanoTime(), 0)

        // 创建一个BufferInfo对象，用于接收输出缓冲区的元数据
        val bufferInfo = MediaCodec.BufferInfo()
        // 获取一个包含编码后数据的输出缓冲区的索引
        var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 1000000)
        Log.w("Encoder", "outputBufferIndex: $outputBufferIndex")
        while (outputBufferIndex >= 0) {
            // 获取指定索引的输出缓冲区，这个缓冲区包含了编码后的数据
            val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
            // 将编码后的数据复制到output缓冲区
            outputBuffer?.let {
                //回调buffer
                outputBufferCallback(it)
            }
            // 将已经读取了数据的输出缓冲区返回给编码器
            codec.releaseOutputBuffer(outputBufferIndex, false)
            // 继续获取下一个包含编码后数据的输出缓冲区的索引，如果没有更多的缓冲区，这个方法将返回一个负数
            outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
        }
        // 在encode函数的结束处，设置isEncoding为false，表示encode函数已经执行完毕
        isEncoding = false

        // encode函数执行完毕后，开始处理队列中的下一个YUV数据
        processQueue()
    }

    // 停止编码
    fun stop() {
        codec.stop()
        isStarted = false
    }

    // 释放资源
    fun release() {
        codec.release()
    }
}
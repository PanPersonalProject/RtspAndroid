package pan.lib.camera_record.media

import android.util.Log
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.util.Arrays

/**
 * @author pan qi
 * @since 2024/2/3
 */
object YuvUtil {
    fun byteBuffer(image: ImageProxy): ByteBuffer {
        // 获取图像的平面数组。对于YUV_420_888格式的图像，这个数组包含3个平面：Y平面、U平面和V平面。
        val planes = image.planes

        // 获取Y平面的数据。
        val yBuffer = planes[0].buffer

        // 获取U平面的数据。
        val uBuffer = planes[1].buffer

        // 获取V平面的数据。
        val vBuffer = planes[2].buffer

        /* 创建一个字节数组，用于存储YUV数据。字节数组的大小等于Y、U、V三个平面的数据大小之和。
                返回缓冲区中剩余的可读或可写的元素数量。在这个特定的上下文中，yBuffer.remaining(), uBuffer.remaining() 和 vBuffer.remaining() 分别返回 Y 平面、U 平面和 V 平面的剩余元素数量*/
        val yuvBytes = ByteArray(yBuffer.remaining() + uBuffer.remaining() + vBuffer.remaining())

        // 将Y平面的数据复制到字节数组中。
        yBuffer.get(yuvBytes, 0, yBuffer.remaining())

        // 将U平面的数据复制到字节数组中，紧接着Y平面的数据。
        uBuffer.get(yuvBytes, yBuffer.remaining(), uBuffer.remaining())

        // 将V平面的数据复制到字节数组中，紧接着U平面的数据。
        vBuffer.get(yuvBytes, yBuffer.remaining() + uBuffer.remaining(), vBuffer.remaining())
        val byteBuffer = ByteBuffer.wrap(yuvBytes)
        return byteBuffer
    }
//    fun yuv420ToNV21(image: ImageProxy): ByteBuffer {
//        val planes = image.planes
//        val yBuffer = planes[0].buffer
//        val uBuffer = planes[1].buffer
//        val vBuffer = planes[2].buffer
//        val yuvBytes = ByteArray(yBuffer.remaining() + uBuffer.remaining() + vBuffer.remaining())
//        yBuffer.get(yuvBytes, 0, yBuffer.remaining())
//        var vIndex = yBuffer.remaining()
//        var uIndex = yBuffer.remaining() + uBuffer.remaining() / 2
//        while (vIndex < yuvBytes.size) {
//            if (!vBuffer.hasRemaining() || !uBuffer.hasRemaining()) {
//                break
//            }
//            yuvBytes[vIndex] = vBuffer.get()
//            yuvBytes[uIndex] = uBuffer.get()
//            vIndex += 2
//            uIndex += 2
//        }
//        val byteBuffer = ByteBuffer.wrap(yuvBytes)
//        return byteBuffer
//    }
//    fun printYuvData(yuvData: ByteBuffer) {
//        val yuvArray = yuvData.array()
//
//        // Assuming the YUV data is in YUV_420_888 format
//        val ySize = yuvData.remaining() / 2
//        val uSize = ySize / 4
//
//        val yData = Arrays.copyOfRange(yuvArray, 0, ySize)
//        val uData = Arrays.copyOfRange(yuvArray, ySize, ySize + uSize)
//        val vData = Arrays.copyOfRange(yuvArray, ySize + uSize, ySize + uSize + uSize)
//
//        Log.w("Encoder", "Y data: ${yData.contentToString()}")
//        Log.w("Encoder", "U data: ${uData.contentToString()}")
//        Log.w("Encoder", "V data: ${vData.contentToString()}")
//    }

    fun yuv420ToNV21(image: ImageProxy): ByteArray {
        val nv21: ByteArray
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21Size = ySize + uSize + vSize
        nv21 = ByteArray(nv21Size)

        yBuffer.get(nv21, 0, ySize)

        val vuBuffer = ByteArray(uSize + vSize)
        uBuffer.get(vuBuffer, 0, uSize)
        vBuffer.get(vuBuffer, uSize, vSize)

        for (i in vuBuffer.indices step 2) {
            nv21[ySize + i] = vuBuffer[i + 1]
            nv21[ySize + i + 1] = vuBuffer[i]
        }

        return nv21
    }

}
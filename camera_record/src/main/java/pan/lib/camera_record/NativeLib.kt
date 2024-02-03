package pan.lib.camera_record

import java.nio.ByteBuffer

class NativeLib {

    /**
     * A native method that is implemented by the 'camera_record' native library,
     * which is packaged with this application.
     */
    external fun yuvToNV21(
        width: Int,
        height: Int,
        byteBufferY: ByteBuffer,
        byteBufferYLength: Int,
        byteBufferU: ByteBuffer,
        byteBufferULength: Int,
        byteBufferV: ByteBuffer,
        byteBufferVLength: Int,
    ): ByteArray

    companion object {
        // Used to load the 'camera_record' library on application startup.
        init {
            System.loadLibrary("camera_record")
        }
    }
}
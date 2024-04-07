package pan.project.stream_pusher

import android.content.Context
import android.util.Log
import java.util.concurrent.ArrayBlockingQueue

class StreamPushLib {

    /**
     * A native method that is implemented by the 'stream_pusher' native library,
     * which is packaged with this application.
     */

    companion object {

        // Used to load the 'stream_pusher' library on application startup.
        fun startRtspServer(context: Context) {
            val ip = NetUtil.getIp(context)
            val port = NetUtil.getAvailablePort(8554)
            Log.d("stream_pusher", "init: ip = $ip, port = $port")
            startRtspServer(ip, port)
        }


        var queue: ArrayBlockingQueue<ByteArray>? = null

        @JvmStatic
        fun getFrame(): ByteArray {
            var take = ByteArray(0)
            try {
                take = queue!!.take()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return take
        }
        private external fun startRtspServer(ip: String, port: Int)
        external fun setFilePath(filePath: String)
        external fun sendH264Frame(array: ByteArray)


        init {
            System.loadLibrary("stream_pusher")
        }
    }
}
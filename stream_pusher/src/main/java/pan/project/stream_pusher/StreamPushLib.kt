package pan.project.stream_pusher

import android.util.Log

class StreamPushLib {

    /**
     * A native method that is implemented by the 'stream_pusher' native library,
     * which is packaged with this application.
     */

    companion object {

        // Used to load the 'stream_pusher' library on application startup.
        fun startRtspServer() {
            val ip = NetUtil.getIp()
            val port = NetUtil.getAvailablePort(8554)
            Log.d("stream_pusher", "init: ip = $ip, port = $port")
            startRtspServer(ip, port)
        }


        private external fun startRtspServer(ip: String, port: Int)
        external fun setFilePath(filePath: String)
        external fun sendH264Frame(array: ByteArray)


        init {
            System.loadLibrary("stream_pusher")
        }
    }
}
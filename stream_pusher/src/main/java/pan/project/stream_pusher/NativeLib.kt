package pan.project.stream_pusher

class NativeLib {

    /**
     * A native method that is implemented by the 'stream_pusher' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'stream_pusher' library on application startup.
        init {
            System.loadLibrary("stream_pusher")
        }
    }
}
package pan.lib.streampusher

class NativeLib {

    /**
     * A native method that is implemented by the 'streampusher' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'streampusher' library on application startup.
        init {
            System.loadLibrary("streampusher")
        }
    }
}
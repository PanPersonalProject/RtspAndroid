package pan.lib.camera_record

class NativeLib {

    /**
     * A native method that is implemented by the 'camera_record' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'camera_record' library on application startup.
        init {
            System.loadLibrary("camera_record")
        }
    }
}
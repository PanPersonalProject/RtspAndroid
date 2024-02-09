package pan.lib.camera_record.test

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.OutputStream

/**
 * @author pan qi
 * @since 2024/2/5
 */

@RequiresApi(Build.VERSION_CODES.Q)
class VideoFileWriter(
    private val contentResolver: ContentResolver
) {
    private var uri: Uri? = null
    private var outputStream: OutputStream? = null
    private val contentValues = ContentValues()

    private val fileName: String = "video.data"

    init {
        createOutputStream()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createOutputStream() {
        contentValues.apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        outputStream = uri?.let { contentResolver.openOutputStream(it) }
        Log.w("VideoFileWriter", "File is saved at: $uri")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun inputBytes(
        data: ByteArray
    ) {
        outputStream?.write(data)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun closeOutputStream() {
        outputStream?.close()

        contentValues.clear()
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        uri?.let { contentResolver.update(it, contentValues, null, null) }
    }
}
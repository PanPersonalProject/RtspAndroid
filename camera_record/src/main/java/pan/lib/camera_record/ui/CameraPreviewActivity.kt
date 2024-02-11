package pan.lib.camera_record.ui

import android.Manifest
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX
import pan.lib.camera_record.databinding.ActivityCameraBinding
import pan.lib.camera_record.media.Encoder
import pan.lib.camera_record.media.YuvUtil
import pan.lib.camera_record.test.FileUtil.writeBytesToFile
import java.nio.ByteBuffer

class CameraPreviewActivity : AppCompatActivity(), PreviewCallback {
    private lateinit var binding: ActivityCameraBinding
    private var camera: Camera? = null
    private var width: Int = 1080
    private var height: Int = 720
    private var avcCodec: Encoder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)


        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    init()
                } else {
                    Toast.makeText(this, "camera权限被拒", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun init() {
        binding.surfaceview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {}

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                camera = backCamera
                startCamera(camera)

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                camera?.apply {
                    setPreviewCallback(null)
                    stopPreview()
                    release()
                }
                avcCodec?.stop()
            }
        })
    }

    private val backCamera: Camera?
        get() = try {
            Camera.open(0)
        } catch (ignored: Exception) {
            null
        }

    private fun startCamera(mCamera: Camera?) {
        mCamera?.apply {
            setPreviewCallback(this@CameraPreviewActivity)
            setDisplayOrientation(90)
            parameters = parameters.apply {
                setPreviewFormat(ImageFormat.NV21)
                val supportedPreviewSizes = getSupportedPreviewSizes()
                width = supportedPreviewSizes[0].width
                height = supportedPreviewSizes[0].height
                setPreviewSize(width, height)
            }
            setPreviewDisplay(binding.surfaceview.holder)
            startPreview()
        }
        avcCodec = Encoder { byteBuffer: ByteBuffer ->
            val data = ByteArray(byteBuffer.remaining()).also { byteBuffer.get(it) }
            writeBytesToFile(this@CameraPreviewActivity, data, "test.h264")
        }
        avcCodec?.init(
            this@CameraPreviewActivity,
            this@CameraPreviewActivity.width,
            this@CameraPreviewActivity.height
        )
        avcCodec?.start()
    }

    @Deprecated("Deprecated in Java")
    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val yuv420sp = ByteArray(width * height * 3 / 2)
        YuvUtil.NV21ToNV12(data, yuv420sp, width, height)
        avcCodec?.encode(yuv420sp)
    }
}
package pan.lib.camera_record.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.media.MediaCodecList
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pan.lib.camera_record.R
import pan.lib.camera_record.media.Encoder
import pan.lib.camera_record.media.YuvUtil
import pan.lib.camera_record.test.FileUtil.writeBytesToFile
import java.io.IOException
import java.nio.ByteBuffer


class CameraPreviewActivity : Activity(), SurfaceHolder.Callback, PreviewCallback {
    private var surfaceview: SurfaceView? = null

    private var surfaceHolder: SurfaceHolder? = null

    private var camera: Camera? = null

    private var parameters: Camera.Parameters? = null

    var width: Int = 1080

    var height: Int = 720


    private var avcCodec: Encoder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        surfaceview = findViewById(R.id.surfaceview)
        SupportAvcCodec()
        if (!checkPermissionAllGranted()) {
            ActivityCompat.requestPermissions(
                this@CameraPreviewActivity,
                PERMISSIONS_STORAGE, CAMERA_OK
            )
        } else {
            init()
        }
    }

    private fun init() {
        surfaceHolder = surfaceview!!.holder
        surfaceHolder?.addCallback(this)
    }


    private fun checkPermissionAllGranted(): Boolean {
        for (permission in PERMISSIONS_STORAGE) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false
            }
        }
        return true
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_OK) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //这里已经获取到了摄像头的权限，想干嘛干嘛了可以
                init()
            }
        }
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        camera = backCamera
        startCamera(camera)
        avcCodec = Encoder { byteBuffer: ByteBuffer ->
            val data: ByteArray
            if (byteBuffer.hasArray()) {
                data = byteBuffer.array()
            } else {
                data = ByteArray(byteBuffer.remaining())
                byteBuffer[data]
            }

            writeBytesToFile(this, data, "test.h264")
        }
        avcCodec!!.init(this, this.width, this.height)
        avcCodec!!.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (null != camera) {
            camera!!.setPreviewCallback(null)
            camera!!.stopPreview()
            camera!!.release()
            camera = null
            avcCodec!!.stop()
        }
    }


    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val yuv420sp = ByteArray(width * height * 3 / 2)
        YuvUtil.NV21ToNV12(data, yuv420sp, width, height)
        avcCodec!!.encode(yuv420sp)
    }


    @SuppressLint("NewApi")
    private fun SupportAvcCodec(): Boolean {
        for (j in MediaCodecList.getCodecCount() - 1 downTo 0) {
            val codecInfo = MediaCodecList.getCodecInfoAt(j)

            val types = codecInfo.supportedTypes
            for (type in types) {
                if (type.equals("video/avc", ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }


    private fun startCamera(mCamera: Camera?) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(this)
                mCamera.setDisplayOrientation(90)
                if (parameters == null) {
                    parameters = mCamera.parameters
                }
                parameters = mCamera.parameters
                parameters?.setPreviewFormat(ImageFormat.NV21)

                val supportedPreviewSizes = parameters?.getSupportedPreviewSizes()

                width = supportedPreviewSizes?.get(0)?.width!!
                height = supportedPreviewSizes[0]?.height!!
                parameters?.setPreviewSize(width, height)

                mCamera.parameters = parameters
                mCamera.setPreviewDisplay(surfaceHolder)
                mCamera.startPreview()
            } catch (ignored: IOException) {
            }
        }
    }

    private val backCamera: Camera?
        get() {
            var c: Camera? = null
            try {
                c = Camera.open(0)
            } catch (ignored: Exception) {
            }
            return c
        }


    companion object {
        private const val CAMERA_OK = 10001
        private val PERMISSIONS_STORAGE = arrayOf(
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )
    }
}

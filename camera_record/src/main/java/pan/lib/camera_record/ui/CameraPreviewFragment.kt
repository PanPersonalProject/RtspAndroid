package pan.lib.camera_record.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.permissionx.guolindev.PermissionX
import pan.lib.camera_record.NativeLib
import pan.lib.camera_record.databinding.FragmentCameraPreviewBinding
import pan.lib.camera_record.media.Encoder
import pan.lib.camera_record.media.HardEncodeUtils
import pan.lib.camera_record.media.Test
import pan.lib.camera_record.media.Yuv
import pan.lib.camera_record.media.YuvUtil
import java.nio.ByteBuffer
import java.util.concurrent.Executors

/**
 * @author pan qi
 * @since 2024/2/3
 */
class CameraPreviewFragment : Fragment() {

    private val TAG = "CameraPreviewFragment"
    private var _binding: FragmentCameraPreviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private lateinit var mHardEncodeUtils: HardEncodeUtils

    private val encoder = Encoder {
        Log.w("CameraPreviewFragment", "onNewYuvData: $it")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestCamera()
        binding.cameraSwitchButton.setOnClickListener {
            switchCamera()
        }

    }

    private fun requestCamera() {
        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    binding.root.post {
                        startCameraPreview()
                    }
                } else {
                    Toast.makeText(context, "camera权限被拒", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun switchCamera() {
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        startCameraPreview()
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider, imageAnalysis: ImageAnalysis) {
        val preview: Preview = Preview.Builder()
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview.setSurfaceProvider(binding.prewview.getSurfaceProvider())

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview
        )
    }

    private fun startCameraPreview() {
        val displayMetrics = resources.displayMetrics

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

//        encoder.init(width, height)
//        encoder.start()

        mHardEncodeUtils = HardEncodeUtils()
        mHardEncodeUtils.init()
        mHardEncodeUtils.startRun()
        val rotation = binding.prewview.display.rotation

        val imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // 非阻塞式，保留最新的图像
            .build()
        val nativeLib = NativeLib()
        // 创建一个新的线程执行器，并设置为图像分析器的执行器。当有新的图像可用时，分析器的代码将在这个新的线程上执行。
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
            val width = imageProxy.width
            val height = imageProxy.height
            val format = imageProxy.format
            val planeProxyY = imageProxy.planes[0]
            val planeProxyU = imageProxy.planes[1]
            val planeProxyV = imageProxy.planes[2]

            val pixelStrideY = planeProxyY.pixelStride
            val rowStrideY = planeProxyY.rowStride
            val bufferY = planeProxyY.buffer
            val remainingY = bufferY.remaining()

            val pixelStrideU = planeProxyU.pixelStride
            val rowStrideU = planeProxyU.rowStride
            val bufferU = planeProxyU.buffer
            val remainingU = bufferU.remaining()

            val pixelStrideV = planeProxyV.pixelStride
            val rowStrideV = planeProxyV.rowStride
            val bufferV = planeProxyV.buffer
            val remainingV = bufferV.remaining()
            val yuv420888tonv21 = Test.yuv_420_888toNv21(imageProxy);
//            val byteBuffer = YuvUtil.yuv420ToNV21(imageProxy)
//            val yuvToNV21 = nativeLib.yuvToNV21(
//                width,
//                height,
//                bufferY,
//                remainingY,
//                bufferU,
//                remainingU,
//                bufferV,
//                remainingV
//            )
//            encoder.onNewYuvData(ByteBuffer.wrap(yuv420888tonv21))
            mHardEncodeUtils.setData(yuv420888tonv21)
            imageProxy.close()
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider, imageAnalysis)
        }, ContextCompat.getMainExecutor(requireContext()))

    }


    override fun onDestroyView() {
//        encoder.stop()
//        encoder.release()
        super.onDestroyView()
        _binding = null
    }
}
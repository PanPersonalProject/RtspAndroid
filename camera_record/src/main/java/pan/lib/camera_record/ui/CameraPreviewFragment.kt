package pan.lib.camera_record.ui

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.permissionx.guolindev.PermissionX
import pan.lib.camera_record.databinding.FragmentCameraPreviewBinding
import pan.lib.camera_record.media.Encoder
import pan.lib.camera_record.media.YuvUtil
import pan.lib.camera_record.test.FileUtil
import pan.lib.camera_record.test.VideoFileWriter
import java.util.concurrent.Executors

/**
 * @author pan qi
 * @since 2024/2/3
 */
class CameraPreviewFragment : Fragment() {

    private var _binding: FragmentCameraPreviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private lateinit var videoFileWriter: VideoFileWriter

    private val encoder = Encoder {
        Log.w("CameraPreviewFragment", "onNewYuvData: ${it.remaining()}")
        it.rewind()
        val array = ByteArray(it.remaining())
        it.get(array)
//        videoFileWriter.inputBytes(array)
        context?.let { it1 -> FileUtil.writeBytesToFile(it1,array, "video.h264") }
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
        videoFileWriter = VideoFileWriter(requireContext().contentResolver)
        binding.cameraSwitchButton.setOnClickListener {
            switchCamera()
        }

        binding.stopButton.setOnClickListener {
            encoder.stop()
            binding.stopButton.postDelayed({
                videoFileWriter.closeOutputStream()

            }, 1000)
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

    @OptIn(ExperimentalGetImage::class)
    private fun startCameraPreview() {
        val displayMetrics = resources.displayMetrics

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        encoder.init(requireContext(),1080, 720)
        encoder.start()

        val rotation = binding.prewview.display.rotation

        val imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setTargetRotation(rotation)
            .setTargetResolution(Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER) // 非阻塞式，保留最新的图像
            .build()


        // 创建一个新的线程执行器，并设置为图像分析器的执行器。当有新的图像可用时，分析器的代码将在这个新的线程上执行。
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
//            val yuvBytes = ByteArray(imageProxy.width * imageProxy.height * 3 / 2)
//            YuvUtil.NV21ToNV12(
//                YuvUtil.YUV_420_888toNV21(imageProxy.image),
//                yuvBytes,
//                imageProxy.width,
//                imageProxy.height
//            )
//            val yuvBytes = ImageUtil.yuv_420_888toNv21(imageProxy)
            val yuvBytes = YuvUtil.YUV_420_888toNV21(imageProxy.image)
//            val yueBuffer = ByteBuffer.wrap(yuvBytes)
//            context?.let { FileUtil.writeBytesToFile(it,yuvBytes, "yuv.data") }

//            val bitmap = BitmapUtils.getBitmap(
//                yueBuffer,
//                imageProxy.width,
//                imageProxy.height,
//                imageProxy.imageInfo.rotationDegrees
//            )
////
//            activity?.runOnUiThread {
//                binding.myImageView.setImageBitmap(bitmap)
//            }

//            encoder.onNewYuvData(yuvBytes)
            encoder.encode(yuvBytes)

            imageProxy.close()
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider, imageAnalysis)
        }, ContextCompat.getMainExecutor(requireContext()))

    }


    override fun onDestroyView() {
        encoder.stop()
        encoder.release()
        super.onDestroyView()
        _binding = null
    }
}
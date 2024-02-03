package pan.lib.camera_record.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.permissionx.guolindev.PermissionX
import pan.lib.camera_record.databinding.FragmentCameraPreviewBinding

/**
 * @author pan qi
 * @since 2024/2/3
 */
class CameraPreviewFragment : Fragment() {

    private var _binding: FragmentCameraPreviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
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
                    startCameraPreview()
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
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview.setSurfaceProvider(binding.prewview.getSurfaceProvider())

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
    }

    private fun startCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package pan.project.fastrtsplive

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pan.lib.camera_record.media.StreamManager
import pan.lib.camera_record.media.audio.AacInterface
import pan.lib.camera_record.media.video.CameraPreviewInterface
import pan.lib.camera_record.media.yuv.BitmapUtils
import pan.project.fastrtsplive.databinding.FragmentCameraPreviewBinding
import pan.project.stream_pusher.StreamPushLib
import java.nio.ByteBuffer

/**
 * @author pan qi
 * @since 2024/7/23
 */
class Live555RtspCameraXFragment : Fragment() {

    private lateinit var binding: FragmentCameraPreviewBinding
    private lateinit var streamManager: StreamManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        streamManager = StreamManager(
            requireContext(),
            viewLifecycleOwner,
            binding.prewview,
            cameraPreviewInterface,
            aacInterface
        )

        binding.cameraSwitchButton.setOnClickListener {
            streamManager.switchCamera()
        }

        binding.stopButton.setOnClickListener {
            streamManager.stop()
        }
        initRtspServer()

        requestPermissions()


    }


    private fun initRtspServer() {
        lifecycleScope.launch(Dispatchers.IO) {
            StreamPushLib.startRtspServer()
        }
    }

    private val cameraPreviewInterface = object : CameraPreviewInterface {
        override fun getPreviewView(): PreviewView = binding.prewview

        override fun onNv21Frame(nv21: ByteArray, imageProxy: ImageProxy) {
            val bitmap = BitmapUtils.getBitmap(
                ByteBuffer.wrap(nv21),
                imageProxy.width,
                imageProxy.height,
                imageProxy.imageInfo.rotationDegrees
            )
            binding.myImageView.post {
                binding.myImageView.setImageBitmap(bitmap)
            }
        }

        override fun onSpsPpsVps(sps: ByteBuffer, pps: ByteBuffer?, vps: ByteBuffer?) {
            val newSps = sps.duplicate()
            val newPps = pps?.duplicate()
            val newVps = vps?.duplicate()


//            rtspServer.setVideoInfo(newSps, newPps, newVps)
//            if (!rtspServer.isRunning()) {
//                rtspServer.startServer()
//            }
        }

        override fun onVideoBuffer(h264Buffer: ByteBuffer, info: MediaCodec.BufferInfo) {

            val data: ByteArray
            if (h264Buffer.hasArray()) {
                data = h264Buffer.array()
            } else {
                data = ByteArray(h264Buffer.remaining())
                h264Buffer.get(data)
            }
            StreamPushLib.sendH264Frame(data)
        }
    }

    private val aacInterface = object : AacInterface {
        override fun getAacData(aacBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
//            rtspServer.sendAudio(aacBuffer, info)
//            Log.d("RtspCameraXFragment", "onAudioBuffer: ${info.getFormattedPresentationTime()}")
        }

        override fun onAudioFormat(mediaFormat: MediaFormat) {
            Log.d("CameraXPreviewFragment", "onAudioFormat: $mediaFormat")
//            rtspServer.setAudioInfo(sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), isStereo = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 2)

        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            handlePermissionsResult(permissions)
        }

    private fun requestPermissions() {
        // 分别检查相机和录音权限的状态
        val cameraPermissionStatus = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        )
        val recordAudioPermissionStatus = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        )

        // 如果任一权限未被授予，则发起权限请求
        if (cameraPermissionStatus != PackageManager.PERMISSION_GRANTED ||
            recordAudioPermissionStatus != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        } else {
            // 权限确认后，启动streamManager
            binding.root.post {
                streamManager.start()
            }
        }
    }

    private fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            binding.root.post {
                streamManager.start()
            }
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys.joinToString("\n")
            showPermissionDeniedDialog(deniedPermissions)
        }
    }

    private fun showPermissionDeniedDialog(deniedPermissions: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("以下权限被拒绝")
            .setMessage(deniedPermissions)
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onDestroyView() {
        streamManager.stop()
        super.onDestroyView()
    }


}

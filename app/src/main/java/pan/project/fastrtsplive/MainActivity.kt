package pan.project.fastrtsplive

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pan.project.fastrtsplive.databinding.ActivityMainBinding
import pan.project.stream_pusher.StreamPushLib

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val fragment = supportFragmentManager.findFragmentById(R.id.fragment) as CameraXPreviewFragment
//        fragment.setOutputBufferCallback {bytes->
//            // 在这里处理ByteBuffer
//            FileUtil.writeBytesToFile(this, bytes, "test.h264")
//
//        }
        startRtspServer()

    }

    private fun startRtspServer() {
        PermissionX.init(this)
            .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "LIVE555 need read and write permissions",
                    "OK",
                    "Cancel"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "Please manually enable read and write permissions for LIVE555",
                    "OK",
                    "Cancel"
                )
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        StreamPushLib.setFilePath(filesDir.absolutePath + "/test.h264")
                        StreamPushLib.startRtspServer()
                    }
                } else {
                    Toast.makeText(this, "STORAGE权限被拒", Toast.LENGTH_SHORT).show()
                }
            }
    }


}
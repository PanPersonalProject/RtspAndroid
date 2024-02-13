package pan.project.fastrtsplive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pan.lib.camera_record.test.FileUtil
import pan.lib.camera_record.ui.CameraXPreviewFragment
import pan.project.fastrtsplive.databinding.ActivityMainBinding
import pan.project.stream_pusher.StreamPushLib

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment = supportFragmentManager.findFragmentById(R.id.fragment) as CameraXPreviewFragment
        fragment.setOutputBufferCallback {bytes->
            // 在这里处理ByteBuffer
            FileUtil.writeBytesToFile(this, bytes, "test.h264")

        }
//        StreamPushLib.startRtspServer()

    }


}
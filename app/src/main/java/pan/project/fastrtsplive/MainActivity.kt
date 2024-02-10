package pan.project.fastrtsplive

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pan.lib.camera_record.ui.CameraPreviewActivity
import pan.project.fastrtsplive.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startActivity(Intent(this, CameraPreviewActivity::class.java))
        finish()
    }


}
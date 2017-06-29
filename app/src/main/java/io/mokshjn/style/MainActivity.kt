package io.mokshjn.style

import android.support.v7.app.AppCompatActivity

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import butterknife.bindView
import com.flurgle.camerakit.CameraListener
import com.flurgle.camerakit.CameraView
import org.jetbrains.anko.startActivity


class MainActivity : AppCompatActivity() {

    val camera: CameraView by bindView(R.id.camera)
    val fab: FloatingActionButton by bindView(R.id.fab)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        camera.setCameraListener(object: CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray?) {
                startActivity<Style>("image" to jpeg!!)
            }
        })
        fab.setOnClickListener { camera.captureImage() }
    }

    override fun onResume() {
        super.onResume()
        camera.start()
    }

    override fun onPause() {
        camera.stop()
        super.onPause()
    }
}

package io.mokshjn.style

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import butterknife.bindView
import com.flurgle.camerakit.CameraListener
import com.flurgle.camerakit.CameraView
import org.jetbrains.anko.startActivity
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    val camera: CameraView by bindView(R.id.camera)
    val fab: FloatingActionButton by bindView(R.id.fab)
    val RC_CAMERA = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), RC_CAMERA)
        }

        camera.setCameraListener(object: CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray) {
                var bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
                bmp = Bitmap.createScaledBitmap(bmp, 720, 1080, false)
                val os = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, os)
                startActivity<StyleActivity>("image" to os.toByteArray())
            }
        })

        fab.setOnClickListener { camera.captureImage() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RC_CAMERA -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] + grantResults[1] + grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                        finish()
                    }
                }
            }
        }
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

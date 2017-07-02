package io.mokshjn.style

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import butterknife.bindView
import com.flurgle.camerakit.CameraListener
import com.flurgle.camerakit.CameraView
import org.jetbrains.anko.startActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    val camera: CameraView by bindView(R.id.camera)
    val fab: FloatingActionButton by bindView(R.id.fab)
    val options: ImageButton by bindView(R.id.options)
    val RC_CAMERA = 100
    val RC_SELECT = 101
    val IMG_SIZE = 900

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), RC_CAMERA)
        }

        camera.setCameraListener(object: CameraListener() {
            override fun onPictureTaken(jpeg: ByteArray) {
                var bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
                bmp = Bitmap.createScaledBitmap(bmp, IMG_SIZE, IMG_SIZE, false)
                val os = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, os)
                startActivity<StyleActivity>("image" to os.toByteArray())
                finish()
            }
        })

        fab.setOnClickListener { camera.captureImage() }

        options.setOnClickListener { v: View -> showPopup(v) }
    }

    private fun  showPopup(v: View) {
        val popup = PopupMenu(this, v)
        popup.setOnMenuItemClickListener(this)
        popup.menuInflater.inflate(R.menu.menu_main, popup.menu)
        popup.show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_SELECT -> {
                    val uri = data.data
                    var istream: InputStream? = null
                    try {
                        istream = contentResolver.openInputStream(uri)
                    } catch (e: Exception) {
                        Log.i("Open", e.toString())
                    }
                    val bmp = BitmapFactory.decodeStream(istream)
                    passSelectedImage(bmp)
                }
            }
        }
    }

    fun importImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select a picture"), RC_SELECT)
    }

    fun passSelectedImage(bmp: Bitmap) {
        val nbmp = Bitmap.createScaledBitmap(bmp, IMG_SIZE, IMG_SIZE, false)
        val os = ByteArrayOutputStream()
        nbmp.compress(Bitmap.CompressFormat.JPEG, 90, os)
        startActivity<StyleActivity>("image" to os.toByteArray())
        finish()
    }

    override fun onResume() {
        super.onResume()
        camera.start()
    }

    override fun onPause() {
        camera.stop()
        super.onPause()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_import -> {
                importImage()
            }
        }
        return false
    }

}

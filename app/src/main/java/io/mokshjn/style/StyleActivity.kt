package io.mokshjn.style

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import butterknife.bindView
import io.mokshjn.style.adapter.StylesAdapter
import io.mokshjn.style.models.Style

class StyleActivity : AppCompatActivity() {

    val imageView: ImageView by bindView(R.id.styledImage)
    val recyclerView: RecyclerView by bindView(R.id.styles)

    val styles = ArrayList<Style>()
    val NUM_STYLES = 26
    var ogImage: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style)

        processInput()
        loadStyles()
        loadRecyclerView()
    }

    fun processInput() {
        ogImage = intent?.extras?.getByteArray("image")
        val bmp: Bitmap = BitmapFactory.decodeByteArray(ogImage, 0, ogImage!!.size)
        imageView.setImageBitmap(bmp)
    }

    fun loadRecyclerView() {
        val adapter = StylesAdapter(styles) {_: Int ->

        }
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }

    fun loadStyles() {
        val path = "thumbnails/style"
        for (i in 0..NUM_STYLES-1) {
            val inputstr = assets.open(path+i+".jpg")
            val bmp = BitmapFactory.decodeStream(inputstr)
            styles.add(Style("Style"+i, bmp))
        }
    }
}

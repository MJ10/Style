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
import org.jetbrains.anko.toast
import org.tensorflow.contrib.android.TensorFlowInferenceInterface

class StyleActivity : AppCompatActivity() {

    val imageView: ImageView by bindView(R.id.styledImage)
    val styles = ArrayList<Style>()
    val recyclerView: RecyclerView by bindView(R.id.styles)
    val NUM_STYLES = 26

    private val MODEL_FILE = "file:///android_asset/stylize_quantized.pb"
    private val INPUT_NODE = "input"
    private val STYLE_NODE = "style_num"
    private val OUTPUT_NODE = "transformer/expand/conv3/conv/Sigmoid"

    var inferenceInterface: TensorFlowInferenceInterface? = null

    var ogImage: ByteArray? = null
    var ogImageBmp: Bitmap? = null
    var finalImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style)

        loadTF()
        processInput()
        loadStyles()
        loadRecyclerView()
    }

    fun loadTF() {
        inferenceInterface = TensorFlowInferenceInterface(assets, MODEL_FILE)
    }

    fun processInput() {
        ogImage = intent?.extras?.getByteArray("image")
        val bmp: Bitmap = BitmapFactory.decodeByteArray(ogImage, 0, ogImage!!.size)
        ogImageBmp = bmp
        imageView.setImageBitmap(bmp)
    }

    fun loadRecyclerView() {
        val adapter = StylesAdapter(styles) { i: Int ->
            toast("Clicked " + i)
            stylizeImage(i)
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

    fun stylizeImage(styleIndex: Int) {
        val styleVals = FloatArray(NUM_STYLES, {0f})
        styleVals[styleIndex] = 1.0f

        val intValues = IntArray(720 * 720)
        val floatValues = FloatArray(720 * 720 * 3)

        val croppedBmp = Bitmap.createScaledBitmap(ogImageBmp, 720, 720, false)
        croppedBmp.getPixels(intValues, 0, croppedBmp.width, 0, 0, croppedBmp.width, croppedBmp.height)

        for (i in 0..intValues.size) {
            val int = intValues[i]
            floatValues[i * 3] = ((int shr 16) and 0xFF) / 255.0f
            floatValues[i*3 + 1] = ((int shr 8) and 0xFF) / 255.0f
            floatValues[i*3 + 2] = (int and 0xFF) / 255.0f
        }

        inferenceInterface?.feed(INPUT_NODE, floatValues, 720, 720, 3)
        inferenceInterface?.feed(STYLE_NODE, styleVals, NUM_STYLES.toLong())

        inferenceInterface?.run(arrayOf(OUTPUT_NODE), false)

        inferenceInterface?.fetch(OUTPUT_NODE, floatValues)

        for (i in 0..intValues.size) {
            intValues[i] = 0xFF000000.toInt() or ((floatValues[i * 3] * 255).toInt() shl 16) or ((floatValues[i * 3 + 1] * 255).toInt() shl 8) or (floatValues[i * 3 + 2] * 255).toInt()
        }

        croppedBmp.setPixels(intValues, 0, 720, 0, 0, 720, 720)

        imageView.setImageBitmap(croppedBmp)
    }
}

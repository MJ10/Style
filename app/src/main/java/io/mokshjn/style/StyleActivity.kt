package io.mokshjn.style

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.SeekBar
import butterknife.bindView
import io.mokshjn.style.adapter.StylesAdapter
import io.mokshjn.style.models.Style
import org.jetbrains.anko.*
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.io.File
import java.io.FileOutputStream
import java.sql.Time
import java.util.*
import android.view.ViewAnimationUtils
import android.animation.Animator
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth



class StyleActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    val imageView: ImageView by bindView(R.id.styledImage)
    val seekbar: SeekBar by bindView(R.id.styleSeek)
    val recyclerView: RecyclerView by bindView(R.id.styles)

    val IMG_SIZE = 900
    val NUM_STYLES = 26
    val styles = ArrayList<Style>()

    private val MODEL_FILE = "file:///android_asset/stylize_quantized.pb"
    private val INPUT_NODE = "input"
    private val STYLE_NODE = "style_num"
    private val OUTPUT_NODE = "transformer/expand/conv3/conv/Sigmoid"
    var inferenceInterface: TensorFlowInferenceInterface? = null

    var ogImage: ByteArray? = null
    var selectedStyle: Int = -1
    var ogImageBmp: Bitmap? = null
    var finalBmp: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style)

        loadTF()
        processInput()
        loadStyles()
        loadViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                doAsync {
                    saveBitmap()
                    uiThread {
                        toast("Saved to storage")
                    }
                }
            }
        }
        return false
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

    fun loadViews() {
        val adapter = StylesAdapter(styles) { i: Int ->
            selectedStyle = i
            val dialog = indeterminateProgressDialog(message = "Performing Style Transfer...", title = "Processing")
            doAsync {
                finalBmp = stylizeImage(i, seekbar.progress)
                uiThread {
                    setBitmap(finalBmp as Bitmap)
                    dialog.dismiss()
                }
            }

        }
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        seekbar.setOnSeekBarChangeListener(this)
    }

    fun loadStyles() {
        val path = "thumbnails/style"
        for (i in 0..NUM_STYLES-1) {
            val inputstr = assets.open(path+i+".jpg")
            val bmp = BitmapFactory.decodeStream(inputstr)
            styles.add(Style("Style"+i, bmp))
        }
    }

    fun setBitmap(bmp: Bitmap) {
        val cx = (imageView.left + imageView.right) / 2
        val cy = imageView.top
        val finalRadius = Math.max(imageView.width, imageView.height)

        val anim = ViewAnimationUtils.createCircularReveal(imageView, cx, cy, 0f, finalRadius.toFloat())
        imageView.setImageBitmap(bmp)
        anim.start()
    }

    fun saveBitmap() {
        if (finalBmp == null) {
            toast("Please apply a style first!")
            return
        }
        val now = Date()
        val timeString = now.year.toString() + now.month.toString() + now.date.toString() + "_" + Time(now.time).toString()
        val root = Environment.getExternalStorageDirectory()
        val dir = File(root, "style")
        if(!dir.exists()) {
            if (!dir.mkdirs()) {
                toast("Error creating directory")
            }
        }
        val filename = timeString + ".jpg"
        val file = File(dir, filename)
        if (file.exists()) {
            file.delete()
        }
        try {
            val out = FileOutputStream(file)
            finalBmp?.compress(Bitmap.CompressFormat.JPEG, 99, out)
            out.flush()
            out.close()
        } catch (e:Exception) {
            Log.d("SaveError", e.message)
        }
    }

    fun stylizeImage(styleIndex: Int, strength: Int): Bitmap {
        val otherStyles = (1.0f - strength) / (NUM_STYLES - 1)
        val styleVals = FloatArray(NUM_STYLES, {otherStyles})
        styleVals[styleIndex] = (strength / 100f)

        val intValues = IntArray(IMG_SIZE* IMG_SIZE)
        val floatValues = FloatArray(IMG_SIZE * IMG_SIZE * 3)

        val tempBmp = Bitmap.createScaledBitmap(ogImageBmp, IMG_SIZE, IMG_SIZE, false)
        tempBmp.getPixels(intValues, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)

        for (i in 0..intValues.size-1) {
            val int = intValues[i]
            floatValues[i * 3] = ((int shr 16) and 0xFF) / 255.0f
            floatValues[i*3 + 1] = ((int shr 8) and 0xFF) / 255.0f
            floatValues[i*3 + 2] = (int and 0xFF) / 255.0f
        }

        inferenceInterface?.feed(INPUT_NODE, floatValues, 1 , IMG_SIZE.toLong(), IMG_SIZE.toLong(), 3)
        inferenceInterface?.feed(STYLE_NODE, styleVals, NUM_STYLES.toLong())

        inferenceInterface?.run(arrayOf(OUTPUT_NODE), false)

        inferenceInterface?.fetch(OUTPUT_NODE, floatValues)

        for (i in 0..intValues.size-1) {
            intValues[i] = 0xFF000000.toInt() or ((floatValues[i * 3] * 255).toInt() shl 16) or ((floatValues[i * 3 + 1] * 255).toInt() shl 8) or (floatValues[i * 3 + 2] * 255).toInt()
        }

        val newBmp = tempBmp.copy(Bitmap.Config.ARGB_8888, true)
        newBmp.setPixels(intValues, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE)

        return newBmp
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val dialog = indeterminateProgressDialog(message = "Performing Style Transfer...", title = "Processing")
        doAsync {
            val bmp = stylizeImage(selectedStyle, seekBar.progress)
            uiThread {
                setBitmap(bmp)
                dialog.dismiss()
            }
        }
    }
}

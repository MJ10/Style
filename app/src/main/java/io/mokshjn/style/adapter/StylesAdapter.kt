package io.mokshjn.style.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import io.mokshjn.style.R
import io.mokshjn.style.models.Style

/**
 * Created by moksh on 29/6/17.
 */

class StylesAdapter(val context: Context, val list: ArrayList<Style>, val listener: (Int) -> Unit): RecyclerView.Adapter<StylesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.style_item, parent, false))
    override fun getItemCount() = list.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position], context, position, listener)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(style: Style, context: Context, pos: Int, listener: (Int) -> Unit) = with(itemView) {
            val image = findViewById(R.id.styleImage) as ImageView
            Glide.with(context)
                    .load(style.image)
                    .into(image)
            setOnClickListener { listener(pos) }
        }
    }

}
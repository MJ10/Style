package io.mokshjn.style.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import io.mokshjn.style.R
import io.mokshjn.style.models.Style

/**
 * Created by moksh on 29/6/17.
 */

class StylesAdapter constructor(val context: Context, val list: ArrayList<Style>): RecyclerView.Adapter<StylesAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.style_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 5
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            val layout = itemView.findViewById(R.id.styleItem) as LinearLayout
            val image = itemView.findViewById(R.id.styleImage) as ImageView
        }

    }

}
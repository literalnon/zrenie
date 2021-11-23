package com.example.zrenie20

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.item_image_pager_layout.view.*

class ViewPagerAdapter : RecyclerView.Adapter<PagerVH>() {

    var images: Array<Int> = arrayOf(
        R.drawable.instruction1,
        R.drawable.instruction2,
        R.drawable.instruction3,
        R.drawable.instruction4,
        R.drawable.instruction5,
        R.drawable.instruction6,
        R.drawable.instruction7,
        R.drawable.instruction8,
        R.drawable.instruction9,
        R.drawable.instruction10,
        R.drawable.instruction11,
        R.drawable.instruction12,
        R.drawable.instruction13
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH =
        PagerVH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_pager_layout, parent, false))

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: PagerVH, position: Int) {
        holder.ivImage.setBackgroundResource(images[position])
    }
}

class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val ivImage = itemView.ivImage
}
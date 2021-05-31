package com.example.zrenie20

import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zrenie20.R
import com.example.zrenie20.base.adapters.AbstractAdapterDelegate
import com.example.zrenie20.myarsample.data.VrObject
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.android.synthetic.main.item_lib_object.view.*
import kotlinx.android.synthetic.main.item_vr_object.view.*


class LibAdapter() :
    AbstractAdapterDelegate<Any, Any, LibAdapter.VrObjectsAdapterHolder>() {

    override fun isForViewType(item: Any, items: List<Any>, position: Int): Boolean {
        return item is VrObject
    }

    override fun onCreateViewHolder(parent: ViewGroup): VrObjectsAdapterHolder {
        val holder = VrObjectsAdapterHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lib_object, parent, false)
        )

        return holder
    }

    override fun onBindViewHolder(
        holder: VrObjectsAdapterHolder,
        item: Any,
        items: List<Any>,
        position: Int
    ) {
        holder.tvLabel.text = "Label"
        holder.tvTitle.text = "Название пакета"
        holder.tvContent.text = "Краткое описание"
        holder.tvTags.text = "#теги #теги #теги"
    }

    class VrObjectsAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view = itemView

        var ivLibAr = itemView.ivLibAr
        var tvLabel = itemView.tvLabel
        var tvTitle = itemView.tvTitle
        var tvContent = itemView.tvContent
        var tvTags = itemView.tvTags
    }
}

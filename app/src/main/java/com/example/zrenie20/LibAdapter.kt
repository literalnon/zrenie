package com.example.zrenie20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zrenie20.base.adapters.AbstractAdapterDelegate
import com.example.zrenie20.data.DataItemObject
import kotlinx.android.synthetic.main.item_lib_object.view.*


class LibAdapter() :
    AbstractAdapterDelegate<Any, Any, LibAdapter.VrObjectsAdapterHolder>() {

    override fun isForViewType(item: Any, items: List<Any>, position: Int): Boolean {
        return item is DataItemObject
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

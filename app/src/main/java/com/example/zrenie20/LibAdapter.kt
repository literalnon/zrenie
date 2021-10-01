package com.example.zrenie20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.zrenie20.base.adapters.AbstractAdapterDelegate
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.data.DataPackageObject
import kotlinx.android.synthetic.main.item_lib_object.view.*


class LibAdapter(
    val onSelectedItem: (DataPackageObject) -> Unit
) :
    AbstractAdapterDelegate<Any, Any, LibAdapter.VrObjectsAdapterHolder>() {

    override fun isForViewType(item: Any, items: List<Any>, position: Int): Boolean {
        return item is DataPackageObject
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
        item as DataPackageObject

        holder.tvLabel.text = item.id.toString()//"Label"
        holder.tvTitle.text = item.name//"Название пакета"
        holder.tvContent.text = item.description//"Краткое описание"
        holder.tvTags.text = item.order//"#теги #теги #теги"

        Glide.with(holder.view.context)
            .load(item?.thumbnailPath)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.ivLibAr)


        holder.view.setOnClickListener {
            onSelectedItem(item)
        }
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
